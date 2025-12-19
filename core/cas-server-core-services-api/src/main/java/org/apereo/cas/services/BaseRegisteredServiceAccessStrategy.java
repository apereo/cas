package org.apereo.cas.services;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * This is {@link BaseRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ToString
@Getter
@EqualsAndHashCode
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class BaseRegisteredServiceAccessStrategy implements RegisteredServiceAccessStrategy {
    @Serial
    private static final long serialVersionUID = 2068108924325533291L;

    /**
     * The delegated authn policy.
     */
    protected RegisteredServiceDelegatedAuthenticationPolicy delegatedAuthenticationPolicy =
        new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
}
