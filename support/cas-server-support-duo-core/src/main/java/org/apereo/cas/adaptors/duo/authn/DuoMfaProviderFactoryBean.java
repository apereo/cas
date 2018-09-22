package org.apereo.cas.adaptors.duo.authn;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.util.http.HttpClient;
import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.cloud.context.scope.GenericScope;

import java.util.function.Supplier;

/**
 * Implementation of AbstractFactoryBean used to create Duo provider instances.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Slf4j
@Setter
public class DuoMfaProviderFactoryBean extends AbstractFactoryBean<DuoMultifactorAuthenticationProvider> {

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    private DuoSecurityMultifactorProperties properties;

    @Override
    public DefaultDuoMultifactorAuthenticationProvider createInstance() throws Exception {
        if (properties != null) {
            LOGGER.debug("Duo = [{}]", properties.getId());
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
