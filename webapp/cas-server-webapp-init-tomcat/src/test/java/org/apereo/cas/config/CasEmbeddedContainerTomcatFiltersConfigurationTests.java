package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEmbeddedContainerTomcatFiltersConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasEmbeddedContainerTomcatAutoConfiguration.class, properties = {
    "server.port=8583",
    "server.ssl.enabled=false",

    "cas.server.tomcat.csrf.enabled=true",

    "cas.server.tomcat.remote-addr.denied-client-ip-address-regex=185.81.+",
    "cas.server.tomcat.remote-addr.enabled=true"
}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableConfigurationProperties({CasConfigurationProperties.class, ServerProperties.class})
@Tag("WebApp")
@ExtendWith(CasTestExtension.class)
class CasEmbeddedContainerTomcatFiltersConfigurationTests {
    @Autowired
    @Qualifier("tomcatCsrfPreventionFilter")
    private FilterRegistrationBean tomcatCsrfPreventionFilter;

    @Autowired
    @Qualifier("tomcatRemoteAddressFilter")
    private FilterRegistrationBean tomcatRemoteAddressFilter;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(tomcatCsrfPreventionFilter.getFilter());
        assertNotNull(tomcatRemoteAddressFilter.getFilter());

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        request.setRemoteAddr("185.81.151.11");
        request.setLocalAddr("195.88.151.11");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        tomcatRemoteAddressFilter.getFilter().doFilter(request, response, new MockFilterChain());
        assertEquals(401, response.getStatus());
    }
}
