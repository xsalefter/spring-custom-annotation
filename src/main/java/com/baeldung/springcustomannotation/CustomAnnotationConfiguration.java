package com.baeldung.springcustomannotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.baeldung.springcustomannotation")
public class CustomAnnotationConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CustomAnnotationConfiguration.class);

    public CustomAnnotationConfiguration() {
        logger.info(">>> CustomAnnotationConfiguration is created.");
    }
}
