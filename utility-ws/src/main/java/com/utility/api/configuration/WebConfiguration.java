package com.utility.api.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAutoConfiguration
@ComponentScan("com.utility.api")
@EnableAspectJAutoProxy
public class WebConfiguration {

}

