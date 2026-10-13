package org.apereo.cas.support.oauth;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

/**
 * This is {@link OAuth20ClientIdAwareProfileManager}.
 * It saves returns a profile based on client_id from the request.
 *
 * @author Kirill Gagarski
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OAuth20ClientIdAwareProfileManager extends ProfileManager {

    private static final String SESSION_CLIENT_ID = "oauthClientId";

    private static final String REQUEST_ATTRIBUTE_CLIENT_ID = OAuth20ClientIdAwareProfileManager.class.getName() + ".clientId";

    private final ServicesManager servicesManager;

    private final OAuth20RequestParameterResolver requestParameterResolver;

    private final CallContext callContext;

    public OAuth20ClientIdAwareProfileManager(final WebContext context,
                                              final SessionStore sessionStore,
                                              final ServicesManager servicesManager,
                                              final OAuth20RequestParameterResolver requestParameterResolver) {
        super(context, sessionStore);
        this.servicesManager = servicesManager;
        this.requestParameterResolver = requestParameterResolver;
        this.callContext = new CallContext(context, sessionStore);
    }

    @Override
    @SuppressWarnings("NonApiType")
    protected LinkedHashMap<String, UserProfile> retrieveAll(final boolean readFromSession) {
        val profiles = super.retrieveAll(readFromSession).entrySet();
        val clientId = getClientIdFromRequest();
        val results = profiles
            .stream()
            .filter(it -> {
                val profile = it.getValue();
                return StringUtils.isBlank(clientId)
                       || Strings.CI.equals((CharSequence) profile.getAttribute(SESSION_CLIENT_ID), clientId);
            })
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (v1, v2) -> {
                    throw new IllegalStateException("Duplicate key");
                },
                LinkedHashMap::new));
        LOGGER.trace("Fetched profiles for this session are [{}]", results);
        return results;
    }

    @Override
    public void save(final boolean saveInSession, final UserProfile profile, final boolean multiProfile) {
        val clientId = getClientIdFromRequest();
        if (StringUtils.isNotBlank(clientId)) {
            profile.addAttribute(SESSION_CLIENT_ID, clientId);
        }
        super.save(saveInSession, profile, multiProfile);
    }

    /*
     * A client that authenticates with HTTP Basic, as a confidential client usually does at the token endpoint,
     * sends no client_id parameter; its credentials name it, as does the assertion of a client that authenticates with
     * private_key_jwt or client_secret_jwt. Only otherwise is the client looked up by matching the redirect URI against
     * every OAuth application.
     */
    private String getClientIdFromRequest() {
        val clientId = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.CLIENT_ID)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isNotBlank(clientId)) {
            return clientId;
        }
        val redirectUri = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.REDIRECT_URI)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        OAuth20Utils.validateRedirectUri(redirectUri, true);
        val authenticatedClientId = requestParameterResolver.resolveClientIdAndClientSecret(callContext).getLeft();
        if (StringUtils.isNotBlank(authenticatedClientId)) {
            return authenticatedClientId;
        }
        return getClientIdFromAssertion().orElseGet(() -> getClientIdByRedirectUri(redirectUri));
    }

    /*
     * RFC 7523 (section 3) and OpenID Connect Core (section 9): iss and sub of a client assertion are both the client_id,
     * which is why the client_id parameter may be left out (RFC 7521, section 4.2). The assertion is only read here to
     * name the client; the authenticator verifies it.
     */
    private Optional<String> getClientIdFromAssertion() {
        val assertionType = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.CLIENT_ASSERTION_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val assertion = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.CLIENT_ASSERTION)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (!OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER.equals(assertionType) || StringUtils.isBlank(assertion)) {
            return Optional.empty();
        }
        try {
            val claims = JWTParser.parse(assertion).getJWTClaimsSet();
            return Optional.ofNullable(claims)
                .map(JWTClaimsSet::getSubject)
                .filter(StringUtils::isNotBlank)
                .filter(subject -> subject.equals(claims.getIssuer()));
        } catch (final Exception e) {
            LOGGER.debug("Unable to read the client assertion to determine the client: [{}]", e.getMessage());
            return Optional.empty();
        }
    }

    /*
     * Profiles are read several times per request, so the lookup is kept for the request, keyed by the redirect URI.
     */
    private String getClientIdByRedirectUri(final String redirectUri) {
        val attributeName = REQUEST_ATTRIBUTE_CLIENT_ID + ':' + redirectUri;
        val resolvedClientId = context.getRequestAttribute(attributeName);
        if (resolvedClientId.isPresent()) {
            return resolvedClientId.get().toString();
        }
        val svc = OAuth20Utils.getRegisteredOAuthServiceByRedirectUri(this.servicesManager, redirectUri);
        val clientId = svc != null ? svc.getClientId() : StringUtils.EMPTY;
        context.setRequestAttribute(attributeName, clientId);
        return clientId;
    }
}
