package org.apereo.cas.authentication.credential;

import org.apereo.cas.authentication.RememberMeCredential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Handles both remember me services and username and password.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RememberMeUsernamePasswordCredential extends UsernamePasswordCredential implements RememberMeCredential {
    private static final long serialVersionUID = -6710007659431302397L;

    private boolean rememberMe;
}
