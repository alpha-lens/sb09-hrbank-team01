package com.team1.hrbank.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc-openapi 2.8.6의 QueryDSL 자동설정이 Spring Boot 4.x와 호환되지 않는 문제 우회.
 * 문제가 되는 빈 정의를 제거하여 ClassNotFoundException 방지.
 */
@Configuration
public class SpringDocQuerydslExcludeConfig {

    @Bean
    static BeanDefinitionRegistryPostProcessor removeQuerydslCustomizer() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                if (registry.containsBeanDefinition("queryDslQuerydslPredicateOperationCustomizer")) {
                    registry.removeBeanDefinition("queryDslQuerydslPredicateOperationCustomizer");
                }
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                // no-op
            }
        };
    }
}
