package org.apereo.cas.adaptors.duo.authn;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProviderFactory;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

/**
 * Implementation of AbstractMultifactorAuthenticationProviderFactory for providing
 * instances of Duo providers.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Slf4j
public class DuoMfaProviderFactory extends AbstractMultifactorAuthenticationProviderFactory {

    public DuoMfaProviderFactory(final GenericApplicationContext applicationContext,
                                 final List<DuoSecurityMultifactorProperties> duos) {
        super(applicationContext, "duoMfaProviderFactoryBean");
        duos.forEach(this::registerInstanceBean);
    }

    /**
     * Returns the Duo provider that is registered to the passed id in the
     * applicationContext.
     *
     * @param id - id of the Duo instance
     * @return - the provider
     */
    public DuoMultifactorAuthenticationProvider getProvider(final String id) {
        return super.getProvider(id);
    }

}
