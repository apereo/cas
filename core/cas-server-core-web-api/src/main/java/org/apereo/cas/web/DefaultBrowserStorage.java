package org.apereo.cas.web;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serial;

/**
 * This is {@link DefaultBrowserStorage}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
@Getter
@ToString
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor(force = true)
@Accessors(chain = true)
public class DefaultBrowserStorage implements BrowserStorage {
    @Serial
    private static final long serialVersionUID = 775566570310426414L;

    private final String payload;

    private String destinationUrl;

    @Builder.Default
    private String context = "casBrowserStorageContext";

    @Builder.Default
    private BrowserStorageTypes storageType = BrowserStorageTypes.SESSION;

    @Builder.Default
    private boolean removeOnRead = true;
}
