package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.RegisteredServiceCorsConfigurationSource;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceCorsConfigurationSourceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Web")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = {
    "cas.http-web-request.cors.allow-credentials=true",
    "cas.http-web-request.cors.allow-origins[0]=*",
    "cas.http-web-request.cors.allow-origin-patterns[0]=https://*.example.com",
    "cas.http-web-request.cors.allow-methods[0]=*",
    "cas.http-web-request.cors.allow-headers[0]=*",
    "cas.http-web-request.cors.max-age=1600",
    "cas.http-web-request.cors.exposed-headers[0]=*"
})
public class RegisteredServiceCorsConfigurationSourceTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyDefault() {
        val servicesManager = mock(ServicesManager.class);
        val argumentExtractor = mock(ArgumentExtractor.class);
        val source = new RegisteredServiceCorsConfigurationSource(casProperties,
            servicesManager, argumentExtractor);
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "example");
        val config = source.getCorsConfiguration(request);

        val cors = casProperties.getHttpWebRequest().getCors();
        assertEquals(cors.getMaxAge(), config.getMaxAge().intValue());
        assertEquals(cors.getAllowHeaders(), config.getAllowedHeaders());
        assertEquals(cors.getAllowOrigins(), config.getAllowedOrigins());
        assertEquals(cors.getAllowOriginPatterns(), config.getAllowedOriginPatterns());
        assertEquals(cors.getAllowMethods(), config.getAllowedMethods());
        assertEquals(cors.getExposedHeaders(), config.getExposedHeaders());
        assertTrue(config.getAllowCredentials().booleanValue());
    }

    @Test
    public void verifyService() {
        val props = new LinkedHashMap<String, RegisteredServiceProperty>();
        props.put(RegisteredServiceProperty.RegisteredServiceProperties.CORS_ALLOW_CREDENTIALS.getPropertyName(),
            new DefaultRegisteredServiceProperty("false"));
        props.put(RegisteredServiceProperty.RegisteredServiceProperties.CORS_MAX_AGE.getPropertyName(),
            new DefaultRegisteredServiceProperty("1000"));
        props.put(RegisteredServiceProperty.RegisteredServiceProperties.CORS_ALLOWED_HEADERS.getPropertyName(),
            new DefaultRegisteredServiceProperty(Set.of("12345")));
        props.put(RegisteredServiceProperty.RegisteredServiceProperties.CORS_ALLOWED_ORIGINS.getPropertyName(),
            new DefaultRegisteredServiceProperty(Set.of("12345")));
        props.put(RegisteredServiceProperty.RegisteredServiceProperties.CORS_ALLOWED_ORIGIN_PATTERNS.getPropertyName(),
                new DefaultRegisteredServiceProperty(Set.of("12345")));
        props.put(RegisteredServiceProperty.RegisteredServiceProperties.CORS_ALLOWED_METHODS.getPropertyName(),
            new DefaultRegisteredServiceProperty(Set.of("12345")));
        props.put(RegisteredServiceProperty.RegisteredServiceProperties.CORS_EXPOSED_HEADERS.getPropertyName(),
            new DefaultRegisteredServiceProperty(Set.of("12345")));

        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getProperties()).thenReturn(props);

        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val argumentExtractor = mock(ArgumentExtractor.class);
        when(argumentExtractor.extractService(any())).thenReturn(RegisteredServiceTestUtils.getService());

        val source = new RegisteredServiceCorsConfigurationSource(casProperties,
            servicesManager, argumentExtractor);
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "example");
        val config = source.getCorsConfiguration(request);

        assertFalse(config.getAllowCredentials().booleanValue());
        assertEquals(1000, config.getMaxAge().intValue());
        assertEquals(List.of("12345"), config.getAllowedHeaders());
        assertEquals(List.of("12345"), config.getAllowedOrigins());
        assertEquals(List.of("12345"), config.getAllowedMethods());
        assertEquals(List.of("12345"), config.getExposedHeaders());
        assertEquals(List.of("12345"), config.getAllowedOriginPatterns());
    }
}
