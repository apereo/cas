package org.apereo.cas.nativex;

import org.apereo.cas.support.oauth.services.OAuth20RegisteredServiceCipherExecutor;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.OAuth20DistributedSessionCookieCipherExecutor;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenCipherExecutor;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20RegisteredServiceJwtAccessTokenCipherExecutor;
import org.apereo.cas.ticket.BaseOAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessToken;
import org.apereo.cas.ticket.code.OAuth20DefaultCode;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceToken;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceUserCode;
import org.apereo.cas.ticket.refreshtoken.OAuth20DefaultRefreshToken;
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
        registerSerializationHints(hints, OAuthRegisteredService.class,
            BaseOAuth20Token.class,
            OAuth20DefaultAccessToken.class,
            OAuth20DefaultCode.class,
            OAuth20DefaultRefreshToken.class,
            OAuth20DefaultDeviceToken.class,
            OAuth20DefaultDeviceUserCode.class);

        List.of(
            OAuthRegisteredService.class,
            OAuth20RegisteredServiceCipherExecutor.class,
            OAuth20JwtAccessTokenCipherExecutor.class,
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
