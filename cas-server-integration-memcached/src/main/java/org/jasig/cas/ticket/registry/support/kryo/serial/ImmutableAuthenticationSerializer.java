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
package org.jasig.cas.ticket.registry.support.kryo.serial;

import java.util.Date;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

/**
 * Serializer for {@link ImmutableAuthentication}.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public final class ImmutableAuthenticationSerializer extends AbstractAuthenticationSerializer<ImmutableAuthentication> {

    private final FieldHelper fieldHelper;

    public ImmutableAuthenticationSerializer(final Kryo kryo, final FieldHelper helper) {
        super(kryo);
        this.fieldHelper = helper;
    }

    protected ImmutableAuthentication createAuthentication(
            final Date authDate, final Principal principal, final Map<String, Object> attributes) {
        final ImmutableAuthentication auth = new ImmutableAuthentication(principal, attributes);
        fieldHelper.setFieldValue(auth, "authenticatedDate", authDate);
        return auth;
    }
}
