package org.apereo.cas.webauthn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yubico.data.CredentialRegistration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link DynamoDbWebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class DynamoDbWebAuthnCredentialRepository extends BaseWebAuthnCredentialRepository {
    private final DynamoDbWebAuthnFacilitator facilitator;

    public DynamoDbWebAuthnCredentialRepository(final CasConfigurationProperties properties,
        final CipherExecutor<String, String> cipherExecutor,
        final DynamoDbWebAuthnFacilitator facilitator) {
        super(properties, cipherExecutor);
        this.facilitator = facilitator;
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        return facilitator.getAccountsBy(username.trim().toLowerCase())
            .map(DynamoDbWebAuthnCredentialRegistration::getRecords)
            .flatMap(List::stream)
            .map(record -> getCipherExecutor().decode(record))
            .map(Unchecked.function(record -> WebAuthnUtils.getObjectMapper().readValue(record, new TypeReference<CredentialRegistration>() {
            })))
            .collect(Collectors.toList());
    }

    @Override
    public Stream<CredentialRegistration> stream() {
        return facilitator.load()
            .map(DynamoDbWebAuthnCredentialRegistration::getRecords)
            .flatMap(List::stream)
            .map(record -> getCipherExecutor().decode(record))
            .map(Unchecked.function(record -> WebAuthnUtils.getObjectMapper().readValue(record, new TypeReference<>() {
            })));
    }

    @Override
    @SneakyThrows
    protected void update(final String username, final Collection<CredentialRegistration> records) {
        if (records.isEmpty()) {
            LOGGER.debug("No records are provided for [{}] so entry will be removed", username);
            facilitator.remove(username.trim().toLowerCase());
        } else {
            val jsonRecords = records.stream()
                .map(record -> {
                    if (record.getRegistrationTime() == null) {
                        return record.withRegistrationTime(Instant.now(Clock.systemUTC()));
                    }
                    return record;
                })
                .map(Unchecked.function(record -> getCipherExecutor().encode(WebAuthnUtils.getObjectMapper().writeValueAsString(record))))
                .collect(Collectors.toList());
            val entry = DynamoDbWebAuthnCredentialRegistration.builder()
                .records(jsonRecords)
                .username(username.trim().toLowerCase())
                .build();
            facilitator.save(entry);
        }
    }
}
