package org.apereo.cas.configuration.model.support.pac4j;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;

import java.util.List;
import java.util.Map;

/**
 * This is {@link Pac4jSamlClientExtensionsProperties}.
 *
 * @author Luis Faria
 * @since 5.3
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Slf4j
@Getter
@Setter
public class Pac4jSamlClientExtensionsProperties {

    /**
     * The extension XML element namespace URI
     */
    private String namespaceURL;

    /**
     * The extension XML element namespace prefix
     */
    private String namespacePrefix;

    /**
     * The extension XML element name
     */
    private String name;

    /**
     * The extension XML element attributes as a key-value pair where
     * the key will be the attribute name (that must be a QName)
     * and the value is the value of attribute.
     */
    private Map<String, String> attributes;

    /**
     * A list of sub-elements.
     */
    private List<Pac4jSamlClientExtensionsProperties> elements;

    /**
     * The extension XML element text content
     */
    private String textContent;

}
