package org.apereo.cas.support.inwebo.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.inwebo.InweboMultifactorAuthenticationProvider;
import org.apereo.cas.support.inwebo.authentication.InweboAuthenticationDeviceMetadataPopulator;
import org.apereo.cas.support.inwebo.authentication.InweboAuthenticationHandler;
import org.apereo.cas.support.inwebo.authentication.InweboCredential;
import org.apereo.cas.support.inwebo.service.InweboService;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * The Inwebo MFA authentication configuration.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Configuration("inweboAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class InweboAuthenticationConfiguration {

    @Autowired
    @Qualifier("inweboBypassEvaluator")
    private ObjectProvider<MultifactorAuthenticationProviderBypassEvaluator> inweboBypassEvaluator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("failureModeEvaluator")
    private ObjectProvider<MultifactorAuthenticationFailureModeEvaluator> failureModeEvaluator;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("inweboService")
    private ObjectProvider<InweboService> inweboService;

    @Bean
    @ConditionalOnMissingBean(name = "inweboMultifactorAuthenticationProvider")
    @RefreshScope
    public MultifactorAuthenticationProvider inweboMultifactorAuthenticationProvider() {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val p = new InweboMultifactorAuthenticationProvider();
        p.setBypassEvaluator(inweboBypassEvaluator.getObject());
        p.setFailureMode(inwebo.getFailureMode());
        p.setFailureModeEvaluator(failureModeEvaluator.getObject());
        p.setOrder(inwebo.getRank());
        p.setId(inwebo.getId());
        return p;
    }

    @ConditionalOnMissingBean(name = "inweboPrincipalFactory")
    @Bean
    public PrincipalFactory inweboPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "inweboAuthenticationHandler")
    @Bean
    @RefreshScope
    public AuthenticationHandler inweboAuthenticationHandler() {
        return new InweboAuthenticationHandler(servicesManager.getObject(), inweboPrincipalFactory(),
                casProperties.getAuthn().getMfa().getInwebo(), inweboService.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboAuthenticationMetaDataPopulator")
    @RefreshScope
    public AuthenticationMetaDataPopulator inweboAuthenticationMetaDataPopulator() {
        return new AuthenticationContextAttributeMetaDataPopulator(
                casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(),
                inweboAuthenticationHandler(),
                inweboMultifactorAuthenticationProvider().getId()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboAuthenticationDeviceMetadataPopulator")
    public AuthenticationMetaDataPopulator inweboAuthenticationDeviceMetadataPopulator() {
        return new InweboAuthenticationDeviceMetadataPopulator();
    }

    @ConditionalOnMissingBean(name = "inweboAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer inweboAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandler(inweboAuthenticationHandler());
            plan.registerAuthenticationMetadataPopulators(Arrays.asList(inweboAuthenticationMetaDataPopulator(),
                    inweboAuthenticationDeviceMetadataPopulator()));
            plan.registerAuthenticationHandlerResolver(
                    new ByCredentialTypeAuthenticationHandlerResolver(InweboCredential.class));
        };
    }
}
