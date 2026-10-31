package org.apereo.cas.configuration.model.support.oauth;

import module java.base;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OAuthCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Getter
@Setter
@Accessors(chain = true)
public class OAuthCoreProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -1687928082301669359L;

    /**
     * Whether approval prompt/consent screen should be bypassed.
     */
    private boolean bypassApprovalPrompt;

    /**
     * User profile view type determines how the final user profile should be rendered
     * once an access token is "validated".
     */
    private UserProfileViewTypes userProfileViewType = UserProfileViewTypes.NESTED;

    /**
     * Resources, as regular expressions, that every OAuth client may request with the RFC 8707 {@code resource}
     * parameter, in addition to those allowed by its registered service. A requested resource must fully match one of
     * the expressions, case-sensitively, the same way it must match the allowed resources of a service. This suits
     * resources hosted by CAS itself, such as its MCP server, which clients that register themselves dynamically
     * cannot list in their own registration. CAS allows its MCP server endpoint by default.
     */
    @RegularExpressionCapable
    private List<String> allowedResources = new ArrayList<>();

    /**
     * Profile view types.
     */
    public enum UserProfileViewTypes {

        /**
         * Return and render the user profile view in nested mode.
         * This is the default option, most usually.
         */
        NESTED,
        /**
         * Return and render the user profile view in flattened mode
         * where all attributes are flattened down to one level only.
         */
        FLAT
    }

}
