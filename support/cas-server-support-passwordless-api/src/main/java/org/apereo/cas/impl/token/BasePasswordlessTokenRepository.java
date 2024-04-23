package org.apereo.cas.impl.token;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.gen.DefaultRandomNumberGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;

/**
 * This is {@link BasePasswordlessTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class BasePasswordlessTokenRepository implements PasswordlessTokenRepository {
    private static final int TOKEN_LENGTH = 6;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final RandomStringGenerator tokenGenerator = new DefaultRandomNumberGenerator(TOKEN_LENGTH);

    private final long tokenExpirationInSeconds;

    private final CipherExecutor<Serializable, String> cipherExecutor;

    @Override
    public PasswordlessAuthenticationToken createToken(final PasswordlessUserAccount passwordlessAccount,
                                                       final PasswordlessAuthenticationRequest passwordlessRequest) {
        val properties = new HashMap<>(passwordlessRequest.getProperties());
        properties.put("passwordlessRequestUsername", passwordlessRequest.getUsername());
        return PasswordlessAuthenticationToken.builder()
            .token(tokenGenerator.getNewString())
            .username(passwordlessAccount.getUsername())
            .expirationDate(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(getTokenExpirationInSeconds()))
            .properties(properties)
            .build();
    }

    @Override
    public String encodeToken(final PasswordlessAuthenticationToken token) {
        return FunctionUtils.doUnchecked(() -> getCipherExecutor().encode(MAPPER.writeValueAsString(token)));
    }

    protected PasswordlessAuthenticationToken decodePasswordlessAuthenticationToken(final String token) {
        return FunctionUtils.doUnchecked(() -> {
            val decoded = getCipherExecutor().decode(token);
            return MAPPER.readValue(decoded, PasswordlessAuthenticationToken.class);
        });
    }
}
