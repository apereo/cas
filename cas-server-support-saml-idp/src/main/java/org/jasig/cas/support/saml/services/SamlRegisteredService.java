package org.jasig.cas.support.saml.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.opensaml.security.credential.Credential;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link SamlRegisteredService} is responsible for managing the SAML metadata for a given SP.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public final class SamlRegisteredService extends RegexRegisteredService {
    private static final long serialVersionUID = 1218757374062931021L;

    private List<String> supportedNameFormats = new ArrayList<>();
    private boolean signAssertions;

    @JsonIgnore
    private Credential signingCredential;

    @JsonIgnore
    private SSODescriptor ssoDescriptor;

    /**
     * Instantiates a new Saml registered service.
     */
    public SamlRegisteredService() {
        super();
        this.supportedNameFormats.add(NameID.UNSPECIFIED);
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.appendSuper(super.toString());

        return builder.toString();
    }

    @Override
    public void copyFrom(final RegisteredService source) {
        super.copyFrom(source);
        final SamlRegisteredService samlRegisteredService = (SamlRegisteredService) source;
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new SamlRegisteredService();
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final SamlRegisteredService rhs = (SamlRegisteredService) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    public void setSupportedNameFormats(final List<String> supportedNameFormats) {
        this.supportedNameFormats = supportedNameFormats;
    }

    public boolean isSignAssertions() {
        return signAssertions;
    }

    public void setSignAssertions(final boolean signAssertions) {
        this.signAssertions = signAssertions;
    }

    public List<String> getSupportedNameFormats() {
        return this.supportedNameFormats;
    }

    public Credential getSigningCredential() {
        return signingCredential;
    }

    public void setSigningCredential(final Credential signingCredential) {
        this.signingCredential = signingCredential;
    }

    public SSODescriptor getSsoDescriptor() {
        return ssoDescriptor;
    }
}
