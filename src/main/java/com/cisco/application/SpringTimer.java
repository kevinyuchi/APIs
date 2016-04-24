package com.cisco.application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Created by kkaiwen on 4/22/2016.
 */
@SpringBootApplication
@EnableScheduling
public class SpringTimer {
    SpringApplication.run(Application.class);
}
