package org.apereo.cas.adaptors.duo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * This is {@link DuoSecurityUserAccountGroup}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ToString
@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
public class DuoSecurityUserAccountGroup implements Serializable {
    private static final long serialVersionUID = -2570722888772919096L;

    private final String id;

    private final String name;

    private final String status;

    private String description;

    private boolean pushEnabled;

    private boolean smsEnabled;

    private boolean voiceEnabled;

    private boolean mobileOtpEnabled;
}
