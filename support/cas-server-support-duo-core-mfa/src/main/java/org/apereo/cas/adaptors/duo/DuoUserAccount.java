package org.apereo.cas.adaptors.duo;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link DuoUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
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
