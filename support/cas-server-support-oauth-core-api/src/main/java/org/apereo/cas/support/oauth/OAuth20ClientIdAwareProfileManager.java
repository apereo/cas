package org.apereo.cas.support.oauth;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link OAuth20ClientIdAwareProfileManager}.
 * It saves returns a profile based on client_id from the request.
 *
 * @author Kirill Gagarski
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OAuth20ClientIdAwareProfileManager<U extends CommonProfile> extends ProfileManager<U> {

    private static final String SESSION_CLIENT_ID = "oauthClientId";

    private final ServicesManager servicesManager;

    public OAuth20ClientIdAwareProfileManager(final WebContext context, final SessionStore sessionStore, final ServicesManager servicesManager) {
        super(context, sessionStore);
        this.servicesManager = servicesManager;
    }

    @Override
    protected LinkedHashMap<String, U> retrieveAll(final boolean readFromSession) {
        val profiles = super.retrieveAll(readFromSession).entrySet();
        val clientId = getClientIdFromRequest();
        val results = profiles
            .stream()
            .filter(it -> it.getValue().getAuthenticationAttribute(SESSION_CLIENT_ID).equals(clientId))
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
    public void save(final boolean saveInSession, final U profile, final boolean multiProfile) {
        val clientId = getClientIdFromRequest();
        profile.addAuthenticationAttribute(SESSION_CLIENT_ID, clientId);
        super.save(saveInSession, profile, multiProfile);
    }

    private String getClientIdFromRequest() {
        var clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isBlank(clientId)) {
            val redirectUri = context.getRequestParameter(OAuth20Constants.REDIRECT_URI)
                .map(String::valueOf).orElse(StringUtils.EMPTY);
            val svc = OAuth20Utils.getRegisteredOAuthServiceByRedirectUri(this.servicesManager, redirectUri);
            clientId = svc != null ? svc.getClientId() : StringUtils.EMPTY;
        }
        return clientId;
    }
}
