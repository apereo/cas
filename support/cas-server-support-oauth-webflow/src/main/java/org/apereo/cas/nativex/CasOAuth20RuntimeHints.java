package org.apereo.cas.nativex;

import org.apereo.cas.support.oauth.services.OAuth20RegisteredServiceCipherExecutor;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.OAuth20DistributedSessionCookieCipherExecutor;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20RegisteredServiceJwtAccessTokenCipherExecutor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

import java.util.List;

/**
 * This is {@link CasOAuth20RuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasOAuth20RuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        List.of(
            OAuthRegisteredService.class,
            OAuth20RegisteredServiceCipherExecutor.class,
            OAuth20RegisteredServiceJwtAccessTokenCipherExecutor.class,
            OAuth20DistributedSessionCookieCipherExecutor.class
        ).forEach(el ->
            hints.reflection().registerType(TypeReference.of(el),
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.PUBLIC_FIELDS));
    }
}
