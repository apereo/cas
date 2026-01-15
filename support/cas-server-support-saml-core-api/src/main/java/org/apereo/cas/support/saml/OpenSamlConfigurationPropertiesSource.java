package org.apereo.cas.support.saml;

import module java.base;
import lombok.val;
import org.opensaml.core.config.ConfigurationProperties;
import org.opensaml.core.config.ConfigurationPropertiesSource;
import org.opensaml.core.config.provider.PropertiesAdapter;

/**
 * This is {@link OpenSamlConfigurationPropertiesSource}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class OpenSamlConfigurationPropertiesSource implements ConfigurationPropertiesSource {

    /**
     * Configuration property that controls strict mode parsing of XML payloads.
     * Strict mode typically rejects all unknown (unsupported by OpenSAML) XML tags and attributes.
     */
    public static final String CONFIG_STRICT_MODE = "opensaml.config.xml.unmarshall.strictMode";
    /**
     * Configuration property that controls whether line breaks particularly in certificates should be ignored.
     */
    public static final String CONFIG_SUN_XML_IGNORE_LINEBREAKS = "com.sun.org.apache.xml.internal.security.ignoreLineBreaks";
    /**
     * Configuration property that controls whether line breaks particularly in certificates should be ignored.
     */
    public static final String CONFIG_APACHE_XML_IGNORE_LINEBREAKS = "org.apache.xml.security.ignoreLineBreaks";

    static {
        System.setProperty(CONFIG_STRICT_MODE, "true");
        System.setProperty(CONFIG_SUN_XML_IGNORE_LINEBREAKS, "true");
        System.setProperty(CONFIG_APACHE_XML_IGNORE_LINEBREAKS, "true");
    }
    
    @Override
    public ConfigurationProperties getProperties() {
        val properties = new Properties();
        properties.setProperty(CONFIG_APACHE_XML_IGNORE_LINEBREAKS, "true");
        properties.setProperty(CONFIG_SUN_XML_IGNORE_LINEBREAKS, "true");
        properties.setProperty(CONFIG_STRICT_MODE, "false");
        return new PropertiesAdapter(properties);
    }
}
