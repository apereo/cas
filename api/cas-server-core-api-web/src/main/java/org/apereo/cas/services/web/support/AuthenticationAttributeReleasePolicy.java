package org.apereo.cas.services.web.support;

import org.apereo.cas.authentication.Authentication;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * This component is used to handle release of protocol attributes in validation responses.
 *
 * @since 5.2.0
 */
public interface AuthenticationAttributeReleasePolicy {
    Map<String, Object> getAuthenticationAttributesForRelease(@Nonnull Authentication authentication);
}
