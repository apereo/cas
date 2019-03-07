package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is a profile manager used during OAuth authentication.
 * <p>
 * It saves returns a profile based on client_id from the request.
 *
 * @param <U> profile type
 * @author Kirill Gagarski
 * @since 5.3.8
 */
@Slf4j
public class ClientIdAwareProfileManager<U extends CommonProfile> extends ProfileManager<U> {

    private static final String SESSION_CLIENT_ID = "oauthClientId";

    private final ServicesManager servicesManager;

    public ClientIdAwareProfileManager(final WebContext context, final SessionStore sessionStore, final ServicesManager servicesManager) {
        super(context, sessionStore);
        this.servicesManager = servicesManager;
    }

    @Override
    protected LinkedHashMap<String, U> retrieveAll(final boolean readFromSession) {
        final Collection<Map.Entry<String, U>> profiles = super.retrieveAll(readFromSession).entrySet();
        final String clientId = getClientIdFromRequest();
        final LinkedHashMap<String, U> results = profiles
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
        final String clientId = getClientIdFromRequest();
        profile.addAuthenticationAttribute(SESSION_CLIENT_ID, clientId);
        super.save(saveInSession, profile, multiProfile);
    }

    private String getClientIdFromRequest() {
        String clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            final String redirectUri = context.getRequestParameter(OAuth20Constants.REDIRECT_URI);
            final OAuthRegisteredService svc = OAuth20Utils.getRegisteredOAuthServiceByRedirectUri(this.servicesManager, redirectUri);
            clientId = svc != null ? svc.getClientId() : StringUtils.EMPTY;
        }
        return clientId;
    }
}
