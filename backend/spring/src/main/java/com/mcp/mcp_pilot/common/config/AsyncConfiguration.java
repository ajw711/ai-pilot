package com.mcp.mcp_pilot.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executors;


@Configuration
public class AsyncConfiguration implements WebMvcConfigurer {

    /**
     * NATS 결과 처리 비동기 워커용 가상 스레드 Executor 빈 등록
     */
    @Bean(name = "asyncEventExecutor")
    public AsyncTaskExecutor asyncEventExecutor() {
        return new TaskExecutorAdapter(
                Executors.newVirtualThreadPerTaskExecutor()
        );
    }


    /**
     * 스프링 MVC의 비동기 요청 처리(Flux/SSE) 시 가상 스레드 실행기 사용 매핑
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(asyncEventExecutor());
    }
}
