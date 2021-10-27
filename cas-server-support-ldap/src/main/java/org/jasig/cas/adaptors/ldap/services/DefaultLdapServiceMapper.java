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

import org.jasig.cas.adaptors.ldap.util.SpringLdapUtils;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

/**
 * Default implementation of LdapServiceMapper which maps each property of RegisteredService to a explicit LDAP attribute
 *
 * @author Siegfried Puchbauer, SPP (http://www.spp.at/)
 */
public final class DefaultLdapServiceMapper extends LdapServiceMapper {

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
    private String serviceAllowedAttributesAttribute = "casAllowedAttributes";


    protected RegisteredService doMapFromContext(final DirContextOperations ctx) {
        RegisteredServiceImpl s = new RegisteredServiceImpl();

        s.setId(Long.parseLong(ctx.getStringAttribute(this.idAttribute)));
        s.setServiceId(ctx.getStringAttribute(this.serviceIdAttribute));
        s.setName(ctx.getStringAttribute(this.namingAttribute));
        s.setEnabled(SpringLdapUtils.getBoolean(ctx, this.serviceEnabledAttribute));
        s.setAllowedToProxy(SpringLdapUtils.getBoolean(ctx, this.serviceAllowedToProxyAttribute));
        s.setAnonymousAccess(SpringLdapUtils.getBoolean(ctx, this.serviceAnonymousAccessAttribute));
        s.setDescription(ctx.getStringAttribute(this.serviceDescriptionAttribute));
        s.setSsoEnabled(SpringLdapUtils.getBoolean(ctx, this.serviceSsoEnabledAttribute));
        s.setTheme(ctx.getStringAttribute(this.serviceThemeAttribute));

        final String[] attributes = ctx.getStringAttributes(this.serviceAllowedAttributesAttribute);

        if (attributes != null) {
 	 	    s.setAllowedAttributes(Arrays.asList(attributes));
        }

        return s;
    }

    protected DirContextAdapter doMapToContext(final RegisteredService service,final  DirContextAdapter ctx) {
        ctx.setAttributeValue(this.idAttribute, String.valueOf(service.getId()));
        ctx.setAttributeValue(this.serviceIdAttribute, service.getServiceId());
        SpringLdapUtils.setBoolean(ctx, this.serviceEnabledAttribute, service.isEnabled());
        SpringLdapUtils.setBoolean(ctx, this.serviceAllowedToProxyAttribute, service.isAllowedToProxy());
        SpringLdapUtils.setBoolean(ctx, this.serviceAnonymousAccessAttribute, service.isAnonymousAccess());
        SpringLdapUtils.setBoolean(ctx, this.serviceSsoEnabledAttribute, service.isSsoEnabled());
        ctx.setAttributeValue(this.serviceThemeAttribute, service.getTheme());
        ctx.setAttributeValues(this.serviceAllowedAttributesAttribute, service.getAllowedAttributes().toArray(new String[service.getAllowedAttributes().size()]), false);
        ctx.setAttributeValue(this.serviceDescriptionAttribute, service.getDescription());
        if (!SpringLdapUtils.containsObjectClass(ctx, this.objectclass))
            ctx.addAttributeValue(SpringLdapUtils.OBJECTCLASS_ATTRIBUTE, this.objectclass);

        return ctx;
    }

    protected DirContextAdapter createCtx(final String parentDn, final RegisteredService service) {
        DistinguishedName dn = new DistinguishedName(parentDn);
        dn.add(this.namingAttribute, service.getName());
        return new DirContextAdapter(dn);
    }

    protected Filter getSearchFilter(final Long id) {
        return new AndFilter().and(getLoadFilter()).and(new EqualsFilter(this.idAttribute, String.valueOf(id)));
    }

    protected Filter getLoadFilter() {
        return new EqualsFilter(SpringLdapUtils.OBJECTCLASS_ATTRIBUTE, this.objectclass);
    }

    public void setObjectclass(final String objectclass) {
        this.objectclass = objectclass;
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
}
