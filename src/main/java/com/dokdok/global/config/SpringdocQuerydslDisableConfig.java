package com.dokdok.global.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringdocQuerydslDisableConfig {

    @Bean
    static BeanDefinitionRegistryPostProcessor disableSpringdocQuerydslCustomizer() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
                // Spring Boot 4 + Spring Data 4 환경에서 springdoc의 Querydsl 커스터마이저가
                // 제거된 TypeInformation 클래스를 참조해 부팅 실패가 발생함.
                // Swagger 자체는 유지하고 문제되는 빈만 제거하기 위하여 추가함.
                if (registry.containsBeanDefinition("queryDslQuerydslPredicateOperationCustomizer")) {
                    registry.removeBeanDefinition("queryDslQuerydslPredicateOperationCustomizer");
                }
            }

            @Override
            public void postProcessBeanFactory(
                    org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) {
                // no-op
            }
        };
    }
}
