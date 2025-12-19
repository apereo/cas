package org.apereo.cas.support.pac4j.authentication;

import module java.base;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

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

    private final String location;

    private final String target;
}
