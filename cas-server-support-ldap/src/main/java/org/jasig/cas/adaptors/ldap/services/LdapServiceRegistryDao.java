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

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServiceRegistryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.*;

import javax.naming.directory.SearchControls;
import javax.naming.directory.ModificationItem;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Implementation of the ServiceRegistryDao interface which stores the services in a LDAP Directory
 *
 * @author Siegfried Puchbauer, SPP (http://www.spp.at)
 * @author Scott Battaglia
 *
 */
public final class LdapServiceRegistryDao implements ServiceRegistryDao {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @NotNull
    private LdapTemplate ldapTemplate;

    @NotNull
    private String serviceBaseDn;

    private boolean ignoreMultipleSearchResults = false;

    @NotNull
    private LdapServiceMapper ldapServiceMapper = new DefaultLdapServiceMapper();

    private final SearchControls cachedSearchControls;

    public LdapServiceRegistryDao() {
        this.cachedSearchControls = new SearchControls();
        this.cachedSearchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    }

    public RegisteredService save(final RegisteredService rs) {
        final RegisteredServiceImpl registeredService = (RegisteredServiceImpl) rs;
        if (registeredService.getId() != -1) {
            return update(registeredService);
        }
        final DirContextAdapter ctx = this.ldapServiceMapper.createCtx(this.serviceBaseDn, registeredService);
        final String dn = ctx.getNameInNamespace();
        registeredService.setId(dn.hashCode());
        this.ldapServiceMapper.doMapToContext(registeredService, ctx);
        this.ldapTemplate.bind(ctx.getNameInNamespace(), ctx, null);
        return registeredService;
    }

    public RegisteredService update(final RegisteredServiceImpl registeredService) {
        final DirContextAdapter ctx = lookupCtx(findDn(this.ldapServiceMapper.getSearchFilter(registeredService.getId()).encode()));
        if (ctx == null) {
            return null;
        }

        this.ldapServiceMapper.doMapToContext(registeredService, ctx);

        final String dn = ctx.getNameInNamespace();
        final ModificationItem[] modItems = ctx.getModificationItems();
        if(log.isDebugEnabled()) {
            log.debug("Attemting to perform modify operations on " + dn);
            for (final ModificationItem modItem : modItems) {
                log.debug(modItem.toString());
            }
        }
        this.ldapTemplate.modifyAttributes(dn, modItems);
        return registeredService;
    }

    protected DirContextAdapter lookupCtx(final String dn) {
        return dn == null ? null : (DirContextAdapter) this.ldapTemplate.lookup(dn);
    }

    protected String findDn(final String filter) {
        final List results = this.ldapTemplate.search(this.serviceBaseDn, filter, SearchControls.SUBTREE_SCOPE, new String[0], new ContextMapper() {
            public Object mapFromContext(final Object ctx) {
                return ((DirContextAdapter) ctx).getNameInNamespace();
            }
        });
        if (results == null || results.isEmpty()) {
            return null;
        } else if (results.size() == 1 || this.ignoreMultipleSearchResults) {
            return (String) results.get(0);
        } else {
            throw new RuntimeException("Multiple results returned by LDAP Server for Filter " + filter);
        }
    }

    public boolean delete(final RegisteredService registeredService) {
        final String dn = findDn(this.ldapServiceMapper.getSearchFilter(registeredService.getId()).encode());
        try {
            this.ldapTemplate.unbind(dn, false);
            return true;
        } catch (final Exception e) {
            log.warn("Error deleting Registered Service", e);
            return false;
        }
    }

    public List<RegisteredService> load() {
        try {
            return this.ldapTemplate.search(this.serviceBaseDn, this.ldapServiceMapper.getLoadFilter().encode(), this.cachedSearchControls, this.ldapServiceMapper);
        } catch (final Exception e) {
            log.error("Exception while loading Registered Services from LDAP Directory...", e);
            return new ArrayList<RegisteredService>();
        }
    }

    public RegisteredService findServiceById(final long id) {
        return (RegisteredService) this.ldapTemplate.lookup(findDn(this.ldapServiceMapper.getSearchFilter(id).encode()), this.ldapServiceMapper);
    }

    public void setServiceBaseDN(final String serviceBaseDN) {
        this.serviceBaseDn = serviceBaseDN;
    }

    public void setLdapTemplate(final LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void setIgnoreMultipleSearchResults(final boolean ignoreMultipleSearchResults) {
        this.ignoreMultipleSearchResults = ignoreMultipleSearchResults;
    }

    public void setLdapServiceMapper(final LdapServiceMapper ldapServiceMapper) {
        this.ldapServiceMapper = ldapServiceMapper;
    }
}
