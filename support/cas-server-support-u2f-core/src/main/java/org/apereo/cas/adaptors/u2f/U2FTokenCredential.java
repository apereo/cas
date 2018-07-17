package org.apereo.cas.adaptors.u2f;

import org.apereo.cas.authentication.Credential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link U2FTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class U2FTokenCredential implements Credential {

    private static final long serialVersionUID = -970682410132111037L;

    private String token;

    @Override
    public String getId() {
        return this.token;
    }
}
