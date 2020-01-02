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
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.Marshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * This is {@link SoapAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("soapAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SoapAuthenticationConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @ConditionalOnMissingBean(name = "soapAuthenticationPrincipalFactory")
    @Bean
    public PrincipalFactory soapAuthenticationPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "soapAuthenticationAuthenticationHandler")
    @Bean
    @RefreshScope
    public AuthenticationHandler soapAuthenticationAuthenticationHandler() {
        val soap = casProperties.getAuthn().getSoap();
        val handler = new SoapAuthenticationHandler(soap.getName(),
            servicesManager.getObject(),
            soapAuthenticationPrincipalFactory(),
            soap.getOrder(),
            soapAuthenticationClient());
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(soap.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(soap.getPasswordEncoder(), applicationContext));
        return handler;
    }

    @ConditionalOnMissingBean(name = "soapAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer soapAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(soapAuthenticationAuthenticationHandler(), defaultPrincipalResolver.getObject());
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
    @RefreshScope
    @Bean
    public SoapAuthenticationClient soapAuthenticationClient() {
        val soap = casProperties.getAuthn().getSoap();
        if (StringUtils.isBlank(soap.getUrl())) {
            throw new BeanCreationException("No SOAP url is defined");
        }
        val client = new SoapAuthenticationClient();
        client.setMarshaller(soapAuthenticationMarshaller());
        client.setUnmarshaller(soapAuthenticationMarshaller());
        client.setDefaultUri(soap.getUrl());
        return client;
    }
}
