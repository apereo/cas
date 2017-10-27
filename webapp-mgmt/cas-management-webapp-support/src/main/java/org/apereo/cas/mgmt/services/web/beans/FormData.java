package org.apereo.cas.mgmt.services.web.beans;

import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.apereo.cas.grouper.GrouperGroupField;
import org.apereo.cas.services.OidcProperties;
import org.apereo.cas.services.OidcSubjectTypes;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.support.saml.services.SamlProperties;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Form data passed onto the screen.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class FormData implements Serializable {
    private static final long serialVersionUID = -5201796557461644152L;

    private List<String> availableAttributes = new ArrayList<>();

    private String[] remoteCodes = {"100", "200", "401", "403", "404", "500"};

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

    public RegisteredService.ServiceType[] getServiceTypes() {
        return RegisteredService.ServiceType.values();
    }

    public SamlProperties.SamlMetadataRoles[] getSamlRoles() {
        return SamlProperties.SamlMetadataRoles.values();
    }

    public SamlProperties.SamlDirections[] getSamlDirections() {
        return SamlProperties.SamlDirections.values();
    }

    public SamlProperties.SamlNameIds[] getSamlNameIds() {
        return SamlProperties.SamlNameIds.values();
    }

    public SamlProperties.SamlCredentialType[] getSamlCredentialTypes() {
        return SamlProperties.SamlCredentialType.values();
    }

    public WSFederationClaims[] getWsFederationClaims() {
        return WSFederationClaims.values();
    }

    public RegisteredServiceMultifactorPolicy.Providers[] getMfaProviders() {
        return RegisteredServiceMultifactorPolicy.Providers.values();
    }

    public RegisteredServiceMultifactorPolicy.FailureModes[] getMfaFailureModes() {
        return RegisteredServiceMultifactorPolicy.FailureModes.values();
    }

    public OidcProperties.Scopes[] getOidcScopes() {
        return OidcProperties.Scopes.values();
    }

    public OidcProperties.EncodingAlgOptions[] getOidcEncodingAlgOptions() {
        return OidcProperties.EncodingAlgOptions.values();
    }

    public OidcProperties.EncryptAlgOptions[] getOidcEncryptAlgOptions() {
        return OidcProperties.EncryptAlgOptions.values();
    }

    public OidcSubjectTypes[] getOidcSubjectTypes() {
        return OidcSubjectTypes.values();
    }

    public CaseCanonicalizationMode[] getCanonicalizationModes() {
        return CaseCanonicalizationMode.values();
    }
}
