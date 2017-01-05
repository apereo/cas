package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link LdapAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class LdapAuthenticationProperties extends AbstractLdapProperties {

    /**
     * The enum Authentication types.
     */
    public enum AuthenticationTypes {
        /**
         * Active Directory.
         */
        AD,
        /**
         * Authenticated Search.
         */
        AUTHENTICATED,
        /**
         * Direct Bind.
         */
        DIRECT,
        /**
         * Anonymous Search.
         */
        ANONYMOUS,
        /**
         * SASL bind search.
         */
        SASL
    }

    @NestedConfigurationProperty
    private PasswordPolicyProperties passwordPolicy = new PasswordPolicyProperties();

    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation =
            new PrincipalTransformationProperties();

    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    private String credentialCriteria;
    private String dnFormat;
    private String principalAttributeId;
    private String principalAttributePassword;
    private List principalAttributeList = new ArrayList();
    private boolean allowMultiplePrincipalAttributeValues;
    private List additionalAttributes = new ArrayList();
    private AuthenticationTypes type;

    private boolean subtreeSearch = true;
    private String baseDn;
    private String userFilter;

    private boolean enhanceWithEntryResolver = true;

    public boolean isEnhanceWithEntryResolver() {
        return enhanceWithEntryResolver;
    }

    public void setEnhanceWithEntryResolver(final boolean enhanceWithEntryResolver) {
        this.enhanceWithEntryResolver = enhanceWithEntryResolver;
    }
    
    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(final String baseDn) {
        this.baseDn = baseDn;
    }

    public String getUserFilter() {
        return userFilter;
    }

    public void setUserFilter(final String userFilter) {
        this.userFilter = userFilter;
    }

    public boolean isSubtreeSearch() {
        return subtreeSearch;
    }

    public void setSubtreeSearch(final boolean subtreeSearch) {
        this.subtreeSearch = subtreeSearch;
    }
    
    public PasswordPolicyProperties getPasswordPolicy() {
        return passwordPolicy;
    }

    public void setPasswordPolicy(final PasswordPolicyProperties passwordPolicy) {
        this.passwordPolicy = passwordPolicy;
    }

    public PrincipalTransformationProperties getPrincipalTransformation() {
        return principalTransformation;
    }

    public void setPrincipalTransformation(final PrincipalTransformationProperties principalTransformation) {
        this.principalTransformation = principalTransformation;
    }

    public PasswordEncoderProperties getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    public String getDnFormat() {
        return dnFormat;
    }

    public void setDnFormat(final String dnFormat) {
        this.dnFormat = dnFormat;
    }

    public AuthenticationTypes getType() {
        return type;
    }

    public void setType(final AuthenticationTypes type) {
        this.type = type;
    }

    public String getPrincipalAttributeId() {
        return principalAttributeId;
    }

    public void setPrincipalAttributeId(final String principalAttributeId) {
        this.principalAttributeId = principalAttributeId;
    }

    public String getPrincipalAttributePassword() {
        return principalAttributePassword;
    }

    public void setPrincipalAttributePassword(final String principalAttributePassword) {
        this.principalAttributePassword = principalAttributePassword;
    }

    public List getPrincipalAttributeList() {
        return principalAttributeList;
    }

    public void setPrincipalAttributeList(final List principalAttributeList) {
        this.principalAttributeList = principalAttributeList;
    }

    public boolean isAllowMultiplePrincipalAttributeValues() {
        return allowMultiplePrincipalAttributeValues;
    }

    public void setAllowMultiplePrincipalAttributeValues(final boolean allowMultiplePrincipalAttributeValues) {
        this.allowMultiplePrincipalAttributeValues = allowMultiplePrincipalAttributeValues;
    }

    public List getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(final List additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public String getCredentialCriteria() {
        return credentialCriteria;
    }

    public void setCredentialCriteria(final String credentialCriteria) {
        this.credentialCriteria = credentialCriteria;
    }
}
