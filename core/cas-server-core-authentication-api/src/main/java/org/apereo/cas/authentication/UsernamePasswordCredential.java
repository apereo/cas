package org.apereo.cas.authentication;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Size;
import java.io.Serializable;
import lombok.ToString;
import lombok.Getter;

/**
 * Credential for authenticating with a username and password.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Slf4j
@ToString(exclude = { "password" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsernamePasswordCredential implements Credential, Serializable {

    /**
     * Authentication attribute name for password.
     **/
    public static final String AUTHENTICATION_ATTRIBUTE_PASSWORD = "credential";

    private static final long serialVersionUID = -700605081472810939L;

    @Size(min = 1, message = "required.username")
    private String username;

    @Size(min = 1, message = "required.password")
    private String password;

    @Override
    public String getId() {
        return this.username;
    }
}
