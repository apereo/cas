package org.apereo.cas.ticket.idtoken;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.profile.UserProfile;

/**
 * This is {@link IdTokenGenerationContext}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ToString
@Getter
@SuperBuilder
@Jacksonized
@With
@AllArgsConstructor
public class IdTokenGenerationContext {
    @NonNull
    private final OAuth20AccessToken accessToken;
    @Nullable
    private final OAuth20RefreshToken refreshToken;
    @Nullable
    private final UserProfile userProfile;
    @NonNull
    @Builder.Default
    private final OAuth20ResponseTypes responseType = OAuth20ResponseTypes.NONE;
    @NonNull
    @Builder.Default
    private final OAuth20GrantTypes grantType = OAuth20GrantTypes.NONE;
    @NonNull
    private final OAuthRegisteredService registeredService;
}
