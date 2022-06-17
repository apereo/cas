package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link BaseAccessTokenGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class BaseAccessTokenGrantRequestExtractor implements AccessTokenGrantRequestExtractor {
    private final OAuth20ConfigurationContext configurationContext;
}
