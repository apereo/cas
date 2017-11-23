package org.apereo.cas.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.clouddirectory.AmazonCloudDirectory;
import com.amazonaws.services.clouddirectory.AmazonCloudDirectoryClientBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CloudDirectoryAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.clouddirectory.CloudDirectoryRepository;
import org.apereo.cas.clouddirectory.DefaultCloudDirectoryRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.clouddirectory.CloudDirectoryProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;


/**
 * This is {@link CloudDirectoryAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("cloudDirectoryAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
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
        return new DefaultPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "amazonCloudDirectory")
    @Bean
    @RefreshScope
    public AuthenticationHandler cloudDirectoryAuthenticationHandler() {

        final CloudDirectoryProperties cloud = casProperties.getAuthn().getCloudDirectory();

        final CloudDirectoryAuthenticationHandler handler = new CloudDirectoryAuthenticationHandler(cloud.getName(), servicesManager,
                cloudDirectoryPrincipalFactory(), cloudDirectoryRepository(), cloud);
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(cloud.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(cloud.getPasswordEncoder()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "cloudDirectoryRepository")
    @Bean
    @RefreshScope
    public CloudDirectoryRepository cloudDirectoryRepository() {
        final CloudDirectoryProperties cloud = casProperties.getAuthn().getCloudDirectory();
        return new DefaultCloudDirectoryRepository(amazonCloudDirectory(), cloud);
    }

    @ConditionalOnMissingBean(name = "amazonCloudDirectory")
    @Bean
    @RefreshScope
    public AmazonCloudDirectory amazonCloudDirectory() {
        final AWSCredentialsProvider provider;
        final CloudDirectoryProperties cloud = casProperties.getAuthn().getCloudDirectory();

        if (cloud.getCredentialsPropertiesFile() != null) {
            try {
                final File file = cloud.getCredentialsPropertiesFile().getFile();
                provider = new PropertiesFileCredentialsProvider(file.getCanonicalPath());
            } catch (final Exception e) {
                throw new BeanCreationException(e.getMessage(), e);
            }
        } else if (StringUtils.isNotBlank(cloud.getCredentialAccessKey())
                && StringUtils.isNotBlank(cloud.getCredentialSecretKey())) {
            provider = new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(cloud.getCredentialAccessKey(), cloud.getCredentialSecretKey()));
        } else if (StringUtils.isNotBlank(cloud.getProfilePath())
                && StringUtils.isNotBlank(cloud.getProfileName())) {
            provider = new ProfileCredentialsProvider(cloud.getProfilePath(), cloud.getProfileName());
        } else {
            provider = new SystemPropertiesCredentialsProvider();
        }

        return AmazonCloudDirectoryClientBuilder.standard()
                .withCredentials(provider)
                .withRegion(cloud.getRegion())
                .build();

    }

    @ConditionalOnMissingBean(name = "cloudDirectoryAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer cloudDirectoryAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(cloudDirectoryAuthenticationHandler(), personDirectoryPrincipalResolver);
    }
}
