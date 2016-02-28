package org.jasig.cas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

/**
 * This is {@link CasPropertiesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casPropertiesConfiguration")
@PropertySource(value={"${cas.properties.config.location:/WEB-INF/cas.properties}"})
public class CasPropertiesConfiguration {
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean(name = "casProperties")
    public Properties casProperties() {
        return new Properties();
    }

}
