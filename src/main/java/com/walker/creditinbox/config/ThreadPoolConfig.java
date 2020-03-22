package com.walker.creditinbox.config;

import com.walker.creditinbox.ApplicationParameter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
public class ThreadPoolConfig {

    @Value("${inbox.thread.coreSize}")
    private int inboxThreadPoolCoreSize;

    @Bean(ApplicationParameter.InBoxThreadName)
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(inboxThreadPoolCoreSize);
        executor.setMaxPoolSize(inboxThreadPoolCoreSize);
        return executor;
    }

}
