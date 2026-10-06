package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.RestActuatorEndpoint;
import org.apereo.cas.util.spring.RestActuatorEndpointDiscoverer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link RestActuatorEndpointDiscovererTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("ActuatorEndpoint")
@Import(RestActuatorEndpointDiscovererTests.MyTestConfiguration.class)
@SpringBootTest(classes = AbstractCasEndpointTests.SharedTestConfiguration.class,
    properties = {
        "management.endpoints.web.exposure.include=myEndpoint,myExcludedEndpoint",
        "management.endpoints.web.exposure.exclude=myExcludedEndpoint",
        "cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(CasTestExtension.class)
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

    @Test
    void verifyUnexposedEndpointsHaveNoMappings() throws Throwable {
        mockMvc.perform(get("/actuator/myEndpoint/hello"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/myHiddenEndpoint/hello"))
            .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/actuator/myExcludedEndpoint/hello"))
            .andExpect(status().is4xxClientError());
    }

    @TestConfiguration(value = "MyTestConfiguration", proxyBeanMethods = false)
    static class MyTestConfiguration {
        @Bean
        public MyEndpoint myEndpoint() {
            return new MyEndpoint();
        }

        @Bean
        public MyHiddenEndpoint myHiddenEndpoint() {
            return new MyHiddenEndpoint();
        }

        @Bean
        public MyExcludedEndpoint myExcludedEndpoint() {
            return new MyExcludedEndpoint();
        }

        @RestActuatorEndpoint
        @Endpoint(id = "myEndpoint", defaultAccess = Access.UNRESTRICTED)
        public static class MyEndpoint {

            @GetMapping("/hello")
            public String hello() {
                return "hello";
            }
        }

        @Endpoint(id = "myHiddenEndpoint", defaultAccess = Access.UNRESTRICTED)
        public static class MyHiddenEndpoint extends MyEndpoint {
        }

        @Endpoint(id = "myExcludedEndpoint", defaultAccess = Access.UNRESTRICTED)
        public static class MyExcludedEndpoint extends MyEndpoint {
        }
    }
}
