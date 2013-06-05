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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.Message;
import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AccountPasswordMustChangeException;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.LdapAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.ldaptive.LdapEntry;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.Authenticator;

/**
 * An extension of the {@link LdapAuthenticationHandler} that is aware of
 * the underlying password policy and can translate errors and warnings
 * back to the CAS flow.
 * @author Misagh Moayyed
 * @since 4.0
 */
public class LPPEAuthenticationHandler extends LdapAuthenticationHandler {

    /** The ldap configuration constructed based on given the policy. **/
    private final PasswordPolicyConfiguration configuration;

    public LPPEAuthenticationHandler(@NotNull final Authenticator authenticator,
            @NotNull final PasswordPolicyConfiguration configuration) {
        super(authenticator);
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     * <p>Builds the {@link PasswordPolicyConfiguration} defined, examines the account status
     * for locked, disabled, expired, etc accounts and validates the password expiration policy.
     * If the policy cannot be built, account status matches one of the defined failure conditions
     * or the password policy expiration fails and the configuration is set as critical,
     * the authentication will fail. otherwise a warning is issued and the flow.
     * is resumed.
     * @see PasswordPolicyConfiguration#setCritical(boolean)
     * @see #examineAccountStatus(AuthenticationResponse)
     * @see #validateAccountPasswordExpirationPolicy()
     */
    @Override
    protected final HandlerResult doPostAuthentication(final AuthenticationResponse response)
            throws LoginException {

        final LdapEntry entry = response.getLdapEntry();
        final PasswordPolicyResult result = configuration.build(entry);
        if (result == null) {
            throw new FailedLoginException("LPPE authentication failed. Configuration cannot be determined.");
        }

        this.examineAccountStatus(response, result);
        final List<Message> warnings = this.validateAccountPasswordExpirationPolicy(result);
        return new HandlerResult(
                this,
                new BasicCredentialMetaData(new UsernamePasswordCredentials()),
                super.createPrincipal(entry), warnings);
    }

    /**
     * Examine the account status based on custom attributes defined (if any),
     * to determine whether the account status matches the following cases.
     * <ul>
     * <li>Disabled: {@link AccountDisabledException}</li>
     * <li>Locked: {@link AccountLockedException}</li>
     * <li>Expired: {@link CredentialExpiredException}</li>
     * <li>Password Must Change: {@link AccountPasswordMustChangeException}</li>
     * </ul>
     * @param response the ldaptive authentication response.
     * @throws LoginException if the above conditions match, an instance of LoginException
     * mapped to the error is thrown.
     */
    protected void examineAccountStatus(final AuthenticationResponse response,
            final PasswordPolicyResult result) throws LoginException {
        final String uid =  result.getDn();

        if (result.isAccountExpired()) {
            final String msg = String.format("Account %s has expired", uid);
            throw new CredentialExpiredException(msg);
        }

        if (result.isAccountDisabled()) {
            final String msg = String.format("Account %s is disabled", uid);
            throw new AccountDisabledException(msg);
        }

        if (result.isAccountLocked()) {
            final String msg = String.format("Account %s is locked", uid);
            throw new AccountLockedException(msg);
        }

        if (result.isAccountPasswordMustChange()) {
            final String msg = String.format("Account %s must change it password", uid);
            throw new AccountPasswordMustChangeException(msg);
        }
    }

    @Override
    protected void initializeInternal() {
        populatePrincipalAttributeMap();
    }

    /**
     * Populate configured custom attributes automatically to be returned
     * as part of the authentication. This is a facility to provide easier and reduced
     * configuration.
     */
    private void populatePrincipalAttributeMap() {
        principalAttributeMap.putAll(configuration.getPasswordPolicyAttributesMap());
    }

    /**
     * Calculates the number of days left to the expiration date.
     * @return Number of days left to the expiration date or -1 if the no expiration warning is
     * calculated based on the defined policy.
     */
    protected int getDaysToExpirationDate(final DateTime expireDate, final PasswordPolicyResult result) throws LoginException {
        final DateTimeZone timezone = configuration.getDateConverter().getTimeZone();
        final DateTime currentTime = new DateTime(timezone);
        log.debug("Current date is {}. Expiration date is {}", currentTime, expireDate);

        final Days d = Days.daysBetween(currentTime, expireDate);
        int daysToExpirationDate = d.getDays();

        log.debug("[{}] days left to the expiration date.", daysToExpirationDate);
        if (expireDate.equals(currentTime) || expireDate.isBefore(currentTime)) {
            final String msgToLog = String.format("Password expiration date %s is on/before the current time %s.",
                                          daysToExpirationDate, currentTime);
            log.debug(msgToLog);
            throw new CredentialExpiredException(msgToLog);
        }

        // Warning period begins from X number of days before the expiration date
        final DateTime warnPeriod = new DateTime(DateTime.parse(expireDate.toString()), timezone)
                                        .minusDays(result.getPasswordWarningNumberOfDays());
        log.debug("Warning period begins on [{}]", warnPeriod);

        if (configuration.isAlwaysDisplayPasswordExpirationWarning()) {
            log.debug("Warning all. The password for [{}] will expire in [{}] day(s).",
                    result.getDn(), daysToExpirationDate);
        } else if (currentTime.equals(warnPeriod) || currentTime.isAfter(warnPeriod)) {
            log.debug("Password will expire in [{}] day(s)", daysToExpirationDate);
        } else {
            log.debug("Password is not expiring. [{}] day(s) left to the warning.", daysToExpirationDate);
            daysToExpirationDate = -1;
        }

        return daysToExpirationDate;
    }

    /**
     * Determines the expiration date to use based on the password policy configuration.
     * Converts the password expiration date based on the
     * {@link PasswordPolicyConfiguration#setDateConverter(LdapDateConverter)} and returns
     * that value is the policy is set to evaluate against a static password expiration date.
     * Otherwise, adds {@link PasswordPolicyConfiguration#getValidPasswordNumberOfDays()} days
     * and returns the expiration date.
     * @return the configured expiration date to use.
     */
    private DateTime getExpirationDateToUse(final PasswordPolicyResult result) {
        final DateTime dateValue = result.getPasswordExpirationDateTime();

        if (configuration.getStaticPasswordExpirationDate() == null) {
          final DateTime expireDate = dateValue.plusDays(result.getValidPasswordNumberOfDays());
          log.debug("Retrieved date value [{}] for date attribute [{}] and added {} valid days. "
                    + "The final expiration date is [{}]", dateValue,
                    configuration.getPasswordExpirationDateAttributeName(),
                    result.getValidPasswordNumberOfDays(), expireDate);

          return expireDate;
        }
        return dateValue;
    }

    /**
     * Validate the account password expiration policy based on results collected.
     * @param result the policy result object
     * @return List of warnings
     * @throws LoginException if the account has already expired.
     */
    private List<Message> validateAccountPasswordExpirationPolicy(final PasswordPolicyResult result) throws LoginException {
        if (result.isAccountPasswordSetToNeverExpire()) {
            log.debug("Account password will never expire. Skipping password policy...");
            return Collections.emptyList();
        }

        final DateTime expireTime = getExpirationDateToUse(result);
        final List<Message> warnings = new LinkedList<Message>();
        if (configuration.getStaticPasswordExpirationDate() != null &&
            (expireTime.equals(configuration.getStaticPasswordExpirationDate()) ||
             expireTime.isAfter(configuration.getStaticPasswordExpirationDate()))) {

            final String msg = String.format("Account password has expired beyond the static expiration date [{}]",
                    configuration.getStaticPasswordExpirationDate());
            log.debug(msg);
            throw new CredentialExpiredException(msg);
        }

        final int days = getDaysToExpirationDate(expireTime, result);
        if (days != -1) {
            final String msg = String.format("Password expires in [%d] days", days);
            log.debug(msg);
            warnings.add(new AccountPasswordExpiringMessage(msg, days));
        }
        return warnings;
    }

}
