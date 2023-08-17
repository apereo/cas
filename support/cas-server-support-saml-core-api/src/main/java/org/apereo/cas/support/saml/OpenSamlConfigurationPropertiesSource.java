package org.apereo.cas.support.saml;

import lombok.val;
import org.opensaml.core.config.ConfigurationPropertiesSource;
import javax.annotation.Nullable;
import java.util.Properties;

/**
 * This is {@link OpenSamlConfigurationPropertiesSource}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class OpenSamlConfigurationPropertiesSource implements ConfigurationPropertiesSource {
    @Nullable
    @Override
    public Properties getProperties() {
        val properties = new Properties();
        properties.setProperty("org.apache.xml.security.ignoreLineBreaks", "true");
        properties.setProperty("com.sun.org.apache.xml.internal.security.ignoreLineBreaks", "true");
        properties.setProperty("opensaml.config.xml.unmarshall.strictMode", "false");
        return properties;
    }
}
