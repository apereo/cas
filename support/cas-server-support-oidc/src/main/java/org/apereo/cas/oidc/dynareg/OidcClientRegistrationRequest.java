package org.apereo.cas.oidc.dynareg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.cas.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link OidcClientRegistrationRequest}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OidcClientRegistrationRequest implements Serializable {
    private static final long serialVersionUID = 1832102135613155844L;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("subject_type")
    private String subjectType;

    @JsonProperty("token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("grant_types")
    private List<String> grantTypes;

    @JsonProperty("response_types")
    private List<String> responseTypes;

    @JsonProperty("jwks_uri")
    private String jwksUri;
    
    @JsonProperty("sector_identifier_uri")
    private String sectorIdentifierUri;

    @JsonProperty("request_object_signing_alg")
    private String requestObjectSigningAlg;

    public OidcClientRegistrationRequest() {
    }

    public String getScope() {
        return scope;
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public String getClientName() {
        return clientName;
    }

    public String getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public List<String> getGrantTypes() {
        return grantTypes;
    }

    public List<String> getResponseTypes() {
        return responseTypes;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public String getRequestObjectSigningAlg() {
        return requestObjectSigningAlg;
    }

    public String getSectorIdentifierUri() {
        return sectorIdentifierUri;
    }

    public String getSubjectType() {
        return subjectType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("redirectUris", redirectUris)
                .append("clientName", clientName)
                .append("tokenEndpointAuthMethod", tokenEndpointAuthMethod)
                .append("scope", scope)
                .append("grantTypes", grantTypes)
                .append("responseTypes", responseTypes)
                .append("jwksUri", jwksUri)
                .append("sectorIdentifierUri", sectorIdentifierUri)
                .append("requestObjectSigningAlg", requestObjectSigningAlg)
                .append("subjectType", subjectType)
                .toString();
    }

    @JsonIgnore
    public Collection<String> getScopes() {
        return CollectionUtils.wrapList(getScope().split(" "));
    }
}

