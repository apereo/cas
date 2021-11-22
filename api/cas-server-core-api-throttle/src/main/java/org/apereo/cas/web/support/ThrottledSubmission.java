package org.apereo.cas.web.support;

import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * This is {@link ThrottledSubmission}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Data
public class ThrottledSubmission implements Serializable {
    private static final long serialVersionUID = -853401483455717926L;

    private final String key;

    private final ZonedDateTime value;
}
