package com.cisco.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cisco.sfdc.common.CommandLineOptions;
import com.cisco.sfdc.service.QueryService;
import com.cisco.sfdc.util.CommandLineOptionsParser;




public class SalesforceApp {
	private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        logger.info( "Initialize salesforce analysis app.");

        /* spring container*/
        String env = "loc";
        if (args.length > 0) {
            env = args[0];
        }

        System.setProperty("spring.profiles.active", env);
        @SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");

        CommandLineOptions option = new CommandLineOptions();
        String configPath = null;
        try {
            new CommandLineOptionsParser(args, option).parse();
            configPath = option.getConfigPath();
        } catch (IllegalArgumentException e) {
            logger.error("Exception : Miss some requried arguments", e);
        }
        QueryService queryService = (QueryService) context.getBean("queryService");
        logger.info("Init a salesforce query session.");
        /*initial or reset all job running status to scheduled*/
        queryService.query(configPath);
        logger.info("Query session is over.");
        logger.info("Exit salesforce analysis app");
        }
    
}
