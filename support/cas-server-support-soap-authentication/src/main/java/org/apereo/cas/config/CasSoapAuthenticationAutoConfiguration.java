package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.SoapAuthenticationClient;
import org.apereo.cas.authentication.SoapAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.soap.generated.GetSoapAuthenticationRequest;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.helpers.DefaultValidationEventHandler;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * This is {@link CasSoapAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "soap")
@AutoConfiguration
public class CasSoapAuthenticationAutoConfiguration {

    @ConditionalOnMissingBean(name = "soapAuthenticationPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory soapAuthenticationPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "soapAuthenticationAuthenticationHandler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationHandler soapAuthenticationAuthenticationHandler(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("soapAuthenticationPrincipalFactory")
        final PrincipalFactory soapAuthenticationPrincipalFactory,
        @Qualifier("soapAuthenticationClient")
        final SoapAuthenticationClient soapAuthenticationClient,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val soap = casProperties.getAuthn().getSoap();
        val handler = new SoapAuthenticationHandler(soap.getName(), servicesManager, soapAuthenticationPrincipalFactory, soap.getOrder(), soapAuthenticationClient);
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(soap.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(soap.getPasswordEncoder(), applicationContext));
        return handler;
    }

    @ConditionalOnMissingBean(name = "soapAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer soapAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("soapAuthenticationAuthenticationHandler")
        final AuthenticationHandler soapAuthenticationAuthenticationHandler,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(soapAuthenticationAuthenticationHandler, defaultPrincipalResolver);
    }

    @ConditionalOnMissingBean(name = "soapAuthenticationMarshaller")
    @Bean
    public Jaxb2Marshaller soapAuthenticationMarshaller() {
        val marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(GetSoapAuthenticationRequest.class.getPackageName());
        val props = new HashMap<String, Object>();
        props.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        props.put(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
        marshaller.setMarshallerProperties(props);
        marshaller.setValidationEventHandler(new DefaultValidationEventHandler());
        return marshaller;
    }

    @ConditionalOnMissingBean(name = "soapAuthenticationClient")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public SoapAuthenticationClient soapAuthenticationClient(
        final CasConfigurationProperties casProperties,
        @Qualifier("soapAuthenticationMarshaller")
        final Jaxb2Marshaller soapAuthenticationMarshaller) {
        val soap = casProperties.getAuthn().getSoap();
        if (StringUtils.isBlank(soap.getUrl())) {
            throw new BeanCreationException("No SOAP url is defined");
        }
        val client = new SoapAuthenticationClient();
        client.setMarshaller(soapAuthenticationMarshaller);
        client.setUnmarshaller(soapAuthenticationMarshaller);
        client.setDefaultUri(soap.getUrl());
        return client;
    }
}
