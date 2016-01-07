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
package org.jasig.cas.adaptors.ldap.services;

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.util.JsonSerializer;
import org.jasig.cas.util.LdapUtils;
import org.jasig.cas.util.RegexUtils;
import org.jasig.cas.util.services.RegisteredServiceJsonSerializer;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Default implementation of {@link LdapRegisteredServiceMapper} that is able
 * to map ldap entries to {@link RegisteredService} instances based on
 * certain attributes names. This implementation also respects the object class
 * attribute of LDAP entries via {@link LdapUtils#OBJECTCLASS_ATTRIBUTE}.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class DefaultLdapRegisteredServiceMapper implements LdapRegisteredServiceMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLdapRegisteredServiceMapper.class);

    @NotNull
    private JsonSerializer<RegisteredService> jsonSerializer = new RegisteredServiceJsonSerializer();

    @NotNull
    private String objectClass = "casRegisteredService";

    @NotNull
    private String idAttribute = "uid";

    @NotNull
    private String serviceDefinitionAttribute = "description";

    @Override
    public LdapEntry mapFromRegisteredService(final String dn, final RegisteredService svc) {
        try {
            if (svc.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
                ((AbstractRegisteredService) svc).setId(System.nanoTime());
            }
            final String newDn = getDnForRegisteredService(dn, svc);
            LOGGER.debug("Creating entry {}", newDn);

            final Collection<LdapAttribute> attrs = new ArrayList<>();
            attrs.add(new LdapAttribute(this.idAttribute, String.valueOf(svc.getId())));

            final StringWriter writer = new StringWriter();
            this.jsonSerializer.toJson(writer, svc);
            attrs.add(new LdapAttribute(this.serviceDefinitionAttribute, writer.toString()));
            attrs.add(new LdapAttribute(LdapUtils.OBJECTCLASS_ATTRIBUTE, "top", this.objectClass));

            return new LdapEntry(newDn, attrs);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RegisteredService mapToRegisteredService(final LdapEntry entry) {
        try {
            final String value = LdapUtils.getString(entry, this.serviceDefinitionAttribute);
            if (StringUtils.hasText(value)) {
                final RegisteredService service = this.jsonSerializer.fromJson(value);
                return service;
            }

            return null;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getObjectClass() {
        return this.objectClass;
    }

    public void setObjectClass(final String objectClass) {
        this.objectClass = objectClass;
    }

    public String getIdAttribute() {
        return this.idAttribute;
    }

    public void setIdAttribute(final String idAttribute) {
        this.idAttribute = idAttribute;
    }

    public String getServiceDefinitionAttribute() {
        return serviceDefinitionAttribute;
    }

    public void setServiceDefinitionAttribute(final String serviceDefinitionAttribute) {
        this.serviceDefinitionAttribute = serviceDefinitionAttribute;
    }

    public void setJsonSerializer(final JsonSerializer<RegisteredService> jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public String getDnForRegisteredService(final String parentDn, final RegisteredService svc) {
        return String.format("%s=%s,%s", this.idAttribute, svc.getId(), parentDn);
    }

    /**
     * Gets the attribute values if more than one, otherwise an empty list.
     *
     * @param entry the entry
     * @param attrName the attr name
     * @return the collection of attribute values
     */
    private Collection<String> getMultiValuedAttributeValues(@NotNull final LdapEntry entry, @NotNull final String attrName) {
        final LdapAttribute attrs = entry.getAttribute(attrName);
        if (attrs != null) {
            return attrs.getStringValues();
        }
        return Collections.emptyList();
    }

    /**
     * Gets the registered service by id that would either match an ant or regex pattern.
     *
     * @param id the id
     * @return the registered service
     */
    private AbstractRegisteredService getRegisteredService(@NotNull final String id) {
        if (RegexUtils.isValidRegex(id)) {
            return new RegexRegisteredService();
        }

        if (new AntPathMatcher().isPattern(id)) {
            return new RegisteredServiceImpl();
        }
        return null;
    }

}
