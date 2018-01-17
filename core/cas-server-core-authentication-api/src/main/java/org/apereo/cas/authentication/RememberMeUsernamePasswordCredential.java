package org.apereo.cas.authentication;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles both remember me services and username and password.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
@Slf4j
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RememberMeUsernamePasswordCredential extends UsernamePasswordCredential implements RememberMeCredential {
    /** Unique Id for serialization. */
    private static final long serialVersionUID = -6710007659431302397L;

    private boolean rememberMe;
}
