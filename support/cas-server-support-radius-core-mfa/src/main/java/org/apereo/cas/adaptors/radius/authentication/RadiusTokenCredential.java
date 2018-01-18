package org.apereo.cas.adaptors.radius.authentication;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Credential;
import java.io.Serializable;
import lombok.ToString;
import lombok.Getter;

/**
 * This is {@link RadiusTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RadiusTokenCredential implements Credential, Serializable {

    private static final long serialVersionUID = -7570675701132111037L;

    private String token;

    @Override
    public String getId() {
        return this.token;
    }
}
