package org.apereo.cas.support.oauth.web.response.introspection;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * This is {@link OAuth20DefaultIntrospectionResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class OAuth20DefaultIntrospectionResponseGenerator implements OAuth20IntrospectionResponseGenerator {
    @Override
    public OAuth20IntrospectionAccessTokenResponse generate(final String accessTokenId, final OAuth20Token accessToken) {
        var introspect = new OAuth20IntrospectionAccessTokenResponse();
        introspect.setScope("CAS");
        introspect.setActive(accessToken != null && !accessToken.isExpired());
        if (introspect.isActive()) {
            return collectIntrospectionDetails(introspect, accessToken).setToken(accessTokenId);
        }
        return introspect;
    }

    protected OAuth20IntrospectionAccessTokenResponse collectIntrospectionDetails(final OAuth20IntrospectionAccessTokenResponse introspect,
                                                                                  final OAuth20Token accessToken) {
        introspect.setClientId(accessToken.getClientId());
        if (accessToken instanceof final ServiceAwareTicket sat) {
            introspect.setAud(sat.getService().getId());
        }

        val authentication = accessToken.getAuthentication();
        val attributes = new LinkedHashMap<String, Object>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());

        val subject = authentication.getPrincipal().getId();
        introspect.setSub(subject);
        introspect.setUniqueSecurityName(subject);
        introspect.setIat(accessToken.getCreationTime().toInstant().getEpochSecond());
        introspect.setExp(introspect.getIat() + accessToken.getExpirationPolicy().getTimeToLive());

        val methods = attributes.get(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE);
        val realmNames = CollectionUtils.toCollection(methods)
            .stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));

        introspect.setRealmName(realmNames);
        val tokenType = attributes.containsKey(OAuth20Constants.DPOP_CONFIRMATION)
            ? OAuth20Constants.TOKEN_TYPE_DPOP
            : OAuth20Constants.TOKEN_TYPE_BEARER;
        introspect.setTokenType(tokenType);

        CollectionUtils.firstElement(attributes.get(OAuth20Constants.GRANT_TYPE))
            .ifPresent(type -> introspect.setGrantType(type.toString()));
        CollectionUtils.firstElement(attributes.get(OAuth20Constants.X509_CERTIFICATE_DIGEST))
            .ifPresent(digest -> introspect.getConfirmation().setX5t(digest.toString()));
        return introspect;
    }
}
