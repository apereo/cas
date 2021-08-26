package org.apereo.cas.adaptors.duo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * This is {@link DuoSecurityBypassCode}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ToString
@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
public class DuoSecurityBypassCode implements Serializable {
    private static final long serialVersionUID = -1570722888772919096L;

    private final String id;

    private long created;

    private long expiration;

    private String createdBy;

    private long reuseCount;
}
