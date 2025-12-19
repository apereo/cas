package org.apereo.cas.tokens;

import module java.base;
import org.apereo.cas.rest.factory.DefaultTicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.token.TokenConstants;
import org.apereo.cas.token.TokenTicketBuilder;
import org.apereo.cas.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link JwtTicketGrantingTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTicketGrantingTicketResourceEntityResponseFactory extends DefaultTicketGrantingTicketResourceEntityResponseFactory {
    private static final List<String> IGNORED_PARAMS = List.of(
        RestHttpRequestCredentialFactory.PARAMETER_USERNAME,
        RestHttpRequestCredentialFactory.PARAMETER_PASSWORD,
        TokenConstants.PARAMETER_NAME_TOKEN);

    private final TokenTicketBuilder tokenTicketBuilder;

    @Override
    public ResponseEntity<@NonNull String> build(final Ticket ticketGrantingTicket, final HttpServletRequest request) throws Throwable {
        var tokenParam = request.getParameter(TokenConstants.PARAMETER_NAME_TOKEN);
        if (StringUtils.isBlank(tokenParam)) {
            tokenParam = request.getHeader(TokenConstants.PARAMETER_NAME_TOKEN);
        }
        if (StringUtils.isBlank(tokenParam) || !BooleanUtils.toBoolean(tokenParam)) {
            LOGGER.debug("The request indicates that ticket-granting ticket should not be created as a JWT");
            return super.build(ticketGrantingTicket, request);
        }
        val claims = (Map) request.getParameterMap()
            .entrySet()
            .stream()
            .filter(entry -> !IGNORED_PARAMS.contains(entry.getKey()))
            .map(entry -> Pair.of(entry.getKey(), CollectionUtils.toCollection(entry.getValue(), ArrayList.class)))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        val jwt = tokenTicketBuilder.build((AuthenticationAwareTicket) ticketGrantingTicket, claims);
        LOGGER.debug("Generated JWT [{}]", jwt);

        val headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        val entity = new ResponseEntity<>(jwt, headers, HttpStatus.CREATED);
        LOGGER.debug("Created response entity [{}]", entity);
        return entity;
    }
}
