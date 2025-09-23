package org.apereo.cas.configuration.model.support.email;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link MailgunProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-mailgun", automated = false)
@Accessors(chain = true)
public class MailgunProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -1514930082372661815L;

    /**
     * API key used to authenticate to the service.
     */
    @ExpressionLanguageCapable
    @RequiredProperty
    private String apiKey;

    /**
     * Base URL for the service.
     * <ul>
     *     <li>For US regions (Default): {@code https://api.mailgun.net}</li>
     *     <li>For EU regions: {@code https://api.eu.mailgun.net}</li>
     * </ul>
     */
    @ExpressionLanguageCapable
    @RequiredProperty
    private String baseUrl = "https://api.mailgun.net";

    /**
     * Whether the service is operating in test/developer mode.
     */
    private boolean testMode;

    /**
     * Your Mailgun account may contain multiple sending domains.
     * Requests must include the address of the domain you're interested in.
     * Example: {@code my.mailgun.org}
     */
    private String domain;
}
