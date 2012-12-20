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
package org.jasig.cas.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import org.springframework.core.io.Resource;

/**
 * Class designed to read data from a file in the format of USERNAME SEPARATOR
 * PASSWORD that will go line by line and look for the username. If it finds the
 * username it will compare the supplied password (first put through a
 * PasswordTranslator) that is compared to the password provided in the file. If
 * there is a match, the user is authenticated. Note that the default password
 * translator is a plaintext password translator and the default separator is
 * "::" (without quotes).
 * 
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0
 */
public class FileAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    /** The default separator in the file. */
    private static final String DEFAULT_SEPARATOR = "::";

    /** The separator to use. */
    @NotNull
    private String separator = DEFAULT_SEPARATOR;

    /** The filename to read the list of usernames from. */
    @NotNull
    private Resource fileName;

    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credentials)
            throws GeneralSecurityException, IOException {

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(this.fileName.getInputStream()));
            String line = bufferedReader.readLine();
            while (line != null) {
                final String[] lineFields = line.split(this.separator);
                final String userName = lineFields[0];
                final String password = lineFields[1];
                final String transformedUsername = getPrincipalNameTransformer().transform(credentials.getUsername());
                if (transformedUsername.equals(userName)) {
                    log.debug("Found matching username {}", userName);
                    if (this.getPasswordEncoder().encode(credentials.getPassword()).equals(password)) {
                        return new HandlerResult(this, new SimplePrincipal(transformedUsername));
                    }
                    break;
                }
                line = bufferedReader.readLine();
            }
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (final IOException e) {
                log.warn("Error closing " + this.fileName);
            }
        }
        throw new FailedLoginException();
    }

    /**
     * @param fileName The fileName to set.
     */
    public final void setFileName(final Resource fileName) {
        this.fileName = fileName;
    }

    /**
     * @param separator The separator to set.
     */
    public final void setSeparator(final String separator) {
        this.separator = separator;
    }
}
