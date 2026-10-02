package org.apereo.cas.oidc.ticket;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.registry.compact.CompactTicketCodec;
import org.apereo.cas.ticket.registry.compact.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link OidcPushedAuthorizationRequestCompactor}.
 * Keeps what the authorization endpoint needs to resume a pushed authorization request: the service and client,
 * the redirect URI, response type and mode, grant type, scopes, PKCE challenge, authorization details, issuer state,
 * DPoP confirmation, requested claims, the request parameters (client credentials excluded), and the principal id,
 * date and attributes of the authentication built for the request. The registered service is looked up again by
 * client id, and the ticket is created again by the pushed authorization request factory. The pac4j profile of the
 * client that pushed the request is not kept.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OidcPushedAuthorizationRequestCompactor implements TicketCompactor<OidcPushedAuthorizationRequest> {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final Set<String> EXCLUDED_PARAMETERS = Set.of(OAuth20Constants.CLIENT_SECRET,
        OAuth20Constants.CLIENT_ASSERTION, OAuth20Constants.CLIENT_ASSERTION_TYPE);

    private static final int CLIENT_ID_INDEX = 3;

    private static final int REDIRECT_URI_INDEX = 4;

    private static final int RESPONSE_TYPE_INDEX = 5;

    private static final int RESPONSE_MODE_INDEX = 6;

    private static final int GRANT_TYPE_INDEX = 7;

    private static final int SCOPES_INDEX = 8;

    private static final int CODE_CHALLENGE_INDEX = 9;

    private static final int CODE_CHALLENGE_METHOD_INDEX = 10;

    private static final int AUTHORIZATION_DETAILS_INDEX = 11;

    private static final int ISSUER_STATE_INDEX = 12;

    private static final int DPOP_CONFIRMATION_INDEX = 13;

    private static final int CLAIMS_INDEX = 14;

    private static final int PARAMETERS_INDEX = 15;

    private static final int PRINCIPAL_INDEX = 16;

    private static final int AUTHENTICATION_DATE_INDEX = 17;

    private static final int AUTHENTICATION_ATTRIBUTES_INDEX = 18;

    private final ObjectProvider<TicketFactory> ticketFactory;

    private final ServiceFactory serviceFactory;

    private final PrincipalFactory principalFactory;

    private final ServicesManager servicesManager;

    @Getter
    private long maximumTicketLength = 2048;

    @Override
    public Class<OidcPushedAuthorizationRequest> getTicketType() {
        return OidcPushedAuthorizationRequest.class;
    }

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) {
        val holder = getPushedAuthorizationRequestFactory().toAccessTokenRequest((OidcPushedAuthorizationRequest) ticket);
        fields.add(holder.getService().getId());
        fields.add(StringUtils.defaultString(holder.getClientId()));
        fields.add(StringUtils.defaultString(holder.getRedirectUri()));
        fields.add(Objects.requireNonNullElse(holder.getResponseType(), OAuth20ResponseTypes.NONE).name());
        fields.add(Objects.requireNonNullElse(holder.getResponseMode(), OAuth20ResponseModeTypes.NONE).name());
        fields.add(Objects.requireNonNullElse(holder.getGrantType(), OAuth20GrantTypes.NONE).name());
        fields.add(CompactTicketCodec.encodeValues(holder.getScopes()));
        fields.add(StringUtils.defaultString(holder.getCodeChallenge()));
        fields.add(StringUtils.defaultString(holder.getCodeChallengeMethod()));
        fields.add(StringUtils.defaultString(holder.getAuthorizationDetails()));
        fields.add(StringUtils.defaultString(holder.getIssuerState()));
        fields.add(StringUtils.defaultString(holder.getDpopConfirmation()));
        fields.add(holder.getClaims() == null || holder.getClaims().isEmpty() ? StringUtils.EMPTY : MAPPER.writeValueAsString(holder.getClaims()));
        val parameters = new ArrayList<String>();
        holder.getParameters().forEach((name, value) -> {
            if (!EXCLUDED_PARAMETERS.contains(name) && value != null) {
                parameters.add(name);
                parameters.add(value.toString());
            }
        });
        fields.add(CompactTicketCodec.encodeValues(parameters));
        val authentication = holder.getAuthentication();
        fields.add(authentication.getPrincipal().getId());
        fields.add(String.valueOf(authentication.getAuthenticationDate().toEpochSecond()));
        val attributes = new ArrayList<String>();
        authentication.getAttributes().forEach((name, values) -> {
            if (values != null && !values.isEmpty()) {
                attributes.add(name);
                attributes.add(CompactTicketCodec.encodeValues(values.stream().map(String::valueOf).toList()));
            }
        });
        fields.add(CompactTicketCodec.encodeValues(attributes));
    }

    @Override
    public Ticket expand(final String compactTicket) throws Throwable {
        val structure = parse(compactTicket, AUTHENTICATION_ATTRIBUTES_INDEX + 1);
        val clientId = structure.get(CLIENT_ID_INDEX);
        val registeredService = Objects.requireNonNull(OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId),
            () -> "Unable to locate registered service for client id " + clientId);
        val holder = AccessTokenRequestContext.builder()
            .service(buildService(structure.get(CompactTicketIndexes.SERVICE), registeredService))
            .authentication(buildAuthentication(structure))
            .registeredService(registeredService)
            .clientId(clientId)
            .redirectUri(structure.get(REDIRECT_URI_INDEX))
            .responseType(OAuth20ResponseTypes.valueOf(structure.get(RESPONSE_TYPE_INDEX)))
            .responseMode(OAuth20ResponseModeTypes.valueOf(structure.get(RESPONSE_MODE_INDEX)))
            .grantType(OAuth20GrantTypes.valueOf(structure.get(GRANT_TYPE_INDEX)))
            .scopes(new LinkedHashSet<>(CompactTicketCodec.decodeValues(structure.get(SCOPES_INDEX))))
            .codeChallenge(structure.get(CODE_CHALLENGE_INDEX))
            .codeChallengeMethod(structure.get(CODE_CHALLENGE_METHOD_INDEX))
            .authorizationDetails(StringUtils.trimToNull(structure.get(AUTHORIZATION_DETAILS_INDEX)))
            .issuerState(StringUtils.trimToNull(structure.get(ISSUER_STATE_INDEX)))
            .claims(readClaims(structure.get(CLAIMS_INDEX)))
            .build();
        holder.setDpopConfirmation(StringUtils.trimToNull(structure.get(DPOP_CONFIRMATION_INDEX)));
        val parameters = CompactTicketCodec.decodeValues(structure.get(PARAMETERS_INDEX));
        if (parameters.size() % 2 != 0) {
            throw new IllegalArgumentException("Invalid pushed authorization request parameters");
        }
        for (var index = 0; index < parameters.size(); index += 2) {
            holder.getParameters().put(parameters.get(index), parameters.get(index + 1));
        }
        val request = getPushedAuthorizationRequestFactory().create(holder);
        request.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        request.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        return request;
    }

    private OidcPushedAuthorizationRequestFactory getPushedAuthorizationRequestFactory() {
        return (OidcPushedAuthorizationRequestFactory) ticketFactory.getObject().get(getTicketType());
    }

    private Service buildService(final String serviceId, final OAuthRegisteredService registeredService) {
        val service = Objects.requireNonNull(serviceFactory.createService(serviceId));
        service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(registeredService.getClientId()));
        service.getAttributes().put(RegisteredService.class.getName(), List.of(registeredService.getId()));
        return service;
    }

    private Authentication buildAuthentication(final CompactTicket structure) throws Throwable {
        val attributes = CompactTicketCodec.decodeValues(structure.get(AUTHENTICATION_ATTRIBUTES_INDEX));
        if (attributes.size() % 2 != 0) {
            throw new IllegalArgumentException("Invalid pushed authorization request authentication attributes");
        }
        val builder = DefaultAuthenticationBuilder.newInstance()
            .setPrincipal(principalFactory.createPrincipal(structure.get(PRINCIPAL_INDEX)))
            .setAuthenticationDate(DateTimeUtils.zonedDateTimeOf(
                Instant.ofEpochSecond(Long.parseLong(structure.get(AUTHENTICATION_DATE_INDEX)))));
        for (var index = 0; index < attributes.size(); index += 2) {
            builder.addAttribute(attributes.get(index), new ArrayList<Object>(CompactTicketCodec.decodeValues(attributes.get(index + 1))));
        }
        return builder.build();
    }

    private static Map<String, Map<String, Object>> readClaims(final String claims) {
        return StringUtils.isBlank(claims)
            ? new HashMap<>()
            : MAPPER.readValue(claims, new TypeReference<HashMap<String, Map<String, Object>>>() {
            });
    }
}
