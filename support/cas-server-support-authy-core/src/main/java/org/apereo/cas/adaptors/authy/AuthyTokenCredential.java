package org.apereo.cas.adaptors.authy;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Credential;

import java.io.Serializable;

/**
 * This is {@link AuthyTokenCredential}.
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
public class AuthyTokenCredential implements Credential, Serializable {
    private static final long serialVersionUID = -7970600701132111037L;

    private String token;

    @Override
    public String getId() {
        return this.token;
    }
}
