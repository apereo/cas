package org.apereo.cas.uma.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20ClaimTokenFormats;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link UmaServerDiscoverySettings}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@RequiredArgsConstructor
public class UmaServerDiscoverySettings {
    @JsonIgnore
    private final CasConfigurationProperties casProperties;

    @JsonIgnore
    private final String serverPrefix;

    @JsonProperty
    private final String issuer;

    @JsonProperty
    private String version = "1.0";

    @JsonProperty("pat_profiles_supported")
    private List<String> patProfilesSupported = CollectionUtils.wrapList(OAuth20Constants.TOKEN_TYPE_BEARER);

    @JsonProperty("aat_profiles_supported")
    private List<String> aatProfilesSupported = CollectionUtils.wrapList(OAuth20Constants.TOKEN_TYPE_BEARER);

    @JsonProperty("rpt_profiles_supported")
    private List<String> rptProfilesSupported = CollectionUtils.wrapList(OAuth20Constants.TOKEN_TYPE_BEARER);

    @JsonProperty("claim_token_profiles_supported")
    private List<String> clientTokenProfilesSupported = CollectionUtils.wrapList(OAuth20ClaimTokenFormats.IDTOKEN.getType(),
        OAuth20ClaimTokenFormats.JWT.getType());

    @JsonProperty("uma_profiles_supported")
    private List<String> umaProfilesSupported = new ArrayList<>(0);

    @JsonProperty("pat_grant_types_supported")
    private List<String> patGrantTypesSupported = CollectionUtils.wrapList(
        OAuth20GrantTypes.AUTHORIZATION_CODE.getType(),
        OAuth20GrantTypes.UMA_TICKET.getType(),
        OAuth20GrantTypes.CLIENT_CREDENTIALS.getType());

    @JsonProperty("aat_grant_types_supported")
    private List<String> aatGrantTypesSupported = CollectionUtils.wrapList(
        OAuth20GrantTypes.AUTHORIZATION_CODE.getType(),
        OAuth20GrantTypes.UMA_TICKET.getType(),
        OAuth20GrantTypes.CLIENT_CREDENTIALS.getType());

    public UmaServerDiscoverySettings(final CasConfigurationProperties casProperties, final String issuer) {
        this.issuer = issuer;
        this.serverPrefix = casProperties.getServer().getPrefix();
        this.casProperties = casProperties;
    }

    @JsonProperty("dynamic_client_endpoint")
    public String getDynamicClientEndpoint() {
        return StringUtils.appendIfMissing(issuer, "/").concat(OAuth20Constants.UMA_REGISTRATION_URL);
    }

    @JsonProperty("token_endpoint")
    public String getTokenEndpoint() {
        return StringUtils.appendIfMissing(issuer, "/").concat(OAuth20Constants.ACCESS_TOKEN_URL);
    }

    @JsonProperty("authorization_endpoint")
    public String getAuthorizationEndpoint() {
        return StringUtils.appendIfMissing(issuer, "/").concat(OAuth20Constants.AUTHORIZE_URL);
    }

    @JsonProperty("requesting_party_claims_endpoint")
    public String getRequestingPartyClaimsEndpoint() {
        return StringUtils.appendIfMissing(issuer, "/").concat(OAuth20Constants.UMA_CLAIMS_COLLECTION_URL);
    }

    @JsonProperty("introspection_endpoint")
    public String getIntrospectionEndpoint() {
        return StringUtils.appendIfMissing(issuer, "/").concat(OAuth20Constants.INTROSPECTION_URL);
    }

    @JsonProperty("resource_set_registration_endpoint")
    public String getResourceSetRegistrationEndpoint() {
        return StringUtils.appendIfMissing(issuer, "/").concat(OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL);
    }

    @JsonProperty("permission_registration_endpoint")
    public String getPermissionRegistrationEndpoint() {
        return StringUtils.appendIfMissing(issuer, "/").concat(OAuth20Constants.UMA_PERMISSION_URL);
    }

    @JsonProperty("rpt_endpoint")
    public String getAuthorizationRequestEndpoint() {
        return StringUtils.appendIfMissing(issuer, "/").concat(OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL);
    }
}
