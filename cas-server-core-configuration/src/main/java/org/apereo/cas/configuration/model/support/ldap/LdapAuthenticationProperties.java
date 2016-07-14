package org.apereo.cas.configuration.model.support.ldap;

import com.google.common.collect.Lists;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
        ANONYMOUS
    }

    private boolean usePasswordPolicy = true;
    private String dnFormat;
    private String principalAttributeId = "uid";
    private List principalAttributeList = Lists.newArrayList("cn,sn,givenName,displayName");
    private boolean allowMultiplePrincipalAttributeValues;
    private List additionalAttributes;
    private AuthenticationTypes type;


    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation =
            new PrincipalTransformationProperties();

    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

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

    public boolean isUsePasswordPolicy() {
        return usePasswordPolicy;
    }

    public void setUsePasswordPolicy(final boolean usePasswordPolicy) {
        this.usePasswordPolicy = usePasswordPolicy;
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
}
