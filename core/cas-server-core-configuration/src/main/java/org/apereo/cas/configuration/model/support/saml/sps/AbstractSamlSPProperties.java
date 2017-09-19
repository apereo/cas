package org.apereo.cas.configuration.model.support.saml.sps;

import org.apereo.cas.configuration.support.RequiredProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link AbstractSamlSPProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractSamlSPProperties implements Serializable {
    private static final long serialVersionUID = -5381463661659831898L;
    /**
     * The location of the metadata for this service provider.
     * Can be a URL or another form of resource.
     */
    @RequiredProperty
    private String metadata;
    /**
     * Name of this service provider.
     */
    private String name = this.getClass().getSimpleName();
    /**
     * Description of this service provider as it's stored in the registry.
     */
    private String description = this.getClass().getSimpleName().concat(" SAML SP Integration");

    /**
     * Attribute to use when generating nameids for this SP.
     */
    private String nameIdAttribute;
    /**
     * The forced nameId format to use when generating a response.
     */
    private String nameIdFormat;
    /**
     * Set up the attribute release policy for this service.
     * Allow attributes that are to be released to this SP.
     * Attributes should be separated by commas and can be virtually mapped and renamed.
     */
    private List<String> attributes = new ArrayList<>();

    /**
     * Signature location used to verify metadata.
     */
    private String signatureLocation;
    /**
     * List of entityIds allowed for this service provider.
     * Multiple ids can be specified in the event that the metadata is an aggregate.
     */
    private List<String> entityIds = new ArrayList<>();

    /**
     * Indicate whether responses should be signed.
     */
    private boolean signResponses = true;
    /**
     * Indicate whether assertions should be signed.
     */
    private boolean signAssertions;

    public boolean isSignResponses() {
        return signResponses;
    }

    public void setSignResponses(final boolean signResponses) {
        this.signResponses = signResponses;
    }

    public boolean isSignAssertions() {
        return signAssertions;
    }

    public void setSignAssertions(final boolean signAssertions) {
        this.signAssertions = signAssertions;
    }

    public List<String> getEntityIds() {
        return entityIds;
    }

    public void setEntityIds(final List<String> entityIds) {
        this.entityIds = entityIds;
    }

    public String getNameIdFormat() {
        return nameIdFormat;
    }

    public void setNameIdFormat(final String nameIdFormat) {
        this.nameIdFormat = nameIdFormat;
    }

    public String getSignatureLocation() {
        return signatureLocation;
    }

    public void setSignatureLocation(final String signatureLocation) {
        this.signatureLocation = signatureLocation;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(final List<String> attributes) {
        this.attributes = attributes;
    }

    /**
     * Sets attributes.
     *
     * @param attributes the attributes
     */
    public void setAttributes(final String... attributes) {
        setAttributes(Stream.of(attributes).collect(Collectors.toList()));
    }

    public String getNameIdAttribute() {
        return nameIdAttribute;
    }

    public void setNameIdAttribute(final String nameIdAttribute) {
        this.nameIdAttribute = nameIdAttribute;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }
}
