/*
 * Copyright 2002-2004 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.springframework.ldap.core;

import java.util.ArrayList;
import java.util.Collection;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

/**
 * Utility class which is used to read attribute values from SearchResult or Attributes, each method is self documenting through its name.
 * 
 * @author Olivier Jolly
 * @see javax.naming.directory.SearchResult
 * @see javax.naming.directory.Attributes
 */
public class AttributeHelper {

    private AttributeHelper() {
        // make construction private
    }

    public static boolean isInstanceOf(SearchResult searchResult,
        String className) throws NamingException {
        return getAttributeAsStringCollection(searchResult, "objectclass")
            .contains(className);
    }

    public static Collection getAttributeAsStringCollection(
        SearchResult searchResult, String attributeName) throws NamingException {
        return getAttributeAsStringCollection(searchResult.getAttributes(),
            attributeName);
    }

    public static Collection getAttributeAsStringCollection(
        Attributes attributes, String attributeName) throws NamingException {
        Collection result = new ArrayList();
        Attribute attribute = attributes.get(attributeName);

        if (attribute != null) {
            NamingEnumeration namingEnumeration = attribute.getAll();
            try {
                while (namingEnumeration.hasMore()) {
                    result.add(namingEnumeration.next());
                }
            }
            finally {
                namingEnumeration.close();
            }
        }

        return result;
    }

    public static String[] getAttributeAsStringArray(SearchResult searchResult,
        String attributeName) throws NamingException {
        Collection result = getAttributeAsStringCollection(searchResult,
            attributeName);
        return (String[])result.toArray(new String[result.size()]);
    }

    public static String[] getAttributeAsStringArray(Attributes attributes,
        String attributeName) throws NamingException {
        Collection result = getAttributeAsStringCollection(attributes,
            attributeName);
        return (String[])result.toArray(new String[result.size()]);
    }

    public static String getAttributeAsString(SearchResult searchResult,
        String attributeName) throws NamingException {
        return getAttributeAsString(searchResult.getAttributes(), attributeName);
    }

    public static String getAttributeAsString(Attributes attributes,
        String attributeName) throws NamingException {
        Attribute attribute = attributes.get(attributeName);
        if (attribute == null) {
            return null;
        }
        return (String)attribute.get();
    }

    public static String getAttributeAsNonNullString(SearchResult searchResult,
        String attributeName) throws NamingException {
        return getAttributeAsNonNullString(searchResult.getAttributes(),
            attributeName);
    }

    public static String getAttributeAsNonNullString(Attributes attributes,
        String attributeName) throws NamingException {
        String result = getAttributeAsString(attributes, attributeName);
        if (result == null) {
            result = "";
        }
        return result;
    }

    // TODO Add support for binary objects. There shouldn't be any other types
    // available with ldap

}