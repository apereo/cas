package org.apereo.cas.support.saml.services;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that defines a SAML Attribute and its parts.
 *
 * @author Travis Schmidt
 * @since 6.2.0
 */
@Getter
@Setter
public class SamlIdpAttributeDefinition implements Serializable {

    /**
     * Attribute key form repository this definition maps to.
     */
    private String attribute = StringUtils.EMPTY;

    /**
     * FriendlyName to use when creating a SAML Attribute in response.
     */
    private String friendlyName = StringUtils.EMPTY;

    /**
     * Name to use when creating a SAML Attribute in response.
     */
    private String name = StringUtils.EMPTY;

    /**
     * Script to execute on the retrieved value before setting the value in a response.
     */
    private String script = StringUtils.EMPTY;

    /**
     * Flag that will scope the attribute before setting the value in a response.
     */
    private boolean scoped;

    /**
     * Source of attribute.
     */
    private String source = StringUtils.EMPTY;

    /**
     * Template to create attribute.
     */
    private String template = StringUtils.EMPTY;

    /**
     * sql.
     */
    private String sql = StringUtils.EMPTY;

    /**
     * args.
     */
    private List<String> args = new ArrayList<>();
}
