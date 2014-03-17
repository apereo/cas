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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.FailedLoginException;

import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0
 */
public class ImmutableAuthenticationTests {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testImmutable() {
        final AuthenticationHandler authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();
        final CredentialMetaData credential1 = new BasicCredentialMetaData(new UsernamePasswordCredential());
        final CredentialMetaData credential2 = new BasicCredentialMetaData(new UsernamePasswordCredential());
        final List<CredentialMetaData> credentials = new ArrayList<CredentialMetaData>();
        credentials.add(credential1);
        credentials.add(credential2);
        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("authenticationMethod", "password");
        final Map<String, HandlerResult> successes = new HashMap<String, HandlerResult>();
        successes.put("handler1", new HandlerResult(authenticationHandler, credential1));
        final Map<String, Class<? extends Exception>> failures = new HashMap<String, Class<? extends Exception>>();
        failures.put("handler2", FailedLoginException.class);
        final ImmutableAuthentication auth = new ImmutableAuthentication(
                new Date(),
                credentials,
                new SimplePrincipal("test"),
                attributes,
                successes,
                failures);
        try {
            auth.getAuthenticatedDate().setTime(100);
            fail("Should have failed");
        } catch (final RuntimeException e) {
            logger.debug("Setting authenticate date/time failed correctly");
        }
        try {
            auth.getCredentials().add(new BasicCredentialMetaData(new UsernamePasswordCredential()));
            fail("Should have failed");
        } catch (final RuntimeException e) {
            logger.debug("Adding authentication credential metadata failed correctly");
        }
        try {
            auth.getSuccesses().put("test", new HandlerResult(authenticationHandler, credential1));
            fail("Should have failed");
        } catch (final RuntimeException e) {
            logger.debug("Adding authentication success event failed correctly");
        }
        try {
            auth.getFailures().put("test", FailedLoginException.class);
            fail("Should have failed");
        } catch (final RuntimeException e) {
            logger.debug("Adding authentication failure event failed correctly");
        }
    }
}
