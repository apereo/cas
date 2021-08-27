package org.apereo.cas.gauth.credential;

import org.apereo.cas.configuration.model.support.mfa.gauth.DynamoDbGoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * This is {@link DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final DynamoDbGoogleAuthenticatorMultifactorProperties dynamoDbProperties;

    private final DynamoDbClient amazonDynamoDBClient;

    
}
