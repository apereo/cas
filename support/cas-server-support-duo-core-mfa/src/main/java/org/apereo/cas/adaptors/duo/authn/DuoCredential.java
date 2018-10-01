package org.apereo.cas.adaptors.duo.authn;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;

/**
 * Represents the duo credential.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"username"})
public class DuoCredential implements Credential {

    private static final long serialVersionUID = -7570600733132111037L;

    private String username;

    private String signedDuoResponse;

    private String mark;

    @Override
    public String getId() {
        return this.username;
    }

    @Override
    public void setMark(final String mark) {
        this.mark = mark;
    }

    @Override
    public String getMark() {
        return mark;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.signedDuoResponse);
    }

}
