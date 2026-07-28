package com.mtbs.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables {@code @Scheduled} jobs (the hold sweeper, and later the reminder job). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
