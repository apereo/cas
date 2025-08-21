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
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import java.net.URI;

/**
 * This is {@link CasAmazonCognitoAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "aws-cognito")
@AutoConfiguration
public class CasAmazonCognitoAuthenticationAutoConfiguration {

    @ConditionalOnMissingBean(name = "amazonCognitoIdentityProvider")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CognitoIdentityProviderClient amazonCognitoIdentityProvider(
        final CasConfigurationProperties casProperties) {
        val props = casProperties.getAuthn().getCognito();
        val provider = ChainingAWSCredentialsProvider.getInstance(props.getCredentialAccessKey(), props.getCredentialSecretKey());
        val builder = CognitoIdentityProviderClient.builder();
        AmazonClientConfigurationBuilder.prepareSyncClientBuilder(builder, provider, props);
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "amazonCognitoPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory amazonCognitoPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "amazonCognitoAuthenticationHandler")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public AuthenticationHandler amazonCognitoAuthenticationHandler(
        final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
        @Qualifier("amazonCognitoPrincipalFactory")
        final PrincipalFactory amazonCognitoPrincipalFactory,
        @Qualifier("amazonCognitoIdentityProvider")
        final CognitoIdentityProviderClient amazonCognitoIdentityProvider,
        @Qualifier("amazonCognitoAuthenticationJwtProcessor")
        final ConfigurableJWTProcessor amazonCognitoAuthenticationJwtProcessor,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val cognito = casProperties.getAuthn().getCognito();
        val handler = new AmazonCognitoAuthenticationAuthenticationHandler(
            amazonCognitoPrincipalFactory, amazonCognitoIdentityProvider, cognito,
            amazonCognitoAuthenticationJwtProcessor);
        handler.setState(cognito.getState());
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(cognito.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(cognito.getPasswordEncoder(), applicationContext));
        return handler;
    }

    @ConditionalOnMissingBean(name = "amazonCognitoAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer amazonCognitoAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("amazonCognitoAuthenticationHandler")
        final AuthenticationHandler amazonCognitoAuthenticationHandler,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(amazonCognitoAuthenticationHandler, defaultPrincipalResolver);
    }

    @ConditionalOnMissingBean(name = "amazonCognitoAuthenticationJwtProcessor")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ConfigurableJWTProcessor amazonCognitoAuthenticationJwtProcessor(
        final CasConfigurationProperties casProperties) throws Exception {
        val cognito = casProperties.getAuthn().getCognito();
        val resourceRetriever = new DefaultResourceRetriever(
            (int) Beans.newDuration(cognito.getConnectionTimeout()).toMillis(),
            (int) Beans.newDuration(cognito.getSocketTimeout()).toMillis());
        val region = StringUtils.defaultIfBlank(cognito.getRegion(), Region.AWS_GLOBAL.id());
        val url = String.format("https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json", region, cognito.getUserPoolId());
        val jwkSetURL = new URI(url).toURL();
        val keySource = new RemoteJWKSet(jwkSetURL, resourceRetriever);
        val jwtProcessor = new DefaultJWTProcessor();
        val keySelector = new JWSVerificationKeySelector(JWSAlgorithm.RS256, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        return jwtProcessor;
    }
}
