package org.apereo.cas.adaptors.swivel;

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
 * This is {@link SwivelCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SwivelCredential implements Credential, Serializable {

    private static final long serialVersionUID = 361318678073819595L;

    private String token;

    @Override
    public String getId() {
        return this.token;
    }
}
