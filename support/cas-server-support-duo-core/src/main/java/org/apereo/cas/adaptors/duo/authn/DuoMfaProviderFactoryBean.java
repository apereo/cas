package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.http.HttpClient;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.beans.factory.config.AbstractFactoryBean;


/**
 * Implementation of AbstractFactoryBean used to create Duo provider instances.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Slf4j
@Setter
public class DuoMfaProviderFactoryBean extends AbstractFactoryBean<DefaultDuoMultifactorAuthenticationProvider> {

    private final HttpClient httpClient;

    private final CasConfigurationProperties casProperties;

    private String duoId;

    public DuoMfaProviderFactoryBean(final HttpClient httpClient,
                                     final CasConfigurationProperties casProperties) {
        this.httpClient = httpClient;
        this.casProperties = casProperties;
    }

    @Override
    public DefaultDuoMultifactorAuthenticationProvider createInstance() throws Exception {
        if (duoId != null) {
            LOGGER.debug("Duo = [{}]", duoId);
            val properties = casProperties.getAuthn().getMfa().getDuo().stream().filter(d -> d.getId().equals(duoId)).findFirst().get();
            val duoP = new DefaultDuoMultifactorAuthenticationProvider();
            duoP.setRegistrationUrl(properties.getRegistrationUrl());
            duoP.setDuoAuthenticationService(new BasicDuoSecurityAuthenticationService(properties, httpClient));
            duoP.setFailureMode(properties.getFailureMode());
            duoP.setBypassEvaluator(MultifactorAuthenticationUtils.newMultifactorAuthenticationProviderBypass(properties.getBypass()));
            duoP.setOrder(properties.getRank());
            duoP.setId(properties.getId());
            return duoP;
        }
        return new DefaultDuoMultifactorAuthenticationProvider();

    }

    public Class<?> getObjectType() {
        return DefaultDuoMultifactorAuthenticationProvider.class;
    }
}
