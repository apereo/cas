/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

package org.jasig.cas.support.saml.util;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;

/**
 * This is {@link org.jasig.cas.support.saml.util.GoogleSaml20ObjectBuilder} that
 * attempts to build the saml response. QName based on the spec described here:
 * https://developers.google.com/google-apps/sso/saml_reference_implementation_web#samlReferenceImplementationWebSetupChangeDomain
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1.0
 */
public class GoogleSaml20ObjectBuilder extends AbstractSaml20ObjectBuilder {
    @Override
    public final QName getSamlObjectQName(final Class objectType) throws RuntimeException {
        try {
            final Field f = objectType.getField(DEFAULT_ELEMENT_LOCAL_NAME_FIELD);
            final String name = f.get(null).toString();

            if (objectType.equals(Response.class) || objectType.equals(Status.class)
                    || objectType.equals(StatusCode.class)) {
                return new QName(SAMLConstants.SAML20P_NS, name, "samlp");
            }
            return new QName(SAMLConstants.SAML20_NS, name, XMLConstants.DEFAULT_NS_PREFIX);
        } catch (final Exception e){
            throw new IllegalStateException("Cannot access field " + objectType.getName() + '.' + DEFAULT_ELEMENT_LOCAL_NAME_FIELD);
        }
    }
}
