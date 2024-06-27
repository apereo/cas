package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.Service;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link LogoutRedirectionResponse}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@SuperBuilder
@ToString
public class LogoutRedirectionResponse {
    @Builder.Default
    private final Optional<Service> service = Optional.empty();
    @Builder.Default
    private final Optional<String> logoutRedirectUrl = Optional.empty();
    @Builder.Default
    private final Optional<String> logoutPostUrl = Optional.empty();
    @Builder.Default
    private final Map<String, Object> logoutPostData = new HashMap<>();
}
