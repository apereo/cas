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
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Misagh Moayyed
 */
public final class DefaultLdapServiceMapper implements LdapRegisteredServiceMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLdapServiceMapper.class);

    @NotNull
    private String objectclass = "casService";

    @NotNull
    private String serviceIdAttribute = "casServiceUrlPattern";

    @NotNull
    private String idAttribute = "casServiceId";

    @NotNull
    private String serviceDescriptionAttribute = "description";

    @NotNull
    private String namingAttribute = "cn";

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
    private String evaluationOrderAttribute = "casEvaluationOrder";

    @Override
    public LdapEntry mapFromRegisteredService(final String dn, final RegisteredService svc) {
        
        final Collection<LdapAttribute> attrs = new LinkedList<LdapAttribute>();
        attrs.add(new LdapAttribute(this.idAttribute, String.valueOf(svc.getId())));
        attrs.add(new LdapAttribute(this.evaluationOrderAttribute, String.valueOf(svc.getEvaluationOrder())));

        attrs.add(new LdapAttribute(this.serviceEnabledAttribute, Boolean.toString(svc.isEnabled())));
        attrs.add(new LdapAttribute(this.serviceAllowedToProxyAttribute, Boolean.toString(svc.isAllowedToProxy())));
        attrs.add(new LdapAttribute(this.serviceAnonymousAccessAttribute, Boolean.toString(svc.isAnonymousAccess())));
        attrs.add(new LdapAttribute(this.serviceSsoEnabledAttribute, Boolean.toString(svc.isSsoEnabled())));

        attrs.add(new LdapAttribute(this.serviceIdAttribute, svc.getServiceId()));

        attrs.add(new LdapAttribute(this.serviceThemeAttribute, svc.getTheme()));
        attrs.add(new LdapAttribute(this.serviceDescriptionAttribute, svc.getDescription()));
        attrs.add(new LdapAttribute(this.usernameAttribute, svc.getUsernameAttribute()));

        attrs.add(new LdapAttribute(this.serviceAllowedAttributesAttribute, svc.getAllowedAttributes().toArray(new String[] {})));
        attrs.add(new LdapAttribute(LdapUtils.OBJECTCLASS_ATTRIBUTE, this.objectclass));

        return new LdapEntry(dn, attrs);
    }

    @Override
    public RegisteredService mapToRegisteredService(final LdapEntry entry) {

        final AbstractRegisteredService s = getRegisteredService(entry.getAttribute(this.idAttribute).getStringValue());

        if (s != null) {
            s.setId(LdapUtils.getLong(entry, this.idAttribute, Long.valueOf(entry.getDn().hashCode())));

            s.setServiceId(LdapUtils.getString(entry, this.serviceIdAttribute));
            s.setName(LdapUtils.getString(entry, this.namingAttribute));
            s.setDescription(LdapUtils.getString(entry, this.serviceDescriptionAttribute));
            s.setTheme(LdapUtils.getString(entry, this.serviceThemeAttribute));

            s.setEvaluationOrder(LdapUtils.getLong(entry, this.evaluationOrderAttribute).intValue());
            s.setUsernameAttribute(LdapUtils.getString(entry, this.usernameAttribute));

            s.setEnabled(LdapUtils.getBoolean(entry, this.serviceEnabledAttribute));
            s.setAllowedToProxy(LdapUtils.getBoolean(entry, this.serviceAllowedToProxyAttribute));
            s.setAnonymousAccess(LdapUtils.getBoolean(entry, this.serviceAnonymousAccessAttribute));
            s.setSsoEnabled(LdapUtils.getBoolean(entry, this.serviceSsoEnabledAttribute));

            final LdapAttribute attrs = entry.getAttribute(this.serviceAllowedAttributesAttribute);
            if (attrs != null) {
                final Collection<String> attributes = attrs.getStringValues();
                s.setAllowedAttributes(new LinkedList<String>(attributes));
            }
        }

        return s;
    }

    public void setServiceIdAttribute(final String serviceIdAttribute) {
        this.serviceIdAttribute = serviceIdAttribute;
    }

    public void setIdAttribute(final String idAttribute) {
        this.idAttribute = idAttribute;
    }

    public void setServiceDescriptionAttribute(final String serviceDescriptionAttribute) {
        this.serviceDescriptionAttribute = serviceDescriptionAttribute;
    }

    public void setNamingAttribute(final String namingAttribute) {
        this.namingAttribute = namingAttribute;
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

    private boolean isValidRegexPattern(final String pattern) {
        try {
            Pattern.compile(pattern);
        } catch (final PatternSyntaxException e) {
            LOGGER.debug("Failed to identify [{}] as a regular expression. Falling back to ant patterns", pattern);
            return new AntPathMatcher().isPattern(pattern);
        }
        return true;
    }

    private AbstractRegisteredService getRegisteredService(final String id) {
        if (isValidRegexPattern(id)) {
            return new RegexRegisteredService();
        }
        return new RegisteredServiceImpl();
    }

    public final void setUsernameAttribute(final String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }

    public final void setEvaluationOrderAttribute(final String evaluationOrderAttribute) {
        this.evaluationOrderAttribute = evaluationOrderAttribute;
    }
}
