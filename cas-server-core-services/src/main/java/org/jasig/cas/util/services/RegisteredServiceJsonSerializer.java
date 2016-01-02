package org.jasig.cas.util.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategy;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.util.AbstractJacksonBackedJsonSerializer;

import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * Serializes registered services to JSON based on the Jackson JSON library.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class RegisteredServiceJsonSerializer extends AbstractJacksonBackedJsonSerializer<RegisteredService> {
    private static final long serialVersionUID = 7645698151115635245L;

    /**
     * Mixins are added to the object mapper in order to
     * ignore certain method signatures from serialization
     * that are otherwise treated as getters. Each mixin
     * implements the appropriate interface as a private
     * dummy class and is annotated with JsonIgnore elements
     * throughout. This helps us catch errors at compile-time
     * when the interface changes.
     * @return the prepped object mapper.
     */
    @Override
    protected ObjectMapper initializeObjectMapper() {
        final ObjectMapper mapper = super.initializeObjectMapper();
        mapper.addMixIn(RegisteredServiceProxyPolicy.class, RegisteredServiceProxyPolicyMixin.class);
        mapper.addMixIn(RegisteredServiceAccessStrategy.class, RegisteredServiceAuthorizationStrategyMixin.class);

        return mapper;
    }

    private static class RegisteredServiceProxyPolicyMixin implements RegisteredServiceProxyPolicy {

        private static final long serialVersionUID = 4854597398304437341L;

        @JsonIgnore
        @Override
        public boolean isAllowedToProxy() {
            return false;
        }

        @JsonIgnore
        @Override
        public boolean isAllowedProxyCallbackUrl(final URL pgtUrl) {
            return false;
        }
    }

    private static class RegisteredServiceAuthorizationStrategyMixin implements RegisteredServiceAccessStrategy {

        private static final long serialVersionUID = -5070823601540670379L;

        @JsonIgnore
        @Override
        public boolean isServiceAccessAllowed() {
            return false;
        }

        @JsonIgnore
        @Override
        public boolean isServiceAccessAllowedForSso() {
            return false;
        }

        @JsonIgnore
        @Override
        public  boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> principalAttributes) {
            return false;
        }

        @Override
        public URI getUnauthorizedRedirectUrl() {
            return null;
        }
    }

    @Override
    protected Class<RegisteredService> getTypeToSerialize() {
        return RegisteredService.class;
    }
}
