package org.jasig.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalAttributesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Abstract release policy for attributes, provides common shared settings such as loggers and attribute filter config.
 * Subclasses are to provide the behavior for attribute retrieval.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public abstract class AbstractRegisteredServiceAttributeReleasePolicy implements RegisteredServiceAttributeReleasePolicy {
    
    private static final long serialVersionUID = 5325460875620586503L;

    /** The logger. */
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The attribute filter. */
    private RegisteredServiceAttributeFilter registeredServiceAttributeFilter;

    /** Attribute repository that refreshes attributes for a principal. **/
    private PrincipalAttributesRepository principalAttributesRepository = new DefaultPrincipalAttributesRepository();

    /** Authorize the release of credential for this service. Default is false. **/
    private boolean authorizedToReleaseCredentialPassword;

    /** Authorize the release of PGT for this service. Default is false. **/
    private boolean authorizedToReleaseProxyGrantingTicket;

    @Override
    public final void setAttributeFilter(final RegisteredServiceAttributeFilter filter) {
        this.registeredServiceAttributeFilter = filter;
    }

    public final void setPrincipalAttributesRepository(final PrincipalAttributesRepository repository) {
        this.principalAttributesRepository = repository;
    }

    public PrincipalAttributesRepository getPrincipalAttributesRepository() {
        return principalAttributesRepository;
    }

    /**
     * Gets the attribute filter.
     *
     * @return the attribute filter
     */
    public final RegisteredServiceAttributeFilter getAttributeFilter() {
        return this.registeredServiceAttributeFilter;
    }

    @Override
    public boolean isAuthorizedToReleaseCredentialPassword() {
        return this.authorizedToReleaseCredentialPassword;
    }

    @Override
    public boolean isAuthorizedToReleaseProxyGrantingTicket() {
        return this.authorizedToReleaseProxyGrantingTicket;
    }

    public void setAuthorizedToReleaseCredentialPassword(final boolean authorizedToReleaseCredentialPassword) {
        this.authorizedToReleaseCredentialPassword = authorizedToReleaseCredentialPassword;
    }

    public void setAuthorizedToReleaseProxyGrantingTicket(final boolean authorizedToReleaseProxyGrantingTicket) {
        this.authorizedToReleaseProxyGrantingTicket = authorizedToReleaseProxyGrantingTicket;
    }

    @Override
    public final Map<String, Object> getAttributes(final Principal p) {
        final Map<String, Object> principalAttributes = this.principalAttributesRepository == null
                ? p.getAttributes() : this.principalAttributesRepository.getAttributes(p);
        final Map<String, Object> attributesToRelease = getAttributesInternal(principalAttributes);
        
        if (this.registeredServiceAttributeFilter != null) {
            return this.registeredServiceAttributeFilter.filter(attributesToRelease);
        }
        return attributesToRelease;
    }
    
    /**
     * Gets the attributes internally from the implementation.
     *
     * @param attributes the principal attributes
     * @return the attributes allowed for release
     */
    protected abstract Map<String, Object> getAttributesInternal(Map<String, Object> attributes);

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 133)
                .append(this.registeredServiceAttributeFilter)
                .append(this.authorizedToReleaseCredentialPassword)
                .append(this.authorizedToReleaseProxyGrantingTicket)
                .append(this.principalAttributesRepository)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof AbstractRegisteredServiceAttributeReleasePolicy)) {
            return false;
        }

        final AbstractRegisteredServiceAttributeReleasePolicy that = (AbstractRegisteredServiceAttributeReleasePolicy) o;
        final EqualsBuilder builder = new EqualsBuilder();
        return builder
                .append(this.registeredServiceAttributeFilter, that.registeredServiceAttributeFilter)
                .append(this.authorizedToReleaseCredentialPassword, that.authorizedToReleaseCredentialPassword)
                .append(this.authorizedToReleaseProxyGrantingTicket, that.authorizedToReleaseProxyGrantingTicket)
                .append(this.principalAttributesRepository, that.principalAttributesRepository)
                .isEquals();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("attributeFilter", registeredServiceAttributeFilter)
                .append("principalAttributesRepository", principalAttributesRepository)
                .append("authorizedToReleaseCredentialPassword", authorizedToReleaseCredentialPassword)
                .append("authorizedToReleaseProxyGrantingTicket", authorizedToReleaseProxyGrantingTicket)
                .toString();
    }
}


