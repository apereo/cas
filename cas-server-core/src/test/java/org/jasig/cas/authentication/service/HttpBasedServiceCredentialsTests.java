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
package org.jasig.cas.authentication.service;

import java.net.URL;

import junit.framework.TestCase;
import org.jasig.cas.TestUtils;
import org.junit.Assert;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpBasedServiceCredentialsTests extends TestCase {

    public void testProperUrl() {
        assertEquals(TestUtils.CONST_GOOD_URL, TestUtils
            .getHttpBasedServiceCredentials().getCallbackUrl().toExternalForm());
    }
    
    public void testEqualsWithNull() throws Exception {
        final HttpBasedServiceCredential c = new HttpBasedServiceCredential(new URL("http://www.cnn.com"));
        
        Assert.assertFalse(c.equals(null));
    }
    
    public void testEqualsWithFalse() throws Exception {
        final HttpBasedServiceCredential c = new HttpBasedServiceCredential(new URL("http://www.cnn.com"));
        final HttpBasedServiceCredential c2 = new HttpBasedServiceCredential(new URL("http://www.msn.com"));
        
        Assert.assertFalse(c.equals(c2));
        Assert.assertFalse(c.equals(new Object()));
    }
    
    public void testEqualsWithTrue() throws Exception {
        final HttpBasedServiceCredential c = new HttpBasedServiceCredential(new URL("http://www.cnn.com"));
        final HttpBasedServiceCredential c2 = new HttpBasedServiceCredential(new URL("http://www.cnn.com"));
        
        Assert.assertTrue(c.equals(c2));
        Assert.assertTrue(c2.equals(c));
    }
}