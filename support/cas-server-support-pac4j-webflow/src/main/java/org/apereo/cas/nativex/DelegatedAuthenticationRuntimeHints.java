package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.web.DelegatedClientAuthenticationDistributedSessionCookieCipherExecutor;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.oauth.profile.OAuth20Profile;
import org.pac4j.oidc.profile.OidcProfile;
import org.pac4j.saml.profile.SAML2Profile;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

import java.util.List;

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

        List.of(
                DelegatedClientAuthenticationDistributedSessionCookieCipherExecutor.class.getName()
            )
            .forEach(el -> hints.reflection().registerTypeIfPresent(classLoader, el,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.PUBLIC_FIELDS));
    }
}
