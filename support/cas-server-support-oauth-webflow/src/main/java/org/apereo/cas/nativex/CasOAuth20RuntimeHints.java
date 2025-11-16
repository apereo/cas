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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link CasOAuth20RuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasOAuth20RuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerSerializationHints(hints,
            OAuthRegisteredService.class,
            BaseOAuth20Token.class,
            OAuth20DefaultAccessToken.class,
            OAuth20DefaultCode.class,
            OAuth20DefaultRefreshToken.class,
            OAuth20DefaultDeviceToken.class,
            OAuth20DefaultDeviceUserCode.class);

        registerReflectionHints(hints, List.of(
            OAuthRegisteredService.class,
            OAuth20RegisteredServiceCipherExecutor.class,
            OAuth20JwtAccessTokenCipherExecutor.class,
            OAuth20RegisteredServiceJwtAccessTokenCipherExecutor.class,
            OAuth20DistributedSessionCookieCipherExecutor.class
        ));
    }
}
