package org.apereo.cas.configuration.model.core.web;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties class for message.bundle.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Slf4j
@Getter
@Setter
public class MessageBundleProperties implements Serializable {

    private static final long serialVersionUID = 3769733438559663237L;

    /**
     * Message bundle character encoding.
     */
    private String encoding = StandardCharsets.UTF_8.name();

    /**
     * Cache size.
     */
    private int cacheSeconds = 180;

    /**
     * Flag that controls whether to fallback to the default system locale if no locale is specified explicitly.
     */
    private boolean fallbackSystemLocale;

    /**
     * Flag that controls whether to use code message.
     */
    private boolean useCodeMessage = true;

    /**
     * A list of strings representing base names for this message bundle.
     */
    private List<String> baseNames = Stream.of("classpath:custom_messages", "classpath:messages").collect(Collectors.toList());

    /**
     * A list of strings representing common names for this message bundle.
     * <p>
     * Entries in last common names override first values (as opposed to baseNames used in message bundles).
     */
    private List<String> commonNames = Stream.of("classpath:common_messages.properties",
        "file:/etc/cas/config/common_messages.properties").collect(Collectors.toList());
}
