package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * An immutable, serializable ticket validation assertion.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public record ImmutableAssertion(Authentication primaryAuthentication, Authentication originalAuthentication,
                                 List<Authentication> chainedAuthentications, boolean fromNewLogin,
                                 WebApplicationService service, RegisteredService registeredService,
                                 Map<String, Serializable> context) implements Assertion {
    @Serial
    private static final long serialVersionUID = -3348826049921010423L;

}
