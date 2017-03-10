package org.apereo.cas.oidc.dynareg;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link OidcClientRegistrationResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcClientRegistrationResponse implements Serializable {
    private static final long serialVersionUID = 1436206039117219598L;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("application_type")
    private String applicationType;

    @JsonProperty("subject_type")
    private String subjectType;

    @JsonProperty("grant_types")
    private List<String> grantTypes;

    @JsonProperty("response_types")
    private List<String> responseTypes;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris;

    @JsonProperty("request_object_signing_alg")
    private String requestObjectSigningAlg;

    @JsonProperty("token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setClientName(final String clientName) {
        this.clientName = clientName;
    }

    public void setApplicationType(final String applicationType) {
        this.applicationType = applicationType;
    }

    public void setSubjectType(final String subjectType) {
        this.subjectType = subjectType;
    }

    public void setGrantTypes(final List<String> grantTypes) {
        this.grantTypes = grantTypes;
    }

    public void setResponseTypes(final List<String> responseTypes) {
        this.responseTypes = responseTypes;
    }

    public void setRedirectUris(final List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public void setRequestObjectSigningAlg(final String requestObjectSigningAlg) {
        this.requestObjectSigningAlg = requestObjectSigningAlg;
    }

    public void setTokenEndpointAuthMethod(final String tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getClientName() {
        return clientName;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public List<String> getGrantTypes() {
        return grantTypes;
    }

    public List<String> getResponseTypes() {
        return responseTypes;
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public String getRequestObjectSigningAlg() {
        return requestObjectSigningAlg;
    }

    public String getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }
}
