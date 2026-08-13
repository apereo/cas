package org.apereo.cas.ticket.code;

import module java.base;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.ServiceAwareTicket;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * An OAuth code is an OAuth token which can be used only once and has a short lifetime.
 * It is used to grant access tokens.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface OAuth20Code extends OAuth20Token, ServiceAwareTicket {

    /**
     * The prefix for OAuth codes.
     */
    String PREFIX = "OC";

    /**
     * The PKCE code challenge.
     *
     * @return code challenge
     */
    String getCodeChallenge();

    /**
     * The PKCE code challenge method.
     *
     * @return code challenge method (i.e. plain, S256, etc)
     */
    String getCodeChallengeMethod();

    /**
     * Sets authorization details.
     *
     * @param details the details
     */
    void setAuthorizationDetails(List<? extends Serializable> details);

    /**
     * Sets issuer state.
     *
     * @param issuerState the issuer state
     */
    void setIssuerState(String issuerState);

    /**
     * Gets authorization details.
     *
     * @return the authorization details
     */
    List<? extends Serializable> getAuthorizationDetails();

    /**
     * Gets issuer state.
     *
     * @return the issuer state
     */
    String getIssuerState();
}
