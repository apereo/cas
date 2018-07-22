package org.apereo.cas.adaptors.swivel;

import org.apereo.cas.authentication.Credential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link SwivelTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SwivelTokenCredential implements Credential {

    private static final long serialVersionUID = 361318678073819595L;

    private String token;

    @Override
    public String getId() {
        return this.token;
    }
}
