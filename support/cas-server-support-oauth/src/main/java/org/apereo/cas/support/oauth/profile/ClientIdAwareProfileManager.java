package org.apereo.cas.support.oauth.profile;

import com.github.scribejava.core.model.OAuthConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is a profile manager used during OAuth authentication.
 *
 * It saves returns a profile based on client_id from the request.
 *
 * @author Kirill Gagarski
 * @param <U> profile type
 * @since 5.3.8
 */
public class ClientIdAwareProfileManager<U extends CommonProfile> extends ProfileManager<U> {

    private static final String SESSION_CLIENT_ID = "oauthClientId";
    private static final String REQUEST_CLIENT_ID = OAuthConstants.CLIENT_ID;

    public ClientIdAwareProfileManager(final WebContext context, final SessionStore sessionStore) {
        super(context, sessionStore);
    }

    public ClientIdAwareProfileManager(final WebContext context) {
        super(context);
    }

    @Override
    protected LinkedHashMap<String, U> retrieveAll(final boolean readFromSession) {
        return super.retrieveAll(readFromSession).entrySet().stream().filter(
            it -> it.getValue()
                .getAttribute(SESSION_CLIENT_ID)
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
        profile.addAttribute(SESSION_CLIENT_ID, context.getRequestParameter(REQUEST_CLIENT_ID));
        super.save(saveInSession, profile, multiProfile);
    }
}
