package org.apereo.cas.oidc.dynareg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.CollectionUtils;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import lombok.ToString;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This is {@link OidcClientRegistrationRequest}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
@ToString
@Getter
@NoArgsConstructor
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

    @JsonIgnore
    public Collection<String> getScopes() {
        return CollectionUtils.wrapList(getScope().split(" "));
    }
}
