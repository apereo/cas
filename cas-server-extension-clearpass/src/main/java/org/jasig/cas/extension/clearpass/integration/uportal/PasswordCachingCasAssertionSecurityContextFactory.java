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

package org.jasig.cas.extension.clearpass.integration.uportal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.ISecurityContextFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 1.0.0.GA
 */
public final class PasswordCachingCasAssertionSecurityContextFactory implements ISecurityContextFactory {

    private final static String DEFAULT_PORTAL_SECURITY_PROPERTY_FILE = "properties/security.properties";

    private final static String CLEARPASS_CAS_URL_PROPERTY = PasswordCachingCasAssertionSecurityContextFactory.class.getName()+".clearPassCasUrl";

    private final Log log = LogFactory.getLog(getClass());

    private final String clearPassUrl;

    public PasswordCachingCasAssertionSecurityContextFactory() {
        final Resource resource = new ClassPathResource(DEFAULT_PORTAL_SECURITY_PROPERTY_FILE, getClass().getClassLoader());
        final Properties securityProperties = new Properties();
        InputStream inputStream = null;

        try {
            inputStream = resource.getInputStream();
            securityProperties.load(inputStream);
            this.clearPassUrl = securityProperties.getProperty(CLEARPASS_CAS_URL_PROPERTY);          
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    //nothing to do
                }
            }
        }
    }

    public ISecurityContext getSecurityContext() {
        if (CommonUtils.isNotBlank(this.clearPassUrl)) {
            return new PasswordCachingCasAssertionSecurityContext(this.clearPassUrl);
        }
        
        throw new IllegalStateException(String.format("clearPassUrl not configured.  Cannot create an instance of [%s] without it.", getClass().getSimpleName()));
    }
}
