package org.apereo.cas.acct.provision;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationResponse;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is {@link RestfulAccountRegistrationProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class RestfulAccountRegistrationProvisioner implements AccountRegistrationProvisioner {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true)
        .build()
        .toObjectMapper();

    @Override
    public AccountRegistrationResponse provision(final AccountRegistrationRequest request) {
        return null;
    }
}
