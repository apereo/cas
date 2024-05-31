package org.apereo.cas.oidc.web.controllers.ciba;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.With;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link OidcCibaResponse}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Data
@ToString
@AllArgsConstructor
@With
public class OidcCibaResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 8828372040723150718L;
    
    @JsonProperty(OidcConstants.AUTH_REQ_ID)
    private String authenticationRequestId;

    @JsonProperty(OAuth20Constants.EXPIRES_IN)
    private long expiration;
}
