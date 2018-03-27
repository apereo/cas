package org.apereo.cas.token;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.DateTimeUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidator;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link JWTTokenTicketBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public class JWTTokenTicketBuilder implements TokenTicketBuilder {

    private final TicketValidator ticketValidator;
    private final String casSeverPrefix;
    private final CipherExecutor<String, String> tokenCipherExecutor;
    private final ExpirationPolicy expirationPolicy;
    

    @Override
    @SneakyThrows
    public String build(final String serviceTicketId, final Service service) {
        final Assertion assertion = this.ticketValidator.validate(serviceTicketId, service.getId());
        final Map<String, Object> attributes = new LinkedHashMap<>(assertion.getAttributes());
        attributes.putAll(assertion.getPrincipal().getAttributes());

        final Date validUntilDate;
        if (assertion.getValidUntilDate() != null) {
            validUntilDate = assertion.getValidUntilDate();
        } else {
            final ZonedDateTime dt = ZonedDateTime.now().plusSeconds(expirationPolicy.getTimeToLive());
            validUntilDate = DateTimeUtils.dateOf(dt);
        }
        return buildJwt(serviceTicketId, service.getId(), assertion.getAuthenticationDate(),
            assertion.getPrincipal().getName(), validUntilDate, attributes);
    }

    @Override
    @SneakyThrows
    public String build(final TicketGrantingTicket ticketGrantingTicket) {

        final Authentication authentication = ticketGrantingTicket.getAuthentication();
        final Map<String, Object> attributes = new LinkedHashMap<>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());

        final ZonedDateTime dt = ZonedDateTime.now().plusSeconds(expirationPolicy.getTimeToLive());
        final Date validUntilDate = DateTimeUtils.dateOf(dt);
        return buildJwt(ticketGrantingTicket.getId(), casSeverPrefix,
            DateTimeUtils.dateOf(ticketGrantingTicket.getCreationTime()),
            authentication.getPrincipal().getId(),
            validUntilDate, attributes);

    }

    private String buildJwt(final String jwtId, final String audience,
                            final Date issueDate, final String subject,
                            final Date validUntilDate, final Map<String, Object> attributes) {
        final JWTClaimsSet.Builder claims =
            new JWTClaimsSet.Builder()
                .audience(audience)
                .issuer(casSeverPrefix)
                .jwtID(jwtId)
                .issueTime(issueDate)
                .subject(subject);

        attributes.forEach(claims::claim);
        claims.expirationTime(validUntilDate);

        final JWTClaimsSet claimsSet = claims.build();
        final JSONObject object = claimsSet.toJSONObject();

        final String jwtJson = object.toJSONString();
        LOGGER.debug("Generated JWT [{}]", JsonValue.readJSON(jwtJson).toString(Stringify.FORMATTED));
        if (tokenCipherExecutor.isEnabled()) {
            return tokenCipherExecutor.encode(jwtJson);
        }
        final String token = new PlainJWT(claimsSet).serialize();
        return token;
    }

}
