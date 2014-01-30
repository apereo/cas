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

package org.jasig.cas.authentication.support;

import java.util.List;

import org.jasig.cas.Message;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.ldaptive.SearchScope;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;

/**
 * Provides platform-specific account state handling for Active Directory.
 *
 * @author Marvin S. Addison
 */
public class ActiveDirectoryAccountStateHandler extends DefaultAccountStateHander {

    /** Number of milliseconds between standard Unix era, 1/1/1970, and filetime start, 1/1/1601. */
    private static final long ERA_OFFSET = 11644473600000L;

    /** Source of LDAP connections. */
    private ConnectionFactory connectionFactory;

    /** Root DN containing CN=Builtin entry. */
    private String rootDN;

    /** Maximum password age. */
    private Duration maxPasswordAge;


    /**
     * Initializes this component. MUST be called prior to
     * {@link #handle(AuthenticationResponse, LdapPasswordPolicyConfiguration)}.
     */
    public void initialize() {
        // Query for the default domain policy maxPwdAge attribute in the domain entry.
        // NOTE: does not support Windows Server 2008 Fine-Grained Password Policies.
        final SearchExecutor se = new SearchExecutor();
        se.setBaseDn(rootDN);
        se.setSearchFilter(new SearchFilter("(objectClass=*)"));
        se.setReturnAttributes("maxPwdAge");
        se.setSearchScope(SearchScope.OBJECT);
        try {
            final Response<SearchResult> response = se.search(connectionFactory);
            maxPasswordAge = parseDeltaTime(response.getResult().getEntry().getAttribute("maxPwdAge").getStringValue());
        } catch (final LdapException e) {
            throw new IllegalStateException("LDAP error searching for maxPwdAge", e);
        }
    }

    public void setConnectionFactory(final ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setRootDN(final String rootDN) {
        this.rootDN = rootDN;
    }

    @Override
    protected void handleWarning(
            final AccountState.Warning warning,
            final AuthenticationResponse response,
            final LdapPasswordPolicyConfiguration configuration,
            final List<Message> messages) {
        super.handleWarning(warning, response, configuration, messages);
        final Instant pwdLastSet = parseFileTime(response.getLdapEntry().getAttribute("pwdLastSet").getStringValue());
        final Days ttl = Days.daysBetween(Instant.now(), pwdLastSet.plus(maxPasswordAge));
        if (ttl.getDays() < configuration.getPasswordWarningNumberOfDays()) {
            messages.add(new PasswordExpiringWarningMessage(
                    "Password expires in {0} days. Please change your password at <href=\"{1}\">{1}</a>",
                    ttl.getDays(),
                    configuration.getPasswordPolicyUrl()));
        }
    }

    /**
     * Parses a time value in delta time format, http://msdn.microsoft.com/en-us/library/cc232152.aspx.
     *
     * @param deltaTimeString Negative number of 100-nanosecond intervals.
     *
     * @return Corresponding Joda time duration.
     */
    private static Duration parseDeltaTime(final String deltaTimeString) {
        final long deltaTime = -Long.parseLong(deltaTimeString);
        return new Duration(deltaTime / 10000L);
    }

    /**
     * Parses a Microsoft FILETIME date, http://msdn.microsoft.com/en-us/library/windows/desktop/ms724290(v=vs.85).aspx.
     *
     * @param fileTimeString Number of 100-nanosecond intervals since Jan 1, 1601.
     *
     * @return Corresponding Joda time instant.
     */
    private static Instant parseFileTime(final String fileTimeString) {
        return new Instant(Long.parseLong(fileTimeString) / 10000L - ERA_OFFSET);
    }
}
