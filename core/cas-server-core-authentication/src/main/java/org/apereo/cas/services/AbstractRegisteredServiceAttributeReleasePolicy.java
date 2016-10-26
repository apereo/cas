package org.apereo.cas.services;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newTreeMap;

/**
 * Abstract release policy for attributes, provides common shared settings such as loggers and attribute filter config.
 * Subclasses are to provide the behavior for attribute retrieval.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public abstract class AbstractRegisteredServiceAttributeReleasePolicy implements RegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = 5325460875620586503L;

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegisteredServiceAttributeReleasePolicy.class);

    /**
     * The attribute filter.
     */
    private RegisteredServiceAttributeFilter registeredServiceAttributeFilter;

    /**
     * Attribute repository that refreshes attributes for a principal.
     **/
    private PrincipalAttributesRepository principalAttributesRepository = new DefaultPrincipalAttributesRepository();

    /**
     * Authorize the release of credential for this service. Default is false.
     **/
    private boolean authorizedToReleaseCredentialPassword;

    /**
     * Authorize the release of PGT for this service. Default is false.
     **/
    private boolean authorizedToReleaseProxyGrantingTicket;

    @Override
    public void setAttributeFilter(final RegisteredServiceAttributeFilter filter) {
        this.registeredServiceAttributeFilter = filter;
    }

    public void setPrincipalAttributesRepository(final PrincipalAttributesRepository repository) {
        this.principalAttributesRepository = repository;
    }

    public PrincipalAttributesRepository getPrincipalAttributesRepository() {
        return this.principalAttributesRepository;
    }

    /**
     * Gets the attribute filter.
     *
     * @return the attribute filter
     */
    public RegisteredServiceAttributeFilter getAttributeFilter() {
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
    public Map<String, Object> getAttributes(final Principal p) {
        LOGGER.debug("Locating principal attributes for {}", p.getId());
        final Map<String, Object> principalAttributes = this.principalAttributesRepository == null
                ? p.getAttributes() : this.principalAttributesRepository.getAttributes(p);
        LOGGER.debug("Found principal attributes {} for {}", principalAttributes, p.getId());

        LOGGER.debug("Calling attribute policy {} to process attributes for {}", getClass().getSimpleName(), p.getId());
        final Map<String, Object> policyAttributes = getAttributesInternal(principalAttributes);
        LOGGER.debug("Attribute policy {} allows release of {} for {}", getClass().getSimpleName(), policyAttributes, p.getId());

        LOGGER.debug("Checking default attribute policy attributes");
        final Map<String, Object> defaultAttributes = getReleasedByDefaultAttributes(p, principalAttributes);
        LOGGER.debug("Default attributes found to be released are {}", defaultAttributes);

        LOGGER.debug("Attempting to merge policy attributes and default attributes");
        final Map<String, Object> attributesToRelease = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

        LOGGER.debug("Adding default attributes first to the released set of attributes");
        attributesToRelease.putAll(defaultAttributes);

        LOGGER.debug("Adding policy attributes to the released set of attributes");
        attributesToRelease.putAll(policyAttributes);

        if (this.registeredServiceAttributeFilter != null) {
            LOGGER.debug("Invoking attribute filter on the final set of attributes");
            return this.registeredServiceAttributeFilter.filter(attributesToRelease);
        }

        return returnFinalAttributesCollection(attributesToRelease);
    }

    /**
     * Return the final attributes collection.
     * Subclasses may override this minute to impose last minute rules.
     * @param attributesToRelease the attributes to release
     * @return the map
     */
    protected Map<String, Object> returnFinalAttributesCollection(final Map<String, Object> attributesToRelease) {
        LOGGER.debug("Final collection of attributes allowed are: {}", attributesToRelease);
        return attributesToRelease;
    }

    /**
     * Determines a default bundle of attributes that may be released to all services
     * without the explicit mapping for each service.
     *
     * @param p          the principal
     * @param attributes the attributes
     * @return the released by default attributes
     */
    protected Map<String, Object> getReleasedByDefaultAttributes(final Principal p, final Map<String, Object> attributes) {
        final ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
        if (ctx != null) {
            LOGGER.debug("Located application context. Retrieving default attributes for release, if any");
            final CasConfigurationProperties props = ctx.getAutowireCapableBeanFactory().getBean(CasConfigurationProperties.class);
            final Set<String> defaultAttrs = props.getAuthn().getAttributeRepository().getDefaultAttributesToRelease();
            LOGGER.debug("Default attributes for release are: {}", defaultAttrs);

            final Map<String, Object> defaultAttributesToRelease = newTreeMap(String.CASE_INSENSITIVE_ORDER);
            defaultAttrs.stream().forEach(key -> {
                if (attributes.containsKey(key)) {
                    LOGGER.debug("Found and added default attribute for release: {}", key);
                    defaultAttributesToRelease.put(key, attributes.get(key));
                }
            });
            return defaultAttributesToRelease;
        }

        return Maps.newTreeMap();
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
                .append("attributeFilter", this.registeredServiceAttributeFilter)
                .append("principalAttributesRepository", this.principalAttributesRepository)
                .append("authorizedToReleaseCredentialPassword", this.authorizedToReleaseCredentialPassword)
                .append("authorizedToReleaseProxyGrantingTicket", this.authorizedToReleaseProxyGrantingTicket)
                .toString();
    }
}


