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
package org.jasig.cas.authentication.principal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;

import javax.validation.constraints.NotNull;

/**
 * Generates PersistentIds based on the Shibboleth algorithm.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2007/04/20 19:39:31 $
 * @since 3.1
 */
public final class ShibbolethCompatiblePersistentIdGenerator implements
    PersistentIdGenerator {

    private static final byte CONST_SEPARATOR = (byte) '!';
    
    @NotNull
    private byte[] salt;

    public String generate(final Principal principal, final Service service) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(service.getId().getBytes());
            md.update(CONST_SEPARATOR);
            md.update(principal.getId().getBytes());
            md.update(CONST_SEPARATOR);

            return Base64.encodeBase64String(md.digest(this.salt)).replaceAll(
                System.getProperty("line.separator"), "");
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSalt(final String salt) {
        this.salt = salt.getBytes();
    }
}
