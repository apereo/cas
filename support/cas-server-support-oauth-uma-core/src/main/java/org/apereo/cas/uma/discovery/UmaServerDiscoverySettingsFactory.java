package org.apereo.cas.uma.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.FactoryBean;

/**
 * This is {@link UmaServerDiscoverySettingsFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class UmaServerDiscoverySettingsFactory implements FactoryBean<UmaServerDiscoverySettings> {
    private final CasConfigurationProperties casProperties;

    @Override
    public UmaServerDiscoverySettings getObject() {
        val uma = casProperties.getAuthn().getUma();
        return new UmaServerDiscoverySettings(casProperties, uma.getIssuer());
    }

    @Override
    public Class<?> getObjectType() {
        return UmaServerDiscoverySettings.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
