package org.apereo.cas.configuration.model.core.web;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuration properties class for message.bundle.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class MessageBundleProperties implements Serializable {

    /**
     * Default message bundle prefix for authentication-failure messages.
     */
    public static final String DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE = "authenticationFailure.";

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
     * Set whether to fall back to the system Locale if no files for a specific Locale have been found.
     * If this is turned off, the only fallback will be the default file (e.g. "messages.properties" for basename "messages").
     * Falling back to the system Locale is the default behavior of java.util.ResourceBundle.
     * However, this is often not desirable in an application server environment, where the system
     * Locale is not relevant to the application at all: set this flag to "false" in such a scenario.
     */
    private boolean fallbackSystemLocale;

    /**
     * Flag that controls whether to use code message.
     * Set whether to use the message code as default message instead of throwing a
     * NoSuchMessageException. Useful for development and debugging.
     * Note: In case of a MessageSourceResolvable with multiple codes (like a FieldError) and a
     * MessageSource that has a parent MessageSource, do not activate "useCodeAsDefaultMessage" in
     * the parent: Else, you'll get the first code returned as message by the parent, without attempts to check further codes.
     */
    private boolean useCodeMessage = true;

    /**
     * A list of strings representing base names for this message bundle.
     * Set an array of basenames, each following the basic ResourceBundle convention of not specifying
     * file extension or language codes. The resource location format is up to the specific MessageSource implementation.
     * Regular and XMl properties files are supported: e.g. "messages" will find a "messages.properties",
     * "messages_en.properties" etc arrangement as well as "messages.xml", "messages_en.xml" etc.
     * The associated resource bundles will be checked sequentially when resolving a message code.
     * Note that message definitions in a previous resource bundle will override ones in a later bundle, due to the sequential lookup.
     */
    private List<String> baseNames = Stream.of("classpath:custom_messages", "classpath:messages").collect(Collectors.toList());

    /**
     * A list of strings representing common names for this message bundle.
     * Specify locale-independent common messages, with the message code as key and the
     * full message String (may contain argument placeholders) as value.
     * <p>
     * Entries in last common names override first values (as opposed to baseNames used in message bundles).
     */
    private List<String> commonNames = Stream.of("classpath:common_messages.properties",
        "file:/etc/cas/config/common_messages.properties").collect(Collectors.toList());
}
