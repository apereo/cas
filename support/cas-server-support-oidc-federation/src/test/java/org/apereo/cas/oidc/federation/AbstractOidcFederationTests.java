package org.apereo.cas.oidc.federation;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * This is {@link AbstractOidcFederationTests}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureMockMvc
abstract class AbstractOidcFederationTests {

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("mockMvc")
    protected MockMvc mockMvc;

    protected static RequestPostProcessor withHttpRequestProcessor() {
        return request -> {
            request.setScheme("https");
            request.setServerName("sso.example.org");
            request.setContextPath("/cas");
            request.setServletPath("/cas");
            request.setServerPort(443);
            return request;
        };
    }
}
