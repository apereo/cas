package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.AbstractCredential;

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
@EqualsAndHashCode(of = {"username"}, callSuper = true)
public class DuoCredential extends AbstractCredential {

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
