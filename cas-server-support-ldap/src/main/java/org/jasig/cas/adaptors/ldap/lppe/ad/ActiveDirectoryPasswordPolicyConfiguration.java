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
package org.jasig.cas.adaptors.ldap.lppe.ad;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.adaptors.ldap.lppe.PasswordPolicyConfiguration;
import org.jasig.cas.adaptors.ldap.lppe.PasswordPolicyResult;
import org.ldaptive.LdapEntry;

/**
 * The password policy configuration defined by the underlying data source.
 * @author Misagh Moayyed
 * @version 4.0.0
 */
public class ActiveDirectoryPasswordPolicyConfiguration extends PasswordPolicyConfiguration {

    /** The attribute that indicates the user account status. **/
    private String userAccountControlAttributeName = "userAccountControl";

    public void setUserAccountControlAttributeName(final String attr) {
        this.userAccountControlAttributeName = attr;
    }

    public String getUserAccountControlAttributeName() {
        return this.userAccountControlAttributeName;
    }

    @Override
    protected Map<String, String> getPasswordPolicyAttributesMap() {
        final Map<String, String> map = super.getPasswordPolicyAttributesMap();
        if (!StringUtils.isBlank(getUserAccountControlAttributeName())) {
            map.put(getUserAccountControlAttributeName(),
                                      getUserAccountControlAttributeName());
        }
        return map;
    }

    @Override
    protected PasswordPolicyResult getPasswordPolicyResultInstance() {
        return new ActiveDirectoryPasswordPolicyResult(this);
    }
    
    @Override
    protected PasswordPolicyResult buildInternal(final LdapEntry entry, final PasswordPolicyResult result) {
        final ActiveDirectoryPasswordPolicyResult adResult = (ActiveDirectoryPasswordPolicyResult) result;
        final String attributeValue = getPasswordPolicyAttributeValue(entry, getUserAccountControlAttributeName());
        if (attributeValue != null) {
            adResult.setUserAccountControl(attributeValue);
        }
        return super.buildInternal(entry, result);
    }
}
