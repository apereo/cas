package org.apereo.cas.api;

import lombok.Data;

/**
 * This is {@link PasswordlessUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Data
public class PasswordlessUserAccount {
    private String username;
    private String email;
    private String phone;
    private String name;
}
