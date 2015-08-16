package org.amirk.website.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {
                   "org.amirk.website"
               },
               excludeFilters = {
                   @ComponentScan.Filter(type=FilterType.ANNOTATION, value=EnableWebMvc.class)
               })
public class AppConfig {
    
}
