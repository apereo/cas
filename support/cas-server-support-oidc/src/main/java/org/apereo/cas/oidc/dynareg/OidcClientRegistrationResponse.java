package org.apereo.cas.oidc.dynareg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link OidcClientRegistrationResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@Setter
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
}
