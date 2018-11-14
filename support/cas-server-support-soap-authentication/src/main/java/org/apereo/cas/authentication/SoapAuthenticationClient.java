package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.soap.generated.GetSoapAuthenticationRequest;
import org.apereo.cas.authentication.soap.generated.GetSoapAuthenticationResponse;

import lombok.val;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;

/**
 * This is {@link SoapAuthenticationClient}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class SoapAuthenticationClient extends WebServiceGatewaySupport {

    /**
     * Send request soap authentication request.
     *
     * @param request the request
     * @return the soap authentication response
     */
    public GetSoapAuthenticationResponse sendRequest(final GetSoapAuthenticationRequest request) {
        return (GetSoapAuthenticationResponse) getWebServiceTemplate().marshalSendAndReceive(request);
    }

    /**
     * Configure credentials.
     *
     * @param credential the credential
     */
    public void setCredentials(final UsernamePasswordCredential credential) {
        val wss4jSecurityInterceptor = new Wss4jSecurityInterceptor();
        wss4jSecurityInterceptor.setSecurementActions("Timestamp UsernameToken");
        wss4jSecurityInterceptor.setSecurementUsername(credential.getUsername());
        wss4jSecurityInterceptor.setSecurementPassword(credential.getPassword());
        setInterceptors(new ClientInterceptor[]{wss4jSecurityInterceptor});
    }
}
