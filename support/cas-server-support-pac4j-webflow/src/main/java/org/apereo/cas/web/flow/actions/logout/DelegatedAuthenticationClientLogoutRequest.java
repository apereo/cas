package org.apereo.cas.web.flow.actions.logout;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link DelegatedAuthenticationClientLogoutRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuperBuilder
@Getter
public class DelegatedAuthenticationClientLogoutRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 4280830773566610694L;

    private final int status;

    private final String message;
}
