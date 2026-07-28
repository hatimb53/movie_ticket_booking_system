package com.mtbs.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so {@code @CreatedDate}/{@code @LastModifiedDate} on
 * {@link com.mtbs.common.domain.BaseEntity} are populated automatically.
 */
@Configuration
@EnableJpaAuditing
public class PersistenceConfig {
}
