package org.apereo.cas.configuration.model.support.saml.sps;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-saml-idp")
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
    private List<String> attributes = new ArrayList<>(0);

    /**
     * Signature location used to verify metadata.
     */
    private String signatureLocation;

    /**
     * List of entityIds allowed for this service provider.
     * Multiple ids can be specified in the event that the metadata is an aggregate.
     */
    private List<String> entityIds = new ArrayList<>(0);

    /**
     * Indicate whether responses should be signed.
     */
    private boolean signResponses = true;

    /**
     * Indicate whether assertions should be signed.
     */
    private boolean signAssertions;

    /**
     * Add attributes.
     *
     * @param attributes the attributes
     */
    protected void addAttributes(final String... attributes) {
        setAttributes(Stream.of(attributes).collect(Collectors.toList()));
    }
}
