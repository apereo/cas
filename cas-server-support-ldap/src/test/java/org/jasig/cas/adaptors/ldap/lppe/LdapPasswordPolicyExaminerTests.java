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
package org.jasig.cas.adaptors.ldap.lppe;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

public class LdapPasswordPolicyExaminerTests {

    @Test(expected=LdapPasswordPolicyAuthenticationException.class)
    public void testPasswordExpirationDateAlwaysWarn() throws LdapPasswordPolicyAuthenticationException {
        final LdapPasswordPolicyExaminer examiner = new LdapPasswordExpirationPolicyExaminer(getDateConvterter(), true);
        final LdapPasswordPolicyConfiguration config = getConfiguration();
        config.setPasswordWarningNumberOfDays(15);
        examiner.examinePasswordPolicy(getConfiguration());
    }
    
    @Test(expected=LdapPasswordPolicyAuthenticationException.class)
    public void testPasswordExpirationDate() throws LdapPasswordPolicyAuthenticationException {
        final LdapPasswordPolicyExaminer examiner = new LdapPasswordExpirationPolicyExaminer(getDateConvterter());
        examiner.examinePasswordPolicy(getConfiguration());
    }

    @Test
    public void testPasswordExpirationDateIgnoreWarning() throws LdapPasswordPolicyAuthenticationException {
        final LdapPasswordPolicyExaminer examiner = new LdapPasswordExpirationPolicyExaminer(getDateConvterter(), Arrays.asList("ignoreWarning", "somethingElse"));
        final LdapPasswordPolicyConfiguration config = getConfiguration();
        config.setIgnorePasswordExpirationWarning("ignoreWarning");
        examiner.examinePasswordPolicy(config);
    }
    
    @Test
    public void testPasswordExpirationDateUserAccountControl() throws LdapPasswordPolicyAuthenticationException {
        final LdapPasswordPolicyExaminer examiner = new LdapPasswordExpirationPolicyExaminer(getDateConvterter());
        final LdapPasswordPolicyConfiguration config = getConfiguration();
        config.setPasswordWarningNumberOfDays(15);
        config.setUserAccountControl("65536");
        examiner.examinePasswordPolicy(config);
    }
    
    private LdapPasswordPolicyConfiguration getConfiguration() {
        final LdapPasswordPolicyConfiguration configuration = new LdapPasswordPolicyConfiguration(getCredentials());
        configuration.setPasswordExpirationDateAttributeName("pswLastChanged");
        configuration.setPasswordExpirationDate("15");
        configuration.setPasswordWarningNumberOfDays(50);
        configuration.setValidPasswordNumberOfDays(30);
        
        return configuration;
    }
    
    private LdapDateConverter getDateConvterter() {
        return new TimeUnitLdapDateConverter(TimeUnit.DAYS, new DateTime(DateTimeZone.UTC));
    }
    
    private UsernamePasswordCredentials getCredentials() {
        final UsernamePasswordCredentials cred = new UsernamePasswordCredentials();
        cred.setPassword("test");
        cred.setUsername("test");
        return cred;
    }
}
