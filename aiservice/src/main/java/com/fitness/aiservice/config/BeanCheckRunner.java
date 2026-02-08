package com.fitness.aiservice.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BeanCheckRunner implements ApplicationRunner {
    private final ApplicationContext context;

    public BeanCheckRunner(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean exists = context.containsBean("recommendationRepository");
        System.out.println("RecommendationRepository bean exists: " + exists);
    }
}
