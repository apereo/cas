/*
 * Copyright 2002-2004 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.springframework.ldap.support;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * DirContext provider which creates new DirContext by finding a sub object from a base DirContext. It is thus parametized by a ContextSource and a
 * relative name.
 * 
 * @author Olivier Jolly
 */
public class RelativeContextSource implements ContextSource, InitializingBean {

    private ContextSource baseContextSource = null;

    private String relativeName = "";

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * @return Returns the baseContextSource.
     */
    public ContextSource getBaseContextSource() {
        return this.baseContextSource;
    }

    /**
     * @param baseContextSource The baseContextSource to set.
     */

    public void setBaseContextSource(ContextSource baseContextSource) {
        this.baseContextSource = baseContextSource;
    }

    /**
     * @return Returns the relativeName.
     */
    public String getRelativeName() {
        return this.relativeName;
    }

    /**
     * @param relativeName The relativeName to set.
     */
    public void setRelativeName(String relativeName) {
        this.relativeName = relativeName;
    }


    public DirContext getDirContext() {
        try {
            return (DirContext)getBaseContextSource().getDirContext().lookup(
                getRelativeName());
        }
        catch (NamingException e) {
            String baseName = "Can not get basename";
            try {
                baseName = getBaseContextSource().getDirContext()
                    .getNameInNamespace();
            }
            catch (NamingException ex) {
                logger.warn("Can not get basename", ex);
            }
            throw new DataAccessResourceFailureException(
                "Can not get relative context (base = " + baseName
                    + " , relative name = " + getRelativeName(), e);
        }
    }

    public DirContext getDirContext(String principal, String password) {
        try {
            return (DirContext)getBaseContextSource().getDirContext(principal,
                password).lookup(getRelativeName());
        }
        catch (NamingException e) {
            String baseName = "Can not get basename";
            try {
                baseName = getBaseContextSource().getDirContext(principal,
                    password).getNameInNamespace();
            }
            catch (NamingException ex) {
                logger.warn("Can not get basename", ex);
            }
            throw new DataAccessResourceFailureException(
                "Can not get relative context (base = " + baseName
                    + " , relative name = " + getRelativeName(), e);
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (getBaseContextSource() == null) {
            logger.error("baseContextSource null");
        }
    }
}