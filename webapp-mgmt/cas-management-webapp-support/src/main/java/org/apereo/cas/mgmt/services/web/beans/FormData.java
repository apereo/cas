package org.apereo.cas.mgmt.services.web.beans;

import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.grouper.GrouperGroupField;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcSubjectTypes;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.opensaml.saml.metadata.resolver.filter.impl.PredicateFilter;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.reflections.ReflectionUtils;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Form data passed onto the screen.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class FormData implements Serializable {
    private static final long serialVersionUID = -5201796557461644152L;

    private List<String> availableAttributes = new ArrayList<>();

    private List<Integer> remoteCodes = Arrays.stream(HttpStatus.values()).map(HttpStatus::value).collect(Collectors.toList());

    private String[] samlMetadataRoles = {SPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME, IDPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME};

    private List<String> samlDirections = Arrays.stream(PredicateFilter.Direction.values()).map(s -> s.name().toUpperCase()).collect(Collectors.toList());

    private String[] samlAttributeNameFormats = {Attribute.BASIC, Attribute.UNSPECIFIED, Attribute.URI_REFERENCE};

    private List<String> samlCredentialTypes = Arrays.stream(SamlIdPProperties.Response.SignatureCredentialTypes.values())
            .map(s -> s.name().toUpperCase())
            .collect(Collectors.toList());

    private List<String> encryptAlgOptions = locateKeyAlgorithmsSupported();

    private List<String> encodingAlgOptions = locateContentEncryptionAlgorithmsSupported();

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

    public List<Integer> getRemoteCodes() {
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

    public List<String> getSamlDirections() {
        return samlDirections;
    }

    public String[] getSamlAttributeNameFormats() {
        return samlAttributeNameFormats;
    }

    public List<String> getSamlCredentialTypes() {
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
        final List<Option> scopes = Arrays.stream(OidcConstants.StandardScopes.values())
                .map(scope -> new Option(scope.getFriendlyName(), scope.getScope()))
                .collect(Collectors.toList());
        scopes.add(new Option("User Defined", "user_defined"));
        return scopes;
    }

    public List<String> getOidcEncodingAlgOptions() {
        return encodingAlgOptions;
    }

    public List<String> getOidcEncryptAlgOptions() {
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


    private List<String> locateKeyAlgorithmsSupported() {
        return ReflectionUtils.getFields(KeyManagementAlgorithmIdentifiers.class,
            field -> Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                    && field.getType().equals(String.class))
            .stream()
            .map(Field::getName)
            .sorted()
            .collect(Collectors.toList());
    }

    private List<String> locateContentEncryptionAlgorithmsSupported() {
        return ReflectionUtils.getFields(ContentEncryptionAlgorithmIdentifiers.class,
            field -> Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                    && field.getType().equals(String.class))
            .stream()
            .map(Field::getName)
            .sorted()
            .collect(Collectors.toList());
    }
}
