package org.apereo.cas.validation;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

/**
 * An immutable, serializable ticket validation assertion.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@ToString
@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@EqualsAndHashCode
@Getter
@NoArgsConstructor(force = true)
public class ImmutableAssertion implements Assertion {

    @Serial
    private static final long serialVersionUID = -3348826049921010423L;

    /**
     * Primary authentication.
     */
    @JsonProperty
    private final Authentication primaryAuthentication;

    @JsonProperty
    private final Authentication originalAuthentication;

    /**
     * Chained authentications.
     */
    @JsonProperty
    private final List<Authentication> chainedAuthentications;

    @JsonProperty
    private final boolean fromNewLogin;

    @JsonProperty
    private final boolean stateless;

    @JsonProperty
    @Nullable
    private final Service service;

    @JsonProperty
    private final RegisteredService registeredService;

    @JsonProperty
    private final Map<String, Serializable> context;
}
