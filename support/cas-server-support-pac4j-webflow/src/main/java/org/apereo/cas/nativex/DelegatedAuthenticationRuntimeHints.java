package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.oauth.profile.OAuth20Profile;
import org.pac4j.oidc.profile.OidcProfile;
import org.pac4j.saml.profile.SAML2Profile;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link DelegatedAuthenticationRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class DelegatedAuthenticationRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.serialization()
            .registerType(OidcProfile.class)
            .registerType(SAML2Profile.class)
            .registerType(OAuth20Profile.class)
            .registerType(CasProfile.class);
    }
}
