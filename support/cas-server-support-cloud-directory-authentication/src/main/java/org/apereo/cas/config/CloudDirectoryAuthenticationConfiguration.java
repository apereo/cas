package org.apereo.cas.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.clouddirectory.AmazonCloudDirectory;
import com.amazonaws.services.clouddirectory.AmazonCloudDirectoryClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CloudDirectoryAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.clouddirectory.CloudDirectoryRepository;
import org.apereo.cas.clouddirectory.DefaultCloudDirectoryRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * This is {@link CloudDirectoryAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("cloudDirectoryAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CloudDirectoryAuthenticationConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @ConditionalOnMissingBean(name = "cloudDirectoryPrincipalFactory")
    @Bean
    public PrincipalFactory cloudDirectoryPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "amazonCloudDirectory")
    @Bean
    @RefreshScope
    public AuthenticationHandler cloudDirectoryAuthenticationHandler() {

        final var cloud = casProperties.getAuthn().getCloudDirectory();

        final var handler = new CloudDirectoryAuthenticationHandler(cloud.getName(), servicesManager,
            cloudDirectoryPrincipalFactory(), cloudDirectoryRepository(), cloud);
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(cloud.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(cloud.getPasswordEncoder()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "cloudDirectoryRepository")
    @Bean
    @RefreshScope
    public CloudDirectoryRepository cloudDirectoryRepository() {
        final var cloud = casProperties.getAuthn().getCloudDirectory();
        return new DefaultCloudDirectoryRepository(amazonCloudDirectory(), cloud);
    }

    @ConditionalOnMissingBean(name = "amazonCloudDirectory")
    @Bean
    @RefreshScope
    public AmazonCloudDirectory amazonCloudDirectory() {
        final var cloud = casProperties.getAuthn().getCloudDirectory();

        final var endpoint = new AwsClientBuilder.EndpointConfiguration(
            cloud.getEndpoint(), cloud.getRegion());
        return AmazonCloudDirectoryClientBuilder
            .standard()
            .withCredentials(ChainingAWSCredentialsProvider.getInstance(cloud.getCredentialAccessKey(),
                cloud.getCredentialSecretKey(), cloud.getCredentialsPropertiesFile(),
                cloud.getProfilePath(), cloud.getProfileName()))
            .withRegion(cloud.getRegion())
            .withEndpointConfiguration(endpoint)
            .build();

    }

    @ConditionalOnMissingBean(name = "cloudDirectoryAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer cloudDirectoryAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(cloudDirectoryAuthenticationHandler(), personDirectoryPrincipalResolver);
    }
}
