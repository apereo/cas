/**
 * 
 */
package org.jasig.cas.support.oauth.token;

import java.util.ArrayList;
import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

/**
 * @author Joe
 *
 */
public class CasTokenServices implements AuthorizationServerTokenServices, ConsumerTokenServices,
        ResourceServerTokenServices {
    
    @NotNull
    private TicketRegistry casTicketRegistry;

    /* (non-Javadoc)
     * @see org.springframework.security.oauth2.provider.token.ResourceServerTokenServices#loadAuthentication(java.lang.String)
     */
    @Override
    public OAuth2Authentication loadAuthentication(String accessTokenValue) throws AuthenticationException {
        OAuth2AccessToken accessToken = readAccessToken(accessTokenValue);
        
        if (accessToken == null) {
            throw new InvalidTokenException("Invalid access token: " + accessTokenValue);
          }
          else if (accessToken.isExpired()) {
            throw new InvalidTokenException("Access token expired: " + accessTokenValue);
          }
        
        // Extract the TGT from the access token
        String tgtValue = (String) accessToken.getAdditionalInformation().get("TGT");
        Ticket ticketGrantingTicket = casTicketRegistry.getTicket(tgtValue);
        
        // Make sure the TGT is valid
        if (ticketGrantingTicket == null) {
            throw new InvalidTokenException("No CAS TGT found widh id " + tgtValue);
        }
        
        if (ticketGrantingTicket.isExpired()) {
            casTicketRegistry.deleteTicket(tgtValue);
            throw new InvalidTokenException("CAS TGT expired: " + tgtValue);
        }
        
        // Make sure the user has access to this service
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.oauth2.provider.token.ResourceServerTokenServices#readAccessToken(java.lang.String)
     */
    @Override
    public OAuth2AccessToken readAccessToken(String accessToken) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.oauth2.provider.token.ConsumerTokenServices#findTokensByUserName(java.lang.String)
     */
    @Override
    public Collection<OAuth2AccessToken> findTokensByUserName(String userName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.oauth2.provider.token.ConsumerTokenServices#findTokensByClientId(java.lang.String)
     * This method means very little for the CasTokenServices, since CAS does not deal with clients, only users.
     */
    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        return new ArrayList<OAuth2AccessToken>();
    }

    /* (non-Javadoc)
     * @see org.springframework.security.oauth2.provider.token.ConsumerTokenServices#revokeToken(java.lang.String)
     */
    @Override
    public boolean revokeToken(String tokenValue) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.oauth2.provider.token.ConsumerTokenServices#getClientId(java.lang.String)
     * This method means very little for the CasTokenServices, since CAS does not deal with clients, only users.
     */
    @Override
    public String getClientId(String tokenValue) {
        // TODO Auto-generated method stub
        return "Client ID Not Implemented for CAS";
    }

    /* (non-Javadoc)
     * @see org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices#createAccessToken(org.springframework.security.oauth2.provider.OAuth2Authentication)
     */
    @Override
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices#refreshAccessToken(java.lang.String, org.springframework.security.oauth2.provider.AuthorizationRequest)
     */
    @Override
    public OAuth2AccessToken refreshAccessToken(String refreshToken, AuthorizationRequest request)
            throws AuthenticationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices#getAccessToken(org.springframework.security.oauth2.provider.OAuth2Authentication)
     */
    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        // TODO Auto-generated method stub
        return null;
    }

}
