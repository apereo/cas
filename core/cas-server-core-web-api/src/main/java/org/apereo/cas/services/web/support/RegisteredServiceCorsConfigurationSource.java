package org.apereo.cas.services.web.support;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Set;

/**
 * This is {@link RegisteredServiceCorsConfigurationSource}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor
public class RegisteredServiceCorsConfigurationSource implements CorsConfigurationSource {
    private final CasConfigurationProperties casProperties;
    private final ServicesManager servicesManager;
    private final ArgumentExtractor argumentExtractor;

    @Override
    public CorsConfiguration getCorsConfiguration(final HttpServletRequest request) {
        val cors = casProperties.getHttpWebRequest().getCors();
        val config = new CorsConfiguration();

        val service = argumentExtractor.extractService(request);
        LOGGER.trace("Extracted service [{}] from the request", service);

        val registeredService = servicesManager.findServiceBy(service);

        config.setAllowCredentials(cors.isAllowCredentials());
        config.setMaxAge(cors.getMaxAge());
        config.setAllowedOrigins(cors.getAllowOrigins());
        config.setAllowedOriginPatterns(cors.getAllowOriginPatterns());
        config.setAllowedMethods(cors.getAllowMethods());
        config.setAllowedHeaders(cors.getAllowHeaders());
        config.setExposedHeaders(cors.getExposedHeaders());

        if (registeredService != null) {
            LOGGER.trace("Evaluating registered service [{}] for cors configuration", registeredService);
            if (RegisteredServiceProperties.CORS_ALLOW_CREDENTIALS.isAssignedTo(registeredService)) {
                val result = RegisteredServiceProperties.CORS_ALLOW_CREDENTIALS.getPropertyBooleanValue(registeredService);
                config.setAllowCredentials(result);
            }
            if (RegisteredServiceProperties.CORS_MAX_AGE.isAssignedTo(registeredService)) {
                val result = RegisteredServiceProperties.CORS_MAX_AGE.getPropertyLongValue(registeredService);
                config.setMaxAge(result);
            }
            if (RegisteredServiceProperties.CORS_ALLOWED_ORIGINS.isAssignedTo(registeredService)) {
                val result = RegisteredServiceProperties.CORS_ALLOWED_ORIGINS.getPropertyValues(registeredService, Set.class);
                if (result != null) {
                    config.setAllowedOrigins(new ArrayList<>(result));
                }
            }
            if (RegisteredServiceProperties.CORS_ALLOWED_ORIGIN_PATTERNS.isAssignedTo(registeredService)) {
                val result = RegisteredServiceProperties.CORS_ALLOWED_ORIGIN_PATTERNS.getPropertyValues(registeredService, Set.class);
                if (result != null) {
                    config.setAllowedOriginPatterns(new ArrayList<>(result));
                }
            }
            if (RegisteredServiceProperties.CORS_ALLOWED_METHODS.isAssignedTo(registeredService)) {
                val result = RegisteredServiceProperties.CORS_ALLOWED_METHODS.getPropertyValues(registeredService, Set.class);
                if (result != null) {
                    config.setAllowedMethods(new ArrayList<>(result));
                }
            }
            if (RegisteredServiceProperties.CORS_ALLOWED_HEADERS.isAssignedTo(registeredService)) {
                val result = RegisteredServiceProperties.CORS_ALLOWED_HEADERS.getPropertyValues(registeredService, Set.class);
                if (result != null) {
                    config.setAllowedHeaders(new ArrayList<>(result));
                }
            }
            if (RegisteredServiceProperties.CORS_EXPOSED_HEADERS.isAssignedTo(registeredService)) {
                val result = RegisteredServiceProperties.CORS_EXPOSED_HEADERS.getPropertyValues(registeredService, Set.class);
                if (result != null) {
                    config.setExposedHeaders(new ArrayList<>(result));
                }
            }
        }

        return config;
    }
}
