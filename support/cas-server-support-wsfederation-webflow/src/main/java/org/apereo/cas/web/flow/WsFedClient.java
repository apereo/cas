package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link WsFedClient}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 * @deprecated Since 8.0.0, WS-Federation support is deprecated and scheduled for removal.
 */
@Getter
@Setter
@Data
@Deprecated(since = "8.0.0", forRemoval = true)
public class WsFedClient implements Serializable {
    @Serial
    private static final long serialVersionUID = 2733280849157146990L;

    private DelegationAutoRedirectTypes autoRedirectType = DelegationAutoRedirectTypes.NONE;

    private String id;

    private String redirectUrl;

    private String name;

    private String replyingPartyId;

    private String authorizationUrl;
}
