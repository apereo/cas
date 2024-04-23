package org.apereo.cas.authentication;

import java.io.Serial;

/**
 * This is {@link DefaultAuthenticationResultBuilderFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class DefaultAuthenticationResultBuilderFactory implements AuthenticationResultBuilderFactory {
    @Serial
    private static final long serialVersionUID = 3506297547445902679L;

    @Override
    public AuthenticationResultBuilder newBuilder() {
        return new DefaultAuthenticationResultBuilder();
    }
}
