package org.jasig.cas.support.saml.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.ChainingMetadataResolverCacheLoader;
import org.jasig.cas.util.ResourceUtils;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.springframework.core.io.AbstractResource;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link SamlRegisteredService} is responsible for managing the SAML metadata for a given SP.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
public final class SamlRegisteredService extends RegexRegisteredService {
    private static final long serialVersionUID = 1218757374062931021L;
    private static final long DEFAULT_METADATA_CACHE_EXPIRATION_MINUTES = 30;

    private long metadataCacheExpirationMinutes;
    private String metadataLocation;
    private String requiredAuthenticationContextClass;
    private boolean signAssertions = false;
    private boolean signResponses = true;

    @JsonIgnore
    private SSODescriptor ssoDescriptor;

    @JsonIgnore
    private transient LoadingCache<String, ChainingMetadataResolver> cache;

    /**
     * Instantiates a new Saml registered service.
     */
    public SamlRegisteredService() {
        super();
        setMetadataCacheExpirationMinutes(DEFAULT_METADATA_CACHE_EXPIRATION_MINUTES);
    }

    /**
     * Sets metadata location.
     *
     * @param metadataLocation the metadata location
     */
    public void setMetadataLocation(final String metadataLocation) {
        try {
            this.metadataLocation = metadataLocation;
        } catch (final Exception e) {
            throw new IllegalArgumentException("Metadata location " + metadataLocation + " cannot be determined");
        }
    }

    /**
     * Gets metadata location.
     *
     * @return the metadata location
     */
    public String getMetadataLocation() {
        try {
            return this.metadataLocation;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long getMetadataCacheExpirationMinutes() {
        return metadataCacheExpirationMinutes;
    }

    public boolean isSignAssertions() {
        return signAssertions;
    }

    public void setSignAssertions(final boolean signAssertions) {
        this.signAssertions = signAssertions;
    }

    public boolean isSignResponses() {
        return signResponses;
    }

    public void setSignResponses(final boolean signResponses) {
        this.signResponses = signResponses;
    }

    public String getRequiredAuthenticationContextClass() {
        return requiredAuthenticationContextClass;
    }

    public void setRequiredAuthenticationContextClass(final String requiredAuthenticationContextClass) {
        this.requiredAuthenticationContextClass = requiredAuthenticationContextClass;
    }

    /**
     * Sets metadata cache expiration minutes.
     *
     * @param metadataCacheExpirationMinutes the metadata cache expiration minutes
     */
    public void setMetadataCacheExpirationMinutes(final long metadataCacheExpirationMinutes) {
        this.metadataCacheExpirationMinutes = metadataCacheExpirationMinutes;
        initializeCache();
    }

    @JsonIgnore
    public ChainingMetadataResolver getChainingMetadataResolver() {
        return resolveMetadata();
    }

    /**
     * Resolve metadata chaining metadata resolver.
     *
     * @return the chaining metadata resolver
     */
    private ChainingMetadataResolver resolveMetadata() {
        try {
            return this.cache.get(this.getMetadataLocation());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        throw new IllegalArgumentException("Metadata resolver could not be located from metadata for service "+ getServiceId());
    }


    @Override
    public void copyFrom(final RegisteredService source) {
        super.copyFrom(source);
        try {
            final SamlRegisteredService samlRegisteredService = (SamlRegisteredService) source;
            samlRegisteredService.setMetadataCacheExpirationMinutes(this.metadataCacheExpirationMinutes);
            samlRegisteredService.setMetadataLocation(this.getMetadataLocation());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
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
                .append(this.metadataCacheExpirationMinutes, rhs.getMetadataCacheExpirationMinutes())
                .append(this.metadataLocation, rhs.getMetadataLocation())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 17)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("metadataLocation", this.metadataLocation)
                .append("metadataCacheExpirationMinutes", this.metadataCacheExpirationMinutes)
                .toString();
    }

    private void initializeCache() {
        this.cache = CacheBuilder.newBuilder().maximumSize(1)
                .expireAfterWrite(this.metadataCacheExpirationMinutes, TimeUnit.MINUTES).build(new ChainingMetadataResolverCacheLoader());
    }
}
