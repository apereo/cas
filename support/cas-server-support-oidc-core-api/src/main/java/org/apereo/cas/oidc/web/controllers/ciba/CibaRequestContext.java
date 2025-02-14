package org.apereo.cas.oidc.web.controllers.ciba;

import org.apereo.cas.authentication.principal.Principal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import lombok.experimental.SuperBuilder;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * This is {@link CibaRequestContext}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@SuperBuilder
@AllArgsConstructor
@With
public class CibaRequestContext implements Serializable {
    @Serial
    private static final long serialVersionUID = -6506681662666375343L;
    
    private final Set<String> scope;
    private final String clientNotificationToken;
    private final Set<String> acrValues;
    private final String userCode;
    private final String bindingMessage;
    private final String loginHintToken;
    private final String idTokenHint;
    private final String loginHint;
    private final long requestedExpiry;
    private final String clientId;
    private final Principal principal;
    private final String tenant;
}
