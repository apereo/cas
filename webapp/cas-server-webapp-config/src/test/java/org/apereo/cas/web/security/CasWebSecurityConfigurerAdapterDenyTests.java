package org.apereo.cas.web.security;

import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasWebAppSecurityConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * This is {@link CasWebSecurityConfigurerAdapterDenyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebAppSecurityConfiguration.class,
    SecurityAutoConfiguration.class,
    EndpointAutoConfiguration.class,
    InfoEndpointAutoConfiguration.class,
    BeansEndpointAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    WebEndpointAutoConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "management.endpoint.info.enabled=true",
        "management.endpoint.beans.enabled=true",
        "management.endpoint.env.enabled=true",
        "management.endpoints.web.exposure.include=*",

        "cas.monitor.endpoints.endpoint.beans.access=DENY",
        "cas.monitor.endpoints.endpoint.info.access=DENY",
        "cas.monitor.endpoints.endpoint.env.access=AUTHENTICATED",
        
        "cas.monitor.endpoints.form-login-enabled=true"
    })
@WebAppConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebApp")
public class CasWebSecurityConfigurerAdapterDenyTests {
    @Test
    public void verifyOperation() {
    }
}
