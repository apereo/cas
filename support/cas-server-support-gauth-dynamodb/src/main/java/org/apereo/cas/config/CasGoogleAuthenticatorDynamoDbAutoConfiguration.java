package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.gauth.credential.DynamoDbGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator;
import org.apereo.cas.gauth.token.GoogleAuthenticatorDynamoDbTokenRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorDynamoDbTokenRepositoryFacilitator;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * This is {@link CasGoogleAuthenticatorDynamoDbAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.GoogleAuthenticator, module = "dynamodb")
@AutoConfiguration
public class CasGoogleAuthenticatorDynamoDbAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "dynamoDbGoogleAuthenticatorAccountRegistry")
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
        @Qualifier(CasGoogleAuthenticator.BEAN_NAME)
        final CasGoogleAuthenticator googleAuthenticatorInstance,
        @Qualifier("googleAuthenticatorAccountCipherExecutor")
        final CipherExecutor googleAuthenticatorAccountCipherExecutor,
        @Qualifier("googleAuthenticatorScratchCodesCipherExecutor")
        final CipherExecutor googleAuthenticatorScratchCodesCipherExecutor,
        @Qualifier("googleAuthenticatorTokenCredentialRepositoryFacilitator")
        final DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator googleAuthenticatorTokenCredentialRepositoryFacilitator) {
        return new DynamoDbGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance, googleAuthenticatorAccountCipherExecutor,
            googleAuthenticatorScratchCodesCipherExecutor, googleAuthenticatorTokenCredentialRepositoryFacilitator);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository(
        final CasConfigurationProperties casProperties,
        @Qualifier("googleAuthenticatorDynamoDbTokenRepositoryFacilitator")
        final GoogleAuthenticatorDynamoDbTokenRepositoryFacilitator googleAuthenticatorDynamoDbTokenRepositoryFacilitator) {
        return new GoogleAuthenticatorDynamoDbTokenRepository(googleAuthenticatorDynamoDbTokenRepositoryFacilitator,
            casProperties.getAuthn().getMfa().getGauth().getCore().getTimeStepSize());
    }

    @Bean
    @ConditionalOnMissingBean(name = "googleAuthenticatorDynamoDbTokenRepositoryFacilitator")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public GoogleAuthenticatorDynamoDbTokenRepositoryFacilitator googleAuthenticatorDynamoDbTokenRepositoryFacilitator(
        final CasConfigurationProperties casProperties,
        @Qualifier("amazonDynamoDbGoogleAuthenticatorClient")
        final DynamoDbClient amazonDynamoDbGoogleAuthenticatorClient) {
        val dynamoDbProperties = casProperties.getAuthn().getMfa().getGauth().getDynamoDb();
        val facilitator = new GoogleAuthenticatorDynamoDbTokenRepositoryFacilitator(
            dynamoDbProperties, amazonDynamoDbGoogleAuthenticatorClient);
        if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
            facilitator.createTable(dynamoDbProperties.isDropTablesOnStartup());
        }
        return facilitator;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "googleAuthenticatorTokenCredentialRepositoryFacilitator")
    public DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator googleAuthenticatorTokenCredentialRepositoryFacilitator(
        final CasConfigurationProperties casProperties,
        @Qualifier("amazonDynamoDbGoogleAuthenticatorClient")
        final DynamoDbClient amazonDynamoDbGoogleAuthenticatorClient) {
        val dynamoDbProperties = casProperties.getAuthn().getMfa().getGauth().getDynamoDb();
        val facilitator = new DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator(
            dynamoDbProperties, amazonDynamoDbGoogleAuthenticatorClient);
        if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
            facilitator.createTable(dynamoDbProperties.isDropTablesOnStartup());
        }
        return facilitator;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "amazonDynamoDbGoogleAuthenticatorClient")
    public DynamoDbClient amazonDynamoDbGoogleAuthenticatorClient(final CasConfigurationProperties casProperties) {
        val dynamoDbProperties = casProperties.getAuthn().getMfa().getGauth().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(dynamoDbProperties);
    }
}
