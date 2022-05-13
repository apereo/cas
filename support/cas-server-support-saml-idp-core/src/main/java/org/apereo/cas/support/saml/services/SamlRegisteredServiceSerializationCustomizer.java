package org.apereo.cas.support.saml.services;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.JacksonObjectMapperCustomizer;

import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * This is {@link SamlRegisteredServiceSerializationCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class SamlRegisteredServiceSerializationCustomizer implements JacksonObjectMapperCustomizer {
    private final CasConfigurationProperties casProperties;

    @Override
    public Map<String, ?> getInjectableValues() {
        return casProperties.getAuthn().getSamlIdp().getServices().getDefaults();
    }
}
