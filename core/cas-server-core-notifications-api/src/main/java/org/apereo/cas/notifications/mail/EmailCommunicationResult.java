package org.apereo.cas.notifications.mail;

import module java.base;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link EmailCommunicationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SuperBuilder
@Getter
@RequiredArgsConstructor
@With
public class EmailCommunicationResult implements Serializable {
    @Serial
    private static final long serialVersionUID = -8625548429667623291L;

    private final List<String> to;

    private final boolean success;

    @Nullable
    private final String body;

    @Builder.Default
    private final Map<String, String> details = new HashMap<>();
}
