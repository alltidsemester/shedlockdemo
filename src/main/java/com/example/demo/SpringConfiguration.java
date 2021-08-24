package com.example.demo;

import java.time.Duration;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockExtender;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SpringConfiguration {

  // Need to run postgres for this: 'psql -h localhost -p 5432 -U postgres'
  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(new JdbcTemplate(dataSource));
  }

  @Scheduled(fixedDelay = 5000)
  @SchedulerLock(name = "leaderLock", lockAtMostFor = "5s", lockAtLeastFor = "5s")
  public void tryAcquireLeadership() throws InterruptedException {
    System.out.println("I am the leader!");
    while (true) {
      Thread.sleep(2000);
      try {
        LockExtender.extendActiveLock(Duration.ofSeconds(5), Duration.ofSeconds(5));
      } catch(RuntimeException e) {
        System.out.println("ERROR! Leadership lock inconsistent! If this happens, we'll need to tweak the timings!");
        break;
      }
    }
  }

}
