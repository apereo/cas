package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import org.junit.jupiter.api.Tag;

/**
 * This is {@link SimpleSurrogateAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Tag("Impersonation")
public class SimpleSurrogateAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    private final SurrogateAuthenticationService service = new SimpleSurrogateAuthenticationService(
            CollectionUtils.wrap("casuser", CollectionUtils.wrapList("banderson")), servicesManager);
}
