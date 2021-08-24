package com.example.demo;

import java.time.Duration;
import net.javacrumbs.shedlock.core.LockExtender;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.jedis.JedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.testcontainers.containers.GenericContainer;
import redis.clients.jedis.JedisPool;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SpringConfiguration {

  @Bean
  public JedisPool jedisPool() {
    var redis = new GenericContainer<>("redis:5.0.3-alpine").withExposedPorts(6379);
    redis.start();
    return new JedisPool("localhost", redis.getFirstMappedPort());
  }

  @Bean
  public LockProvider lockProvider(JedisPool jedisPool) {
    return new JedisLockProvider(jedisPool);
  }

  @Scheduled(fixedDelay = 1)
  @SchedulerLock(name = "scheduledTaskName", lockAtMostFor = "5s", lockAtLeastFor = "4s")
  public void scheduledTask1() {
    System.out.println("1 is running!");
  }

  @Scheduled(fixedDelay = 1)
  @SchedulerLock(name = "scheduledTaskName", lockAtMostFor = "5s", lockAtLeastFor = "4s")
  public void scheduledTask2() {
    System.out.println("2 is running!");
  }
}
