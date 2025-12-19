package org.apereo.cas.authentication.policy;

import module java.base;
import org.apereo.cas.authentication.AuthenticationPolicy;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.Ordered;

/**
 * This is {@link BaseAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@EqualsAndHashCode
@Setter
@Getter
@Accessors(chain = true)
public abstract class BaseAuthenticationPolicy implements AuthenticationPolicy {
    @Serial
    private static final long serialVersionUID = -2825457764398118845L;

    private int order = Ordered.LOWEST_PRECEDENCE;

    private String name = getClass().getSimpleName();
}
