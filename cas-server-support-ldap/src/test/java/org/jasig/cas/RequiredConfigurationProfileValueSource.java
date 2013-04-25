package org.jasig.cas;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.ProfileValueSource;

/**
 * Provides a mechanism to enable LDAP tests if required configuration files are found in LDAP module root directory.
 * Sets the following property values to "true" if necessary configuration files are found:
 *
 * <ol>
 *     <li>authenticationConfig</li>
 * </ol>
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class RequiredConfigurationProfileValueSource implements ProfileValueSource {

    private static final String TRUE = "true";

    private static final String FALSE = "false";

    private Map<String, Resource[]> propertyResourceMap = new HashMap<String, Resource[]>();

    public RequiredConfigurationProfileValueSource() {
        final Resource ldaptiveProperties = new FileSystemResource("ldaptive.properties");
        final Resource extraConfig = new FileSystemResource("extraConfigContext.xml");
        propertyResourceMap.put(
                "authenticationConfig",
                new Resource[] {
                        ldaptiveProperties,
                        new FileSystemResource("credentials.properties"),
                        extraConfig
                });
    }

    @Override
    public String get(final String s) {
        final Resource[] resources = propertyResourceMap.get(s);
        String result = FALSE;
        if (resources != null) {
            for (Resource res : resources) {
                if (!res.exists()) {
                    return result;
                }
            }
            result = TRUE;
        }
        return result;
    }
}
