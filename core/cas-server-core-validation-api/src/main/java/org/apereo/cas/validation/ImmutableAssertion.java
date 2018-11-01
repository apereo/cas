package org.apereo.cas.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * An immutable, serializable ticket validation assertion.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode
@Getter
public class ImmutableAssertion implements Assertion, Serializable {

    /**
     * Unique Id for Serialization.
     */
    private static final long serialVersionUID = -3348826049921010423L;

    /**
     * Primary authentication.
     */
    private final @NonNull Authentication primaryAuthentication;

    /**
     * Chained authentications.
     */
    private final @NonNull List<Authentication> chainedAuthentications;

    /**
     * Was this the result of a new login.
     */
    private final boolean fromNewLogin;

    /**
     * The service we are asserting this ticket for.
     */
    private final @NonNull Service service;
}
