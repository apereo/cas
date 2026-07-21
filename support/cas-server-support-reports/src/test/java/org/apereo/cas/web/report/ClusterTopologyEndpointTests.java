package org.apereo.cas.web.report;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link ClusterTopologyEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@TestPropertySource(properties = "management.endpoint.clusterTopology.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
@Import(ClusterTopologyEndpointTests.ClusterTopologyEndpointTestConfiguration.class)
class ClusterTopologyEndpointTests extends AbstractCasEndpointTests {
    @Test
    void verifyTopologyOperationWithoutRequestContentType() throws Exception {
        mockMvc.perform(get("/actuator/clusterTopology/topology")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void verifyDiscoveryOperationWithoutRequestContentType() throws Exception {
        mockMvc.perform(get("/actuator/clusterTopology/discovery")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @TestConfiguration(value = "ClusterTopologyEndpointTestConfiguration", proxyBeanMethods = false)
    static class ClusterTopologyEndpointTestConfiguration {
        @Bean
        DiscoveryClient discoveryClient() {
            val discoveryClient = mock(DiscoveryClient.class);
            when(discoveryClient.getServices()).thenReturn(List.of());
            return discoveryClient;
        }
    }
}
