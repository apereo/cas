package org.apereo.cas.web;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link DefaultBrowserSessionStorage}.
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
public class DefaultBrowserSessionStorage implements BrowserSessionStorage {
    private static final long serialVersionUID = 775566570310426414L;

    private final String payload;

    private String destinationUrl;
}
