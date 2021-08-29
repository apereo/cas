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
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * This is {@link GoogleAuthenticatorDynamoDbConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("googleAuthenticatorDynamoDbConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class GoogleAuthenticatorDynamoDbConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Bean
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(@Qualifier("googleAuthenticatorInstance")
                                                                               final IGoogleAuthenticator googleAuthenticatorInstance,
                                                                               @Qualifier("googleAuthenticatorAccountCipherExecutor")
                                                                               final CipherExecutor googleAuthenticatorAccountCipherExecutor) {
        return new DynamoDbGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance,
            googleAuthenticatorAccountCipherExecutor,
            googleAuthenticatorTokenCredentialRepositoryFacilitator());
    }

    @Bean
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository() {
        return new GoogleAuthenticatorDynamoDbTokenRepository(googleAuthenticatorDynamoDbTokenRepositoryFacilitator(),
            casProperties.getAuthn().getMfa().getGauth().getCore().getTimeStepSize());
    }

    @Bean
    @ConditionalOnMissingBean(name = "googleAuthenticatorDynamoDbTokenRepositoryFacilitator")
    @RefreshScope
    public GoogleAuthenticatorDynamoDbTokenRepositoryFacilitator googleAuthenticatorDynamoDbTokenRepositoryFacilitator() {
        val dynamoDbProperties = casProperties.getAuthn().getMfa().getGauth().getDynamoDb();
        val facilitator = new GoogleAuthenticatorDynamoDbTokenRepositoryFacilitator(
            dynamoDbProperties, amazonDynamoDbGoogleAuthenticatorClient());
        if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
            facilitator.createTable(dynamoDbProperties.isDropTablesOnStartup());
        }
        return facilitator;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "googleAuthenticatorTokenCredentialRepositoryFacilitator")
    public DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator googleAuthenticatorTokenCredentialRepositoryFacilitator() {
        val dynamoDbProperties = casProperties.getAuthn().getMfa().getGauth().getDynamoDb();
        val facilitator = new DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator(dynamoDbProperties, amazonDynamoDbGoogleAuthenticatorClient());
        if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
            facilitator.createTable(dynamoDbProperties.isDropTablesOnStartup());
        }
        return facilitator;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "amazonDynamoDbGoogleAuthenticatorClient")
    public DynamoDbClient amazonDynamoDbGoogleAuthenticatorClient() {
        val dynamoDbProperties = casProperties.getAuthn().getMfa().getGauth().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(dynamoDbProperties);
    }
}
