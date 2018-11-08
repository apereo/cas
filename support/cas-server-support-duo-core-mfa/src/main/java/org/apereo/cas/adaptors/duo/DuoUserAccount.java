package org.apereo.cas.adaptors.duo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link DuoUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString
@Getter
@Setter
@EqualsAndHashCode
public class DuoUserAccount {

    private DuoUserAccountAuthStatus status = DuoUserAccountAuthStatus.AUTH;

    private String enrollPortalUrl;

    private String username;

    private String message;

    public DuoUserAccount(final String username) {
        this.username = username;
    }

}
