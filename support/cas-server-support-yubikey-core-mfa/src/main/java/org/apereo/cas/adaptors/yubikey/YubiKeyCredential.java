package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.authentication.Credential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

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
@AllArgsConstructor
@EqualsAndHashCode
public class YubiKeyCredential implements Credential {

    private static final long serialVersionUID = -7570600701132111037L;

    private String token;

    @Override
    public String getId() {
        return this.token;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.token);
    }
}
