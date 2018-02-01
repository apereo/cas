package org.apereo.cas.pm;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link PasswordChangeBean}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class PasswordChangeBean implements Serializable {
    private static final long serialVersionUID = 8885460875620586503L;

    private String password;
    private String confirmedPassword;
}
