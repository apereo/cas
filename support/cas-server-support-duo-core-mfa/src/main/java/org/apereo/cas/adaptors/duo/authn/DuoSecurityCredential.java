package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.MultifactorAuthenticationCredential;
import org.apereo.cas.authentication.credential.AbstractCredential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents the duo credential.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "username", callSuper = true)
public class DuoSecurityCredential extends AbstractCredential implements MultifactorAuthenticationCredential {

    private static final long serialVersionUID = -7570600733132111037L;

    private String username;

    private String signedDuoResponse;

    private String providerId;

    @Override
    public String getId() {
        return this.username;
    }

    @Override
    public boolean isValid() {
        return StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.signedDuoResponse);
    }
}
