/*
 * Copyright 2002-2004 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.springframework.ldap.core.support;

import javax.naming.directory.DirContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.ContextSource;
import org.springframework.ldap.support.LdapExceptionTranslator;
import org.springframework.ldap.support.LdapUtils;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public abstract class LdapDaoSupport implements InitializingBean {

    protected final Log log = LogFactory.getLog(getClass());

    private LdapTemplate ldapTemplate;

    public final void setContextSource(final ContextSource contextSource) {
        this.ldapTemplate = new LdapTemplate(contextSource);
    }

    public final ContextSource getContextSource() {
        return this.ldapTemplate != null ? this.ldapTemplate.getContextSource()
            : null;
    }

    /**
     * @return Returns the ldapTemplate.
     */
    public LdapTemplate getLdapTemplate() {
        return this.ldapTemplate;
    }

    /**
     * @param ldapTemplate The ldapTemplate to set.
     */
    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public final void afterPropertiesSet() throws Exception {
        if (this.ldapTemplate == null) {
            throw new IllegalArgumentException(
                "dataSource or ldapTemplate is required");
        }
        initDao();
    }

    /**
     * Subclasses can override this for custom initialization behavior. Gets called after population of this instance's bean properties.
     * 
     * @throws Exception if initialization fails
     */
    protected void initDao() throws Exception {
        // not required
    }

    /**
     * Return the LdapExceptionTranslator of this DAO's LdapTemplate, for translating SQLExceptions in custom LDAP access code.
     */
    protected final LdapExceptionTranslator getExceptionTranslator() {
        return this.ldapTemplate.getExceptionTranslator();
    }

    /**
     * Close the given LDAP Context if necessary, created via this bean's ContextSource, if it isn't bound to the thread.
     * 
     * @param con Ldap Context to close
     */
    protected final void closeContextIfNecessary(DirContext con) {
        LdapUtils.closeContext(con);
    }
}