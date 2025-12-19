package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.util.spring.RestActuatorEndpoint;
import org.apereo.cas.util.spring.RestActuatorEndpointDiscoverer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestActuatorEndpointDiscovererTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("ActuatorEndpoint")
@Import(RestActuatorEndpointDiscovererTests.MyTestConfiguration.class)
class RestActuatorEndpointDiscovererTests extends AbstractCasEndpointTests {

    @Autowired
    @Qualifier("myEndpoint")
    private MyTestConfiguration.MyEndpoint myEndpoint;

    @Autowired
    @Qualifier("restControllerEndpointDiscoverer")
    private RestActuatorEndpointDiscoverer controllerEndpointDiscoverer;

    @Test
    void verifyOperation() {
        assertNotNull(myEndpoint);
        assertEquals(1, controllerEndpointDiscoverer.getEndpoints().size());
        val endpoint = controllerEndpointDiscoverer.getEndpoints().iterator().next();
        assertEquals("myEndpoint", endpoint.getEndpointId().toString());
    }

    @TestConfiguration(value = "MyTestConfiguration", proxyBeanMethods = false)
    static class MyTestConfiguration {
        @Bean
        public MyEndpoint myEndpoint() {
            return new MyEndpoint();
        }

        @RestActuatorEndpoint
        @Endpoint(id = "myEndpoint", defaultAccess = Access.UNRESTRICTED)
        public static class MyEndpoint {

            @GetMapping("/hello")
            public String hello() {
                return "hello";
            }
        }
    }
}
