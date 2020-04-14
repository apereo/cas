package org.apereo.cas.config;

import org.apereo.cas.authentication.AmazonCognitoAuthenticationAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

/**
 * This is {@link AmazonCognitoAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("amazonCognitoAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class AmazonCognitoAuthenticationConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @ConditionalOnMissingBean(name = "amazonCognitoIdentityProvider")
    @Bean
    @RefreshScope
    public AWSCognitoIdentityProvider amazonCognitoIdentityProvider() {
        val props = casProperties.getAuthn().getCognito();

        val config = AmazonClientConfigurationBuilder.buildClientConfiguration(props);
        val provider = ChainingAWSCredentialsProvider.getInstance(props.getCredentialAccessKey(),
            props.getCredentialSecretKey(), props.getCredentialsPropertiesFile());

        val clientBuilder = AWSCognitoIdentityProviderClientBuilder.standard()
            .withCredentials(provider)
            .withClientConfiguration(config);

        val region = StringUtils.defaultIfBlank(props.getRegionOverride(), props.getRegion());

        if (StringUtils.isNotBlank(props.getEndpoint())) {
            LOGGER.trace("Setting Cognito endpoint [{}]", props.getEndpoint());
            clientBuilder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(props.getEndpoint(), region));
        }

        if (StringUtils.isNotBlank(region)) {
            LOGGER.trace("Setting Cognito client region [{}]", props.getRegion());
            clientBuilder.withRegion(region);
        }

        return clientBuilder.build();
    }

    @ConditionalOnMissingBean(name = "amazonCognitoPrincipalFactory")
    @Bean
    public PrincipalFactory amazonCognitoPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "amazonCognitoAuthenticationHandler")
    @Bean(destroyMethod = "destroy")
    @RefreshScope
    public AuthenticationHandler amazonCognitoAuthenticationHandler() {
        val cognito = casProperties.getAuthn().getCognito();
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(cognito.getName(),
            servicesManager.getObject(),
            amazonCognitoPrincipalFactory(),
            amazonCognitoIdentityProvider(),
            cognito,
            amazonCognitoAuthenticationJwtProcessor());
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(cognito.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(cognito.getPasswordEncoder(), applicationContext));
        return handler;
    }

    @ConditionalOnMissingBean(name = "amazonCognitoAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer amazonCognitoAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(amazonCognitoAuthenticationHandler(), defaultPrincipalResolver.getObject());
    }

    @ConditionalOnMissingBean(name = "amazonCognitoAuthenticationJwtProcessor")
    @Bean
    @RefreshScope
    @SneakyThrows
    public ConfigurableJWTProcessor amazonCognitoAuthenticationJwtProcessor() {
        val cognito = casProperties.getAuthn().getCognito();
        val resourceRetriever = new DefaultResourceRetriever(cognito.getConnectionTimeout(), cognito.getRequestTimeout());
        val region = StringUtils.defaultIfBlank(cognito.getRegionOverride(), cognito.getRegion());
        val url = String.format("https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json", region, cognito.getUserPoolId());
        val jwkSetURL = new URL(url);
        val keySource = new RemoteJWKSet(jwkSetURL, resourceRetriever);
        val jwtProcessor = new DefaultJWTProcessor();
        val keySelector = new JWSVerificationKeySelector(JWSAlgorithm.RS256, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        return jwtProcessor;
    }
}
