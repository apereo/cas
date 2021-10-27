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
package org.jasig.cas.adaptors.generic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

import org.apache.commons.io.IOUtils;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.springframework.core.io.Resource;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

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

    /** {@inheritDoc} */
    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {
        try {
            
            final String username = credential.getUsername();
            final String passwordOnRecord = getPasswordOnRecord(username);
            if (passwordOnRecord == null) {
                throw new AccountNotFoundException(username + " not found in backing file.");
            }
            if (credential.getPassword() != null
                    && this.getPasswordEncoder().encode(credential.getPassword()).equals(passwordOnRecord)) {
                return createHandlerResult(credential, new SimplePrincipal(username), null);
            }
        } catch (final IOException e) {
            throw new PreventedException("IO error reading backing file", e);
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

    private String getPasswordOnRecord(final String username) throws IOException {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(this.fileName.getInputStream()));
            String line = bufferedReader.readLine();
            while (line != null) {
                final String[] lineFields = line.split(this.separator);
                final String userOnRecord = lineFields[0];
                final String passOnRecord = lineFields[1];
                if (username.equals(userOnRecord)) {
                    return passOnRecord;
                }
                line = bufferedReader.readLine();
            }
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
        return null;
    }
}
