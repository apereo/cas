package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This is {@link PasswordlessTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PasswordlessTokenCredential extends OneTimePasswordCredential {
    @Serial
    private static final long serialVersionUID = -4781957245167355023L;

    @JsonCreator
    public PasswordlessTokenCredential(@JsonProperty("id") final String id,
                                       @JsonProperty("password") final String password) {
        super(id, password);
    }
}
