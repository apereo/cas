package org.apereo.cas.qr.authentication;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link QRAuthenticationTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
public class QRAuthenticationTokenCredential extends BasicIdentifiableCredential {
    private static final long serialVersionUID = -8234522701132144037L;

    private String deviceId;

    public QRAuthenticationTokenCredential(final String token, @NonNull final String deviceId) {
        super(token);
        this.deviceId = deviceId;
    }
}
