package org.apereo.cas.notifications.mail;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link EmailCommunicationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SuperBuilder
@Getter
public class EmailCommunicationResult implements Serializable {
    private static final long serialVersionUID = -8625548429667623291L;

    private final List<String> to;

    private final boolean success;

    private final String body;
}
