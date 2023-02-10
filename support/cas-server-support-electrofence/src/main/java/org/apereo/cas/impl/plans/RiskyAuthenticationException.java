package org.apereo.cas.impl.plans;

import org.apereo.cas.authentication.AuthenticationException;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * This is {@link RiskyAuthenticationException}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@NoArgsConstructor
public class RiskyAuthenticationException extends AuthenticationException {
    @Serial
    private static final long serialVersionUID = 4819155188558680032L;

    private static final String CODE = "RISKY_AUTHN_DETECTED";

    public RiskyAuthenticationException(final Throwable handlerError) {
        super(handlerError);
    }

    @Override
    public String getCode() {
        return CODE;
    }
}
