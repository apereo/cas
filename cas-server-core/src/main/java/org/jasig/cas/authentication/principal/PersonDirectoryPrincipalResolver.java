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
package org.jasig.cas.authentication.principal;

import org.jasig.cas.authentication.Credential;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.StubPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves principals by querying a data source using the Jasig
 * <a href="http://developer.jasig.org/projects/person-directory/1.5.0-SNAPSHOT/apidocs/">Person Directory API</a>.
 * The {@link org.jasig.cas.authentication.principal.Principal#getAttributes()} are populated by the results of the
 * query and the principal ID may optionally be set by proving an attribute whose first non-null value is used;
 * otherwise the credential ID is used for the principal ID.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 *
 */
public class PersonDirectoryPrincipalResolver implements PrincipalResolver {

    /** Log instance. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean returnNullIfNoAttributes;

    /** Repository of principal attributes to be retrieved. */
    @NotNull
    private IPersonAttributeDao attributeRepository = new StubPersonAttributeDao(new HashMap<String, List<Object>>());

    /** Factory to create the principal type. **/
    @NotNull
    private PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    /** Optional principal attribute name. */
    private String principalAttributeName;

    @Override
    public boolean supports(final Credential credential) {
        return true;
    }

    @Override
    public final Principal resolve(final Credential credential) {
        logger.debug("Attempting to resolve a principal...");

        String principalId = extractPrincipalId(credential);

        if (principalId == null) {
            logger.debug("Got null for extracted principal ID; returning null.");
            return null;
        }

        logger.debug("Creating SimplePrincipal for [{}]", principalId);

        final IPersonAttributes personAttributes = this.attributeRepository.getPerson(principalId);
        final Map<String, List<Object>> attributes;

        if (personAttributes == null) {
            attributes = null;
        } else {
            attributes = personAttributes.getAttributes();
        }

        if (attributes == null & !this.returnNullIfNoAttributes) {
            return this.principalFactory.createPrincipal(principalId);
        }

        if (attributes == null) {
            return null;
        }

        final Map<String, Object> convertedAttributes = new HashMap<>();
        for (final Map.Entry<String, List<Object>> entry : attributes.entrySet()) {
            final String key = entry.getKey();
            final List<Object> values = entry.getValue();
            if (key.equalsIgnoreCase(this.principalAttributeName)) {
                if (values.isEmpty()) {
                    logger.debug("{} is empty, using {} for principal", this.principalAttributeName, principalId);
                } else {
                    principalId = values.get(0).toString();
                    logger.debug(
                            "Found principal attribute value {}; removing {} from attribute map.",
                            principalId,
                            this.principalAttributeName);
                }
            } else {
                convertedAttributes.put(key, values.size() == 1 ? values.get(0) : values);
            }
        }
        return this.principalFactory.createPrincipal(principalId, convertedAttributes);
    }

    public final void setAttributeRepository(final IPersonAttributeDao attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    public void setReturnNullIfNoAttributes(final boolean returnNullIfNoAttributes) {
        this.returnNullIfNoAttributes = returnNullIfNoAttributes;
    }

    /**
     * Sets the name of the attribute whose first non-null value should be used for the principal ID.
     *
     * @param attribute Name of attribute containing principal ID.
     */
    public void setPrincipalAttributeName(final String attribute) {
        this.principalAttributeName = attribute;
    }

    /**
     * Sets principal factory to create principal objects.
     *
     * @param principalFactory the principal factory
     */
    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }

    /**
     * Extracts the id of the user from the provided credential. This method should be overridded by subclasses to
     * achieve more sophisticated strategies for producing a principal ID from a credential.
     *
     * @param credential the credential provided by the user.
     * @return the username, or null if it could not be resolved.
     */
    protected String extractPrincipalId(final Credential credential) {
        return credential.getId();
    }
}
