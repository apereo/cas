package org.apereo.cas.authentication;

import org.apereo.cas.authentication.soap.generated.GetSoapAuthenticationRequest;
import org.apereo.cas.authentication.soap.generated.GetSoapAuthenticationResponse;
import org.apereo.cas.authentication.soap.generated.MapItemType;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * This is {@link CasSoapEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Endpoint
public class CasSoapEndpoint {
    /**
     * The namespace URI.
     */
    public static final String NAMESPACE_URI = "http://apereo.org/cas";

    /**
     * Soap authentication request get.
     *
     * @param request the request
     * @return the get soap authentication response
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getSoapAuthenticationRequest")
    @ResponsePayload
    public GetSoapAuthenticationResponse soapAuthenticationRequest(@RequestPayload final GetSoapAuthenticationRequest request) {
        val response = new GetSoapAuthenticationResponse();
        response.setUsername("CAS");
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Test CAS Authentication");
        val map = new MapItemType();
        map.setKey("givenName");
        map.setValue("CAS");
        response.getAttributes().addAll(CollectionUtils.wrap(map));
        return response;
    }
}
