package org.apereo.cas.digest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AbstractCredential;
import lombok.Getter;

/**
 * This is {@link DigestCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DigestCredential extends AbstractCredential {

    private static final long serialVersionUID = 1523693794392289803L;

    private String realm;

    private String hash;

    private String id;

    /**
     * Instantiates a new Basic identifiable credential.
     *
     * @param id   the id
     * @param realm the realm
     * @param hash  the hash
     */
    @JsonCreator
    public DigestCredential(@JsonProperty("id") final String id, @JsonProperty("realm") final String realm,
                            @JsonProperty("hash") final String hash) {
        this.realm = realm;
        this.hash = hash;
        this.id = id;
    }
    
}
