package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.core.authentication.passwordsync.LdapPasswordSynchronizationProperties;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapAttribute;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ResultCode;
import org.ldaptive.ad.UnicodePwdAttribute;

import java.util.List;

/**
 * This is {@link LdapPasswordSynchronizationAuthenticationPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class LdapPasswordSynchronizationAuthenticationPostProcessor implements AuthenticationPostProcessor {
    private final LdapConnectionFactory searchFactory;

    private final LdapPasswordSynchronizationProperties ldapProperties;

    @Override
    public void destroy() {
        searchFactory.close();
    }

    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) throws AuthenticationException {
        val primaryCredential = transaction.getPrimaryCredential();
        if (primaryCredential.isEmpty()) {
            LOGGER.warn("Current authentication transaction does not have a primary credential");
            return;
        }

        try {
            val credential = (UsernamePasswordCredential) primaryCredential.get();
            val filter = LdapUtils.newLdaptiveSearchFilter(ldapProperties.getSearchFilter(),
                LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, List.of(credential.getUsername()));
            LOGGER.trace("Constructed LDAP filter [{}] to locate user and update password", filter);

            val response = searchFactory.executeSearchOperation(ldapProperties.getBaseDn(), filter, this.ldapProperties.getPageSize());
            LOGGER.debug("LDAP response is [{}]", response);

            if (LdapUtils.containsResultEntry(response)) {
                val dn = response.getEntry().getDn();
                LOGGER.debug("Updating account password for [{}]", dn);

                val operation = new ModifyOperation(searchFactory.getConnectionFactory());
                val mod = new AttributeModification(AttributeModification.Type.REPLACE, getLdapPasswordAttribute(credential));
                val updateResponse = operation.execute(new ModifyRequest(dn, mod));
                LOGGER.trace("Result code [{}], message: [{}]", response.getResultCode(), response.getDiagnosticMessage());
                val result = updateResponse.getResultCode() == ResultCode.SUCCESS;
                if (!result) {
                    val message = String.format("Could not update the LDAP entry's password for %s and base DN %s: %s",
                        filter.format(), ldapProperties.getBaseDn(), updateResponse.getDiagnosticMessage());
                    throw new IllegalStateException(message);
                }
                LOGGER.info("Updated the LDAP entry's password for [{}] and base DN [{}]", filter.format(), ldapProperties.getBaseDn());
            } else {
                val message = String.format("Could not locate an LDAP entry for %s and base DN %s", filter.format(), ldapProperties.getBaseDn());
                throw new IllegalStateException(message);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            if (ldapProperties.isPasswordSynchronizationFailureFatal()) {
                throw new AuthenticationException(e);
            }

        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }

    protected LdapAttribute getLdapPasswordAttribute(final UsernamePasswordCredential credential) {
        if ("unicodePwd".equals(ldapProperties.getPasswordAttribute())) {
            return new UnicodePwdAttribute(credential.toPassword());
        }
        val attr = new LdapAttribute(ldapProperties.getPasswordAttribute());
        attr.addStringValues(credential.toPassword());
        return attr;
    }
}
