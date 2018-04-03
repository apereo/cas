package org.apereo.cas.web.flow;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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
    private static final long serialVersionUID = 2733280849157146990L;

    private boolean autoRedirect;
    private String id;
    private String redirectUrl;
    private String name;
    private String replyingPartyId;
    private String authorizationUrl;
}
