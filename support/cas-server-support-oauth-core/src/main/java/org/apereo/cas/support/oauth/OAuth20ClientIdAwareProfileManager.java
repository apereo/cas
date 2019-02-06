package org.apereo.cas.support.oauth;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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
    private static final String REQUEST_CLIENT_ID = OAuth20Constants.CLIENT_ID;

    public OAuth20ClientIdAwareProfileManager(final WebContext context, final SessionStore sessionStore) {
        super(context, sessionStore);
    }

    public OAuth20ClientIdAwareProfileManager(final WebContext context) {
        super(context);
    }

    @Override
    protected LinkedHashMap<String, U> retrieveAll(final boolean readFromSession) {
        val profiles = super.retrieveAll(readFromSession).entrySet();
        return profiles.stream().filter(
            it -> it.getValue()
                .getAuthenticationAttribute(SESSION_CLIENT_ID)
                .equals(context.getRequestParameter(REQUEST_CLIENT_ID)))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (v1, v2) -> {
                    throw new IllegalStateException("Duplicate key");
                },
                LinkedHashMap::new));
    }

    @Override
    public void save(final boolean saveInSession, final U profile, final boolean multiProfile) {
        profile.addAuthenticationAttribute(SESSION_CLIENT_ID, context.getRequestParameter(REQUEST_CLIENT_ID));
        super.save(saveInSession, profile, multiProfile);
    }
}
