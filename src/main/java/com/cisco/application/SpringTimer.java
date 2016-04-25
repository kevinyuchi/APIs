package com.cisco.application;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kkaiwen on 4/22/2016.
 */

public class SpringTimer {
    private static Logger logger = LoggerFactory.getLogger(SpringTimer.class);

    public static void main(String args[]){
            logger.info("Step into Spring Timer Task");

            /*Spring App Env*/
            String env = "loc";
            if(args.length>0){
                env =args[0];
            }
        System.setProperty("spring.profiles.active", env);

        @SuppressWarnings("resource")
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");


        }
}
