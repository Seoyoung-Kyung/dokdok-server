package com.dokdok.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Scheduler 활성화 설정
 * - @Scheduled 어노테이션 사용을 위해 필요
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
