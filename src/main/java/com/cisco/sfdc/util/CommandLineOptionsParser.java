package com.cisco.sfdc.util;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.sfdc.common.CommandLineOptions;




public class CommandLineOptionsParser {

    private static final Logger logger = LoggerFactory
            .getLogger(CommandLineOptionsParser.class);

    private String[] args = null;
    Options options = new Options();
    CommandLineOptions elqOptions = null;

    @SuppressWarnings("static-access")
    public CommandLineOptionsParser(String[] args, CommandLineOptions elqOptions) {
        this.args = args;
        this.elqOptions = elqOptions;
        options.addOption(OptionBuilder.hasArg().withLongOpt("help")
                .isRequired(false).withType(String.class).create());

        options.addOption(OptionBuilder.hasArg().withLongOpt("configPath")
                .isRequired(false).withType(String.class).create());
        
    }

    public void parse() {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;
        logger.info("Begin with parsing!!");
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("help"))
                this.help();
            if (cmd.hasOption("configPath"))
                this.getConfigPath(cmd.getOptionValue("configPath"));
        } catch (ParseException e) {
            logger.error("ParseException :", e);
            throw new IllegalArgumentException(e);
        }
        logger.info("Done with parsing!!");
    }

    public void help() {
        logger.info("### This is the help option!");
    }

    public void getConfigPath(String configPath) {
        logger.info("configPath: " + configPath);
        this.elqOptions.setConfigPath(configPath);
    }

}
