package org.apereo.cas.oidc.web.controllers.ciba;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import java.util.Set;

/**
 * This is {@link CibaRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@SuperBuilder
public class CibaRequest {
    private Set<String> scope;
    private String clientNotificationToken;
    private Set<String> acrValues;
    private String userCode;
    private String bindingMessage;
    private String loginHintToken;
    private String idTokenHint;
    private String loginHint;
    private long requestedExpiry;
}
