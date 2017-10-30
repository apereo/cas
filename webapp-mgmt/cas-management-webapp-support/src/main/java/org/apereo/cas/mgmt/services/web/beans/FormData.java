package org.apereo.cas.mgmt.services.web.beans;

import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.apereo.cas.grouper.GrouperGroupField;
import org.apereo.cas.services.OidcSubjectTypes;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Form data passed onto the screen.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class FormData implements Serializable {
    private static final long serialVersionUID = -5201796557461644152L;

    private List<String> availableAttributes = new ArrayList<>();

    private String[] remoteCodes = {"100", "200", "401", "403", "404", "500"};

    private String[] samlMetadataRoles = {"SPSSODescriptor", "IDPSSODescriptor"};

    private String[] samlDirections = {"INCLUDE", "EXCLUDE"};

    private String[] samlNameIds = {"BASIC", "URI", "UNSPECIFIED"};

    private String[] samlCredentialTypes = {"BASIC", "X509"};

    private String[] encryptAlgOptions = {
        "RSA-5",
        "RSA-OAEP",
        "RSA-OAEP-256",
        "ECDH-ES",
        "ECDH-ES+A128KW",
        "ECDH-ES+A192KW",
        "ECDH-ES+A256KW",
        "A128KW",
        "A192KW",
        "A256KW",
        "A128GCMKW",
        "A192GXMKW",
        "A256GCMKW",
        "PBES2-HS256+A128KW",
        "PBES2-HS384+A192KW",
        "PBES2-HS512+A256KW"
    };

    private String[] encodingAlgOptions = {
        "A128CBC-HS256",
        "A192CBC-HS384",
        "A256CBC-HS512",
        "A128GCM",
        "A192GCM",
        "A256GCM"
    };


    public List<String> getAvailableAttributes() {
        return this.availableAttributes;
    }

    public void setAvailableAttributes(final List<String> availableAttributes) {
        this.availableAttributes = availableAttributes;
    }

    public RegisteredServiceProperty.RegisteredServiceProperties[] getRegisteredServiceProperties() {
        return RegisteredServiceProperty.RegisteredServiceProperties.values();
    }

    public GrouperGroupField[] getGrouperFields() {
        return GrouperGroupField.values();
    }

    public String[] getRemoteCodes() {
        return remoteCodes;
    }

    public TimeUnit[] getTimeUnits() {
        return TimeUnit.values();
    }

    public AbstractPrincipalAttributesRepository.MergingStrategy[] getMergingStrategies() {
        return AbstractPrincipalAttributesRepository.MergingStrategy.values();
    }

    public RegisteredService.LogoutType[] getLogoutTypes() {
        return RegisteredService.LogoutType.values();
    }

    /**
     * Gets service types.
     *
     * @return the service types
     */
    public List<Option> getServiceTypes() {
        final ArrayList<Option> serviceTypes = new ArrayList<>();
        serviceTypes.add(new Option("CAS Client", "cas"));
        serviceTypes.add(new Option("OAuth2 Client", "oauth"));
        serviceTypes.add(new Option("SAML2 Service Provider", "saml"));
        serviceTypes.add(new Option("OpenID Connect Client", "oidc"));
        serviceTypes.add(new Option("WS Federation", "wsfed"));
        return serviceTypes;
    }

    public String[] getSamlRoles() {
        return samlMetadataRoles;
    }

    public String[] getSamlDirections() {
        return samlDirections;
    }

    public String[] getSamlNameIds() {
        return samlNameIds;
    }

    public String[] getSamlCredentialTypes() {
        return samlCredentialTypes;
    }

    public WSFederationClaims[] getWsFederationClaims() {
        return WSFederationClaims.values();
    }

    /**
     * Gets mfa providers.
     *
     * @return the mfa providers
     */
    public List<Option> getMfaProviders() {
        final ArrayList<Option> providers = new ArrayList<>();
        providers.add(new Option("Duo Security", "mfa-duo"));
        providers.add(new Option("Authy Authenticator", "mfa-authy"));
        providers.add(new Option("YubiKey", "mfa-yubikey"));
        providers.add(new Option("RSA/RADIUS", "mfa-radius"));
        providers.add(new Option("WiKID", "mfa-wikid"));
        providers.add(new Option("Google Authenitcator", "mfa-gauth"));
        providers.add(new Option("Microsoft Azure", "mfa-azure"));
        providers.add(new Option("FIDO U2F", "mfa-u2f"));
        providers.add(new Option("Swivel Secure", "mfa-swivel"));
        return providers;
    }

    /**
     * Get mfa failure modes registered service multifactor policy . failure modes [ ].
     *
     * @return the registered service multifactor policy . failure modes [ ]
     */
    public RegisteredServiceMultifactorPolicy.FailureModes[] getMfaFailureModes() {
        return RegisteredServiceMultifactorPolicy.FailureModes.values();
    }

    /**
     * Gets oidc scopes.
     *
     * @return the oidc scopes
     */
    public List<Option> getOidcScopes() {
        final ArrayList<Option> scopes = new ArrayList<>();
        scopes.add(new Option("Profile", "profile"));
        scopes.add(new Option("Email", "email"));
        scopes.add(new Option("Address", "address"));
        scopes.add(new Option("Phone", "phone"));
        scopes.add(new Option("User Defined", "user_defined"));
        return scopes;
    }

    public String[] getOidcEncodingAlgOptions() {
        return encodingAlgOptions;
    }

    public String[] getOidcEncryptAlgOptions() {
        return encryptAlgOptions;
    }

    public OidcSubjectTypes[] getOidcSubjectTypes() {
        return OidcSubjectTypes.values();
    }

    public CaseCanonicalizationMode[] getCanonicalizationModes() {
        return CaseCanonicalizationMode.values();
    }

    private class Option {
        private String display;
        private String value;

        Option(final String display, final String value) {
            this.display = display;
            this.value = value;
        }

        public String getDisplay() {
            return display;
        }

        public void setDisplay(final String display) {
            this.display = display;
        }

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }
}
