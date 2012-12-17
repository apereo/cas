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

import java.util.Arrays;
import java.util.Collection;

import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link FileAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 */
@RunWith(Parameterized.class)
public class FileAuthenticationHandlerTests {

    private FileAuthenticationHandler authenticationHandler;

    private UsernamePasswordCredential credential;

    boolean expected;

    public FileAuthenticationHandlerTests(
            final FileAuthenticationHandler handler,
            final UsernamePasswordCredential credential,
            final boolean expected) {
        this.authenticationHandler = handler;
        this.credential = credential;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> generateData() throws Exception {

        final FileAuthenticationHandler handlerDefaultSep = new FileAuthenticationHandler();
        handlerDefaultSep.setFileName(new ClassPathResource("/authentication.txt"));

        final FileAuthenticationHandler handlerCommaSep = new FileAuthenticationHandler();
        handlerCommaSep.setFileName(new ClassPathResource("/authentication2.txt"));
        handlerCommaSep.setSeparator(",");

        final FileAuthenticationHandler handlerNoFile = new FileAuthenticationHandler();

        return Arrays.asList(new Object[][]{
                {handlerDefaultSep, newCredential("scott", "rutgers"), true},
                {handlerDefaultSep, newCredential("scott", "Rutgers"), false},
                {handlerDefaultSep, newCredential("scott", "invalid"), false},
                {handlerDefaultSep, newCredential(null, "nomatter"), false},
                {handlerCommaSep, newCredential("bill", "YES"), true},
                {handlerCommaSep, newCredential("bill", "yes"), false},
                {handlerNoFile , newCredential("scott", "rutgers"), false},
        });
    }

    @Test
    public void testAuthenticate() {
        boolean success;
        try {
            this.authenticationHandler.authenticate(this.credential);
            success = true;
        } catch (Exception e) {
            success = false;
        }
        assertEquals(expected, success);
    }



    private static UsernamePasswordCredential newCredential(final String user, final String password) {
        final UsernamePasswordCredential credential = new UsernamePasswordCredential();
        credential.setUsername(user);
        credential.setPassword(password);
        return credential;
    }
}