package org.apereo.cas.token;

import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.DateTimeUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

/**
 * This is {@link JWTTokenTicketBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JWTTokenTicketBuilder implements TokenTicketBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWTTokenTicketBuilder.class);

    private final AbstractUrlBasedTicketValidator ticketValidator;
    private final String casSeverPrefix;
    private final CipherExecutor<String, String> tokenCipherExecutor;
    private final long expirationTimeSeconds;

    public JWTTokenTicketBuilder(final AbstractUrlBasedTicketValidator ticketValidator,
                                 final String casSeverPrefix,
                                 final CipherExecutor<String, String> tokenCipherExecutor,
                                 final long expirationTimeSeconds) {
        this.ticketValidator = ticketValidator;
        this.casSeverPrefix = casSeverPrefix;
        this.tokenCipherExecutor = tokenCipherExecutor;
        this.expirationTimeSeconds = expirationTimeSeconds;
    }

    @Override
    public String build(final String serviceTicketId, final Service service) {
        try {
            final Assertion assertion = this.ticketValidator.validate(serviceTicketId, service.getId());
            final JWTClaimsSet.Builder claims =
                    new JWTClaimsSet.Builder()
                            .audience(service.getId())
                            .issuer(casSeverPrefix)
                            .jwtID(serviceTicketId)
                            .issueTime(assertion.getAuthenticationDate())
                            .subject(assertion.getPrincipal().getName());
            assertion.getAttributes().forEach(claims::claim);
            assertion.getPrincipal().getAttributes().forEach(claims::claim);

            if (assertion.getValidUntilDate() != null) {
                claims.expirationTime(assertion.getValidUntilDate());
            } else {
                final ZonedDateTime dt = ZonedDateTime.now().plusSeconds(expirationTimeSeconds);
                claims.expirationTime(DateTimeUtils.dateOf(dt));
            }
            final JWTClaimsSet claimsSet = claims.build();
            final JSONObject object = claimsSet.toJSONObject();

            final String jwtJson = object.toJSONString();
            LOGGER.debug("Generated JWT [{}]", JsonValue.readJSON(jwtJson).toString(Stringify.FORMATTED));
            return tokenCipherExecutor.encode(jwtJson);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
