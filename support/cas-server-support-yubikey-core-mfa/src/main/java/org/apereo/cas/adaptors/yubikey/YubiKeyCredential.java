package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.authentication.credential.OneTimeTokenCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link YubiKeyCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString
@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class YubiKeyCredential extends OneTimeTokenCredential {
    private static final long serialVersionUID = -7570600701132111037L;

    public YubiKeyCredential(final String token) {
        super(token);
    }
}
