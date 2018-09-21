package org.apereo.cas.adaptors.duo.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProviderFactory;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.springframework.context.support.GenericApplicationContext;

@Slf4j
public class DuoMfaProviderFactory extends AbstractMultifactorAuthenticationProviderFactory {

    public DuoMfaProviderFactory(final GenericApplicationContext applicationContext) {
        super(applicationContext, "duoMfaProviderFactoryBean");
    }

    public DuoMultifactorAuthenticationProvider getProvider(final String id) {
        return super.getProvider(id);
    }

    public void registerInstance(final DuoSecurityMultifactorProperties properties) {
        registerInstanceBean(properties);
    }

}
