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

    private String deviceName;

    public InweboCredential(final String login, final String deviceName) {
        super(login);
        this.deviceName = deviceName;
    }
}
