package org.apereo.cas.configuration.model.support.slack;

import module java.base;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SlackMessagingProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-notifications-slack")
@Accessors(chain = true)
public class SlackMessagingProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -1679682641899738092L;

    /**
     * To call a Slack Web API method to post messages, CAS needs to be initialized with a Slack API token.
     * A token usually begins with {@code xoxb-} (bot token) or {@code xoxp-} (user token).
     * You get them from each workspace that an app has been installed.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String apiToken;

    /**
     * The name of the user attribute that would then be used as the slack username.
     * If the attribute is left blank, the default principal identifier is used.
     * Note that in either case the final value is prefixed with {@code @},
     * but only if the prefix does not already exist.
     * Multivalued attributes are supported.
     */
    private String usernameAttribute;
}
