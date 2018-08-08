package org.apereo.cas.uma.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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
    private List<String> patProfilesSupported = CollectionUtils.wrapList("bearer");

    @JsonProperty("aat_profiles_supported")
    private List<String> aatProfilesSupported = CollectionUtils.wrapList("bearer");

    @JsonProperty("rpt_profiles_supported")
    private List<String> rptProfilesSupported = CollectionUtils.wrapList("bearer");

    @JsonProperty("claim_token_profiles_supported")
    private List<String> clientTokenProfilesSupported = new ArrayList<>();

    @JsonProperty("uma_profiles_supported")
    private List<String> umaProfilesSupported = new ArrayList<>();

    @JsonProperty("pat_grant_types_supported")
    private List<String> patGrantTypesSupported = CollectionUtils.wrapList(
        OAuth20GrantTypes.AUTHORIZATION_CODE.getType(),
        OAuth20GrantTypes.CLIENT_CREDENTIALS.getType());

    @JsonProperty("aat_grant_types_supported")
    private List<String> aatGrantTypesSupported = CollectionUtils.wrapList(
        OAuth20GrantTypes.AUTHORIZATION_CODE.getType(),
        OAuth20GrantTypes.CLIENT_CREDENTIALS.getType());

    public UmaServerDiscoverySettings(final CasConfigurationProperties casProperties, final String issuer) {
        this.issuer = issuer;
        this.serverPrefix = casProperties.getServer().getPrefix();
        this.casProperties = casProperties;
    }

    /*
		m.put("dynamic_client_endpoint", issuer + DynamicClientRegistrationEndpoint.URL);
		m.put("token_endpoint", issuer + "token");
		m.put("authorization_endpoint", issuer + "authorize");
		m.put("requesting_party_claims_endpoint", issuer + ClaimsCollectionEndpoint.URL);
		m.put("introspection_endpoint", issuer + IntrospectionEndpoint.URL);
		m.put("resource_set_registration_endpoint", issuer + ResourceSetRegistrationEndpoint.DISCOVERY_URL);
		m.put("permission_registration_endpoint", issuer + PermissionRegistrationEndpoint.URL);
		m.put("rpt_endpoint", issuer + AuthorizationRequestEndpoint.URL);
     */
}
