package org.apereo.cas.config;

import org.apereo.cas.authentication.AmazonCloudDirectoryAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.clouddirectory.AmazonCloudDirectoryRepository;
import org.apereo.cas.clouddirectory.DefaultAmazonCloudDirectoryRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.clouddirectory.CloudDirectoryClient;

/**
 * This is {@link CloudDirectoryAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "cloudDirectoryAuthenticationConfiguration", proxyBeanMethods = false)
public class CloudDirectoryAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "cloudDirectoryPrincipalFactory")
    @Bean
    public PrincipalFactory cloudDirectoryPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "cloudDirectoryAuthenticationHandler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuthenticationHandler cloudDirectoryAuthenticationHandler(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
                                                                     @Qualifier("cloudDirectoryPrincipalFactory")
                                                                     final PrincipalFactory cloudDirectoryPrincipalFactory,
                                                                     @Qualifier("cloudDirectoryRepository")
                                                                     final AmazonCloudDirectoryRepository cloudDirectoryRepository,
                                                                     @Qualifier(ServicesManager.BEAN_NAME)
                                                                     final ServicesManager servicesManager) {
        val cloud = casProperties.getAuthn().getCloudDirectory();
        val handler = new AmazonCloudDirectoryAuthenticationHandler(cloud.getName(), servicesManager, cloudDirectoryPrincipalFactory, cloudDirectoryRepository, cloud);
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(cloud.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(cloud.getPasswordEncoder(), applicationContext));
        return handler;
    }

    @ConditionalOnMissingBean(name = "cloudDirectoryRepository")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AmazonCloudDirectoryRepository cloudDirectoryRepository(final CasConfigurationProperties casProperties,
                                                                   @Qualifier("amazonCloudDirectory")
                                                                   final CloudDirectoryClient amazonCloudDirectory) {
        val cloud = casProperties.getAuthn().getCloudDirectory();
        return new DefaultAmazonCloudDirectoryRepository(amazonCloudDirectory, cloud);
    }

    @ConditionalOnMissingBean(name = "amazonCloudDirectory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public CloudDirectoryClient amazonCloudDirectory(final CasConfigurationProperties casProperties) {
        val cloud = casProperties.getAuthn().getCloudDirectory();
        val builder = CloudDirectoryClient.builder();
        AmazonClientConfigurationBuilder.prepareClientBuilder(builder,
            ChainingAWSCredentialsProvider.getInstance(cloud.getCredentialAccessKey(), cloud.getCredentialSecretKey(), cloud.getProfilePath(), cloud.getProfileName()), cloud);
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "cloudDirectoryAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer cloudDirectoryAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("cloudDirectoryAuthenticationHandler")
        final AuthenticationHandler cloudDirectoryAuthenticationHandler,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(cloudDirectoryAuthenticationHandler, defaultPrincipalResolver);
    }
}
