package org.apereo.cas.adaptors.duo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link DuoSecurityUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString
@Getter
@Setter
@EqualsAndHashCode
public class DuoSecurityUserAccount {

    private DuoSecurityUserAccountStatus status = DuoSecurityUserAccountStatus.AUTH;

    private String enrollPortalUrl;

    private String username;

    private String message;

    public DuoSecurityUserAccount(final String username) {
        this.username = username;
    }
}
