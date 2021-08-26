package org.apereo.cas.support.inwebo.authentication;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Inwebo credentials.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InweboCredential extends BasicIdentifiableCredential {

    private static final long serialVersionUID = 7458888463097030052L;

    private String otp;

    private String deviceName;

    private boolean alreadyAuthenticated;

    public InweboCredential(final String login) {
        super(login);
    }

    public String getLogin() {
        return getId();
    }
}
