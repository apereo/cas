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
package org.jasig.cas.authentication.support;

import org.jasig.cas.authentication.PreventedException;
import org.ldaptive.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;

/**
 * Created by ruaa on 2015. 8. 20..
 */
public class LdapAttributeRetriever {
    @Autowired
    private ConnectionFactory connectionFactory;
    @NotNull
    private String baseDn;
    @NotNull
    private String userFilter;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void setUserFileter(String userFileter) {
        this.userFilter = userFileter;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public LdapEntry retrieve(String user, String... attrs) throws PreventedException{
        LdapEntry ldapEntry = null;
        Connection connection = null;

        try {
            connection = connectionFactory.getConnection();
            connection.open();

            String.format("%s,%s", attrs);

            SearchOperation search = new SearchOperation(connection);
            SearchRequest searchRequest = new SearchRequest(baseDn, createSearchFilter(user));
            searchRequest.setReturnAttributes(attrs);

            SearchResult result = search.execute(searchRequest).getResult();

            ldapEntry = result.getEntry();
        } catch (LdapException e) {
            throw new PreventedException("Unexpected LDAP error", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        return ldapEntry;
    }

    protected SearchFilter createSearchFilter(String user) {
        SearchFilter filter = new SearchFilter();
        if(this.userFilter != null) {
            this.logger.debug("searching for DN using userFilter");
            filter.setFilter(this.userFilter);
            filter.setParameter("user", user);
        } else {
            this.logger.error("Invalid userFilter, cannot be null or empty.");
        }

        return filter;
    }
}
