package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;

import lombok.NonNull;

import java.io.Serial;
import java.util.List;

/**
 * An immutable, serializable ticket validation assertion.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public record ImmutableAssertion(@NonNull Authentication primaryAuthentication, @NonNull List<Authentication> chainedAuthentications, boolean fromNewLogin,
                                 @NonNull WebApplicationService service, @NonNull RegisteredService registeredService) implements Assertion {

    @Serial
    private static final long serialVersionUID = -3348826049921010423L;

}
