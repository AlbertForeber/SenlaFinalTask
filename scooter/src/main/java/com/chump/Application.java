package com.chump;

import com.chump.config.ScooterKafkaConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Application {

    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ScooterKafkaConfig.class);
        context.registerShutdownHook(); // graceful shutdown при SIGTERM

        // Не дает main-потоку завершится
        Thread.currentThread().join();
    }
}
