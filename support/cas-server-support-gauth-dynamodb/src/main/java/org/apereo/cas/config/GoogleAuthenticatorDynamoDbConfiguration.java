package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.gauth.credential.DynamoDbGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator;
import org.apereo.cas.gauth.token.GoogleAuthenticatorDynamoDbTokenRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorDynamoDbTokenRepositoryFacilitator;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * This is {@link GoogleAuthenticatorDynamoDbConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "googleAuthenticatorDynamoDbConfiguration", proxyBeanMethods = false)
public class GoogleAuthenticatorDynamoDbConfiguration {

    @Autowired
    @Bean
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
        @Qualifier("googleAuthenticatorInstance")
        final IGoogleAuthenticator googleAuthenticatorInstance,
        @Qualifier("googleAuthenticatorAccountCipherExecutor")
        final CipherExecutor googleAuthenticatorAccountCipherExecutor,
        @Qualifier("googleAuthenticatorTokenCredentialRepositoryFacilitator")
        final DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator googleAuthenticatorTokenCredentialRepositoryFacilitator) {
        return new DynamoDbGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance, googleAuthenticatorAccountCipherExecutor,
            googleAuthenticatorTokenCredentialRepositoryFacilitator);
    }

    @Bean
    @Autowired
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository(
        final CasConfigurationProperties casProperties,
        @Qualifier("googleAuthenticatorDynamoDbTokenRepositoryFacilitator")
        final GoogleAuthenticatorDynamoDbTokenRepositoryFacilitator googleAuthenticatorDynamoDbTokenRepositoryFacilitator) {
        return new GoogleAuthenticatorDynamoDbTokenRepository(googleAuthenticatorDynamoDbTokenRepositoryFacilitator, casProperties.getAuthn()
            .getMfa()
            .getGauth()
            .getCore()
            .getTimeStepSize());
    }

    @Bean
    @ConditionalOnMissingBean(name = "googleAuthenticatorDynamoDbTokenRepositoryFacilitator")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public GoogleAuthenticatorDynamoDbTokenRepositoryFacilitator googleAuthenticatorDynamoDbTokenRepositoryFacilitator(
        final CasConfigurationProperties casProperties,
        @Qualifier("amazonDynamoDbGoogleAuthenticatorClient")
        final DynamoDbClient amazonDynamoDbGoogleAuthenticatorClient) {
        val dynamoDbProperties = casProperties.getAuthn()
            .getMfa()
            .getGauth()
            .getDynamoDb();
        val facilitator = new GoogleAuthenticatorDynamoDbTokenRepositoryFacilitator(dynamoDbProperties, amazonDynamoDbGoogleAuthenticatorClient);
        if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
            facilitator.createTable(dynamoDbProperties.isDropTablesOnStartup());
        }
        return facilitator;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "googleAuthenticatorTokenCredentialRepositoryFacilitator")
    @Autowired
    public DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator googleAuthenticatorTokenCredentialRepositoryFacilitator(
        final CasConfigurationProperties casProperties,
        @Qualifier("amazonDynamoDbGoogleAuthenticatorClient")
        final DynamoDbClient amazonDynamoDbGoogleAuthenticatorClient) {
        val dynamoDbProperties = casProperties.getAuthn()
            .getMfa()
            .getGauth()
            .getDynamoDb();
        val facilitator = new DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator(dynamoDbProperties, amazonDynamoDbGoogleAuthenticatorClient);
        if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
            facilitator.createTable(dynamoDbProperties.isDropTablesOnStartup());
        }
        return facilitator;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "amazonDynamoDbGoogleAuthenticatorClient")
    @Autowired
    public DynamoDbClient amazonDynamoDbGoogleAuthenticatorClient(final CasConfigurationProperties casProperties) {
        val dynamoDbProperties = casProperties.getAuthn()
            .getMfa()
            .getGauth()
            .getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(dynamoDbProperties);
    }
}
