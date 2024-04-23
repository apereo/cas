package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link WsFedClient}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@Data
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
