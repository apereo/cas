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
package org.jasig.cas.adaptors.ldap.services;

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.util.LdapUtils;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Default implementation of {@link LdapRegisteredServiceMapper} that is able
 * to map ldap entries to {@link RegisteredService} instances based on
 * certain attributes names. This implementation also respects the object class
 * attribute of LDAP entries via {@link LdapUtils#OBJECTCLASS_ATTRIBUTE}.
 * @author Misagh Moayyed
 */
public final class DefaultLdapServiceMapper implements LdapRegisteredServiceMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLdapServiceMapper.class);

    @NotNull
    private String objectClass = "casRegisteredService";

    @NotNull
    private String serviceIdAttribute = "casServiceUrlPattern";

    @NotNull
    private String idAttribute = "uid";

    @NotNull
    private String serviceDescriptionAttribute = "description";

    @NotNull
    private String serviceNameAttribute = "cn";

    @NotNull
    private String serviceEnabledAttribute = "casServiceEnabled";

    @NotNull
    private String serviceSsoEnabledAttribute = "casServiceSsoEnabled";

    @NotNull
    private String serviceAnonymousAccessAttribute = "casServiceAnonymousAccess";

    @NotNull
    private String serviceAllowedToProxyAttribute = "casServiceAllowedToProxy";

    @NotNull
    private String serviceThemeAttribute = "casServiceTheme";

    @NotNull
    private String usernameAttribute = "casUsernameAttribute";

    @NotNull
    private String serviceAllowedAttributesAttribute = "casAllowedAttributes";

    @NotNull
    private String ignoreAttributesAttribute = "casIgnoreAttributes";

    @NotNull
    private String evaluationOrderAttribute = "casEvaluationOrder";

    @NotNull
    private String requiredHandlersAttribute = "casRequiredHandlers";

    @Override
    public LdapEntry mapFromRegisteredService(final String dn, final RegisteredService svc) {


        if (svc.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            ((AbstractRegisteredService) svc).setId(System.nanoTime());
        }
        final String newDn = getDnForRegisteredService(dn, svc);
        LOGGER.debug("Creating entry {}", newDn);

        final Collection<LdapAttribute> attrs = new ArrayList<LdapAttribute>();
        attrs.add(new LdapAttribute(this.idAttribute, String.valueOf(svc.getId())));
        attrs.add(new LdapAttribute(this.serviceIdAttribute, svc.getServiceId()));
        attrs.add(new LdapAttribute(this.serviceNameAttribute, svc.getName()));
        attrs.add(new LdapAttribute(this.serviceDescriptionAttribute, svc.getDescription()));
        attrs.add(new LdapAttribute(this.serviceEnabledAttribute, Boolean.toString(svc.isEnabled()).toUpperCase()));
        attrs.add(new LdapAttribute(this.serviceAllowedToProxyAttribute, Boolean.toString(svc.isAllowedToProxy()).toUpperCase()));
        attrs.add(new LdapAttribute(this.serviceAnonymousAccessAttribute, Boolean.toString(svc.isAnonymousAccess()).toUpperCase()));
        attrs.add(new LdapAttribute(this.serviceSsoEnabledAttribute, Boolean.toString(svc.isSsoEnabled()).toUpperCase()));
        attrs.add(new LdapAttribute(this.ignoreAttributesAttribute, Boolean.toString(svc.isAnonymousAccess()).toUpperCase()));
        attrs.add(new LdapAttribute(this.evaluationOrderAttribute, String.valueOf(svc.getEvaluationOrder())));
        attrs.add(new LdapAttribute(this.serviceThemeAttribute, svc.getTheme()));
        attrs.add(new LdapAttribute(this.usernameAttribute, svc.getUsernameAttribute()));

        if (svc.getAllowedAttributes().size() > 0) {
            attrs.add(new LdapAttribute(this.serviceAllowedAttributesAttribute, svc.getAllowedAttributes().toArray(new String[] {})));
        }

        if (svc.getRequiredHandlers().size() > 0) {
            attrs.add(new LdapAttribute(this.requiredHandlersAttribute, svc.getRequiredHandlers().toArray(new String[] {})));
        }

        attrs.add(new LdapAttribute(LdapUtils.OBJECTCLASS_ATTRIBUTE, this.objectClass));

        return new LdapEntry(newDn, attrs);
    }

    @Override
    public RegisteredService mapToRegisteredService(final LdapEntry entry) {

        final LdapAttribute attr = entry.getAttribute(this.serviceIdAttribute);

        if (attr != null) {
            final AbstractRegisteredService s = getRegisteredService(attr.getStringValue());

            if (s != null) {
                s.setId(LdapUtils.getLong(entry, this.idAttribute, Long.valueOf(entry.getDn().hashCode())));

                s.setServiceId(LdapUtils.getString(entry, this.serviceIdAttribute));
                s.setName(LdapUtils.getString(entry, this.serviceNameAttribute));
                s.setDescription(LdapUtils.getString(entry, this.serviceDescriptionAttribute));
                s.setEnabled(LdapUtils.getBoolean(entry, this.serviceEnabledAttribute));
                s.setTheme(LdapUtils.getString(entry, this.serviceThemeAttribute));
                s.setEvaluationOrder(LdapUtils.getLong(entry, this.evaluationOrderAttribute).intValue());
                s.setUsernameAttribute(LdapUtils.getString(entry, this.usernameAttribute));
                s.setAllowedToProxy(LdapUtils.getBoolean(entry, this.serviceAllowedToProxyAttribute));
                s.setAnonymousAccess(LdapUtils.getBoolean(entry, this.serviceAnonymousAccessAttribute));
                s.setSsoEnabled(LdapUtils.getBoolean(entry, this.serviceSsoEnabledAttribute));
                s.setAllowedAttributes(new ArrayList<String>(getMultiValuedAttributeValues(entry, this.serviceAllowedAttributesAttribute)));
                s.setIgnoreAttributes(LdapUtils.getBoolean(entry, this.ignoreAttributesAttribute));
                s.setRequiredHandlers(new HashSet<String>(getMultiValuedAttributeValues(entry, this.requiredHandlersAttribute)));
            }
            return s;
        }
        return null;
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

    public void setServiceIdAttribute(final String serviceIdAttribute) {
        this.serviceIdAttribute = serviceIdAttribute;
    }

    public void setServiceDescriptionAttribute(final String serviceDescriptionAttribute) {
        this.serviceDescriptionAttribute = serviceDescriptionAttribute;
    }

    public void setServiceNameAttribute(final String serviceNameAttribute) {
        this.serviceNameAttribute = serviceNameAttribute;
    }

    public void setServiceEnabledAttribute(final String serviceEnabledAttribute) {
        this.serviceEnabledAttribute = serviceEnabledAttribute;
    }

    public void setServiceSsoEnabledAttribute(final String serviceSsoEnabledAttribute) {
        this.serviceSsoEnabledAttribute = serviceSsoEnabledAttribute;
    }

    public void setServiceAnonymousAccessAttribute(final String serviceAnonymousAccessAttribute) {
        this.serviceAnonymousAccessAttribute = serviceAnonymousAccessAttribute;
    }

    public void setServiceAllowedToProxyAttribute(final String serviceAllowedToProxyAttribute) {
        this.serviceAllowedToProxyAttribute = serviceAllowedToProxyAttribute;
    }

    public void setServiceThemeAttribute(final String serviceThemeAttribute) {
        this.serviceThemeAttribute = serviceThemeAttribute;
    }

    public void setServiceAllowedAttributesAttribute(final String serviceAllowedAttributesAttribute) {
        this.serviceAllowedAttributesAttribute = serviceAllowedAttributesAttribute;
    }

    public void setIgnoreAttributesAttribute(final String ignoreAttributesAttribute) {
        this.ignoreAttributesAttribute = ignoreAttributesAttribute;
    }

    public void setRequiredHandlersAttribute(final String handlers) {
        this.requiredHandlersAttribute = handlers;
    }


    public void setUsernameAttribute(final String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }

    public void setEvaluationOrderAttribute(final String evaluationOrderAttribute) {
        this.evaluationOrderAttribute = evaluationOrderAttribute;
    }

    @Override
    public String getDnForRegisteredService(final String parentDn, final RegisteredService svc) {
        return String.format("%s=%s,%s", this.idAttribute, svc.getId(), parentDn);
    }

    private boolean isValidRegexPattern(final String pattern) {
        try {
            Pattern.compile(pattern);
        } catch (final PatternSyntaxException e) {
            LOGGER.debug("Failed to identify [{}] as a regular expression", pattern);
            return false;
        }
        return true;
    }

    private Collection<String> getMultiValuedAttributeValues(@NotNull final LdapEntry entry, @NotNull final String attrName) {
        final LdapAttribute attrs = entry.getAttribute(attrName);
        if (attrs != null) {
            return attrs.getStringValues();
        }
        return Collections.emptyList();
    }

    private AbstractRegisteredService getRegisteredService(@NotNull final String id) {
        if (isValidRegexPattern(id)) {
            return new RegexRegisteredService();
        }

        if (new AntPathMatcher().isPattern(id)) {
            return new RegisteredServiceImpl();
        }
        return null;
    }
}
