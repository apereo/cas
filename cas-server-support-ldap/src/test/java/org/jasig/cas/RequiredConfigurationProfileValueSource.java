/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.ProfileValueSource;

/**
 * Provides a mechanism to enable LDAP tests if required configuration files are found in LDAP module root directory.
 * Sets the following property values to "true" if necessary configuration files are found:
 *
 * <ol>
 *     <li>authenticationConfig</li>
 *     <li>monitorConfig</li>
 *     <li>userDetailsConfig</li>
 * </ol>
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class RequiredConfigurationProfileValueSource implements ProfileValueSource {

    private static final Logger LOG = LoggerFactory.getLogger(RequiredConfigurationProfileValueSource.class);

    private static final String TRUE = "true";

    private static final String FALSE = "false";

    private Map<String, Resource[]> propertyResourceMap = new HashMap<String, Resource[]>();

    public RequiredConfigurationProfileValueSource() {
        final Resource ldaptiveProperties = new FileSystemResource("ldaptive.properties");
        final Resource extraConfig = new FileSystemResource("extraConfigContext.xml");
        this.propertyResourceMap.put(
                "authenticationConfig",
                new Resource[] {
                        ldaptiveProperties,
                        extraConfig,
                        new FileSystemResource("credentials.properties")
                });
        this.propertyResourceMap.put(
                "monitorConfig",
                new Resource[] {
                        ldaptiveProperties,
                        extraConfig
                });
        this.propertyResourceMap.put(
                "userDetailsConfig",
                new Resource[] {
                        ldaptiveProperties,
                        extraConfig,
                        new FileSystemResource("userdetails.properties")
                });
    }

    @Override
    public String get(final String s) {
        final Resource[] resources = this.propertyResourceMap.get(s);
        String result = FALSE;
        if (resources != null) {
            for (Resource res : resources) {
                LOG.debug("Checking for {}", res);
                if (!res.exists()) {
                    LOG.info("Required configuration resource {} not found", res);
                    return result;
                }
            }
            result = TRUE;
        }
        return result;
    }
}
