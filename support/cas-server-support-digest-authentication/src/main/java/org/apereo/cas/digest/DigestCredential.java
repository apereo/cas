package org.apereo.cas.digest;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link DigestCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DigestCredential extends BasicIdentifiableCredential {

    private static final long serialVersionUID = 1523693794392289803L;

    private String realm;

    private String hash;

    /**
     * Instantiates a new Basic identifiable credential.
     *
     * @param id    the id
     * @param realm the realm
     * @param hash  the hash
     */
    @JsonCreator
    public DigestCredential(@JsonProperty("id") final String id, @JsonProperty("realm") final String realm,
                            @JsonProperty("hash") final String hash) {
        super(id);
        this.realm = realm;
        this.hash = hash;
    }

}
