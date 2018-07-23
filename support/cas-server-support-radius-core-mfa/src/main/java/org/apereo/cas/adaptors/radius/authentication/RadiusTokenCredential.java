package org.apereo.cas.adaptors.radius.authentication;

import org.apereo.cas.authentication.Credential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link RadiusTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RadiusTokenCredential implements Credential {

    private static final long serialVersionUID = -7570675701132111037L;

    private String token;

    @Override
    public String getId() {
        return this.token;
    }
}
