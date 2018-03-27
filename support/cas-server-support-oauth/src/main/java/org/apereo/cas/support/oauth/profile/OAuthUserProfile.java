package org.apereo.cas.support.oauth.profile;

import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.profile.CommonProfile;

/**
 * Specific profile for OAuth user authentication.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Slf4j
public class OAuthUserProfile extends CommonProfile {

    private static final long serialVersionUID = 7183931516213061247L;
}
