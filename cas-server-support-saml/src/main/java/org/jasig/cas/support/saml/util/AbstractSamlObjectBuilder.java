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

import org.opensaml.Configuration;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.saml1.core.AttributeValue;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;

/**
 * An abstract response builder to serve as the template
 * for SAML1 and SAML2 responses.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
public abstract class AbstractSamlObjectBuilder {

    private static final String DEFAULT_ELEMENT_NAME_FIELD = "DEFAULT_ELEMENT_NAME";

    /** Logger instance. **/
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Create a new SAML object.
     *
     * @param <T> the generic type
     * @param objectType the object type
     * @return the t
     */
    public final <T extends SAMLObject> T newSamlObject(final Class<T> objectType) {
        final QName qName;
        try {
            final Field f = objectType.getField(DEFAULT_ELEMENT_NAME_FIELD);
            qName = (QName) f.get(null);
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException("Cannot find field " + objectType.getName() + "." + DEFAULT_ELEMENT_NAME_FIELD);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Cannot access field " + objectType.getName() + "." + DEFAULT_ELEMENT_NAME_FIELD);
        }
        final SAMLObjectBuilder<T> builder = (SAMLObjectBuilder<T>) Configuration.getBuilderFactory().getBuilder(qName);
        if (builder == null) {
            throw new IllegalStateException("No SAMLObjectBuilder registered for class " + objectType.getName());
        }
        return objectType.cast(builder.buildObject());
    }


    /**
     * New attribute value.
     *
     * @param value the value
     * @return the xS string
     */
    protected final XSString newAttributeValue(final Object value) {
        final XSStringBuilder attrValueBuilder = new XSStringBuilder();
        final XSString stringValue = attrValueBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        if (value instanceof String) {
            stringValue.setValue((String) value);
        } else {
            stringValue.setValue(value.toString());
        }
        return stringValue;
    }
}
