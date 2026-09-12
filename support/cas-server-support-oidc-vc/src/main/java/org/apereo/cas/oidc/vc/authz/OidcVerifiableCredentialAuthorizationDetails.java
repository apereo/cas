package org.apereo.cas.oidc.vc.authz;

import module java.base;
import org.apereo.cas.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SuperBuilder;
import lombok.ToString;
import lombok.val;

/**
 * This is {@link OidcVerifiableCredentialAuthorizationDetails}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public class OidcVerifiableCredentialAuthorizationDetails implements Serializable {
    /**
     * The default type of authz for verifiable credentials.
     */
    public static final String TYPE = "openid_credential";

    @Serial
    private static final long serialVersionUID = -1723524572989494292L;
    
    @Builder.Default
    private String type = TYPE;

    @JsonProperty("credential_configuration_id")
    private String credentialConfigurationId;

    /**
     * Identifiers the wallet must then present as {@code credential_identifier} at the credential
     * endpoint. CAS issues the credential configuration id itself as the identifier.
     */
    @JsonProperty("credential_identifiers")
    private List<String> credentialIdentifiers;

    /**
     * Construct list of authz details.
     *
     * @param authorizationDetails     the authorization details
     * @param credentialConfigurations the credential configurations
     * @return the list
     */
    public static List<OidcVerifiableCredentialAuthorizationDetails> from(
        final String authorizationDetails, final Set<String> credentialConfigurations) {
        val authorized = JsonUtils.parseAsList(authorizationDetails, OidcVerifiableCredentialAuthorizationDetails.class)
            .stream()
            .filter(details -> details.getType().equals(OidcVerifiableCredentialAuthorizationDetails.TYPE))
            .filter(details -> credentialConfigurations.contains(details.getCredentialConfigurationId()))
            .collect(Collectors.toList());
        authorized.forEach(details -> details.setCredentialIdentifiers(List.of(details.getCredentialConfigurationId())));
        return authorized;
    }
}
