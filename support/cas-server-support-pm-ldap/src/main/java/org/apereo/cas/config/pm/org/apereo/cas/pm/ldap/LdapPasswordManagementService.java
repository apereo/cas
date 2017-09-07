package org.apereo.cas.config.pm.org.apereo.cas.pm.ldap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;

import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link LdapPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class LdapPasswordManagementService extends BasePasswordManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapPasswordManagementService.class);

    public LdapPasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                         final String issuer,
                                         final PasswordManagementProperties passwordManagementProperties) {
        super(cipherExecutor, issuer, passwordManagementProperties);
    }

    @Override
    public String findEmail(final String username) {
        try {
            final PasswordManagementProperties.Ldap ldap = properties.getLdap();
            final SearchFilter filter = LdapUtils.newLdaptiveSearchFilter(ldap.getUserFilter(),
                    LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                    CollectionUtils.wrap(username));
            LOGGER.debug("Constructed LDAP filter [{}] to locate account email", filter);

            final ConnectionFactory factory = LdapUtils.newLdaptivePooledConnectionFactory(ldap);
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(factory, ldap.getBaseDn(), filter);
            LOGGER.debug("LDAP response to locate account email is [{}]", response);

            if (LdapUtils.containsResultEntry(response)) {
                final LdapEntry entry = response.getResult().getEntry();
                LOGGER.debug("Found LDAP entry [{}] to use for the account email", entry);

                final String attributeName = properties.getReset().getEmailAttribute();
                final LdapAttribute attr = entry.getAttribute(attributeName);
                if (attr != null) {
                    final String email = attr.getStringValue();
                    LOGGER.debug("Found email address [{}] for user [{}]. Validating...", email, username);
                    if (EmailValidator.getInstance().isValid(email)) {
                        LOGGER.debug("Email address [{}] matches a valid email address", email);
                        return email;
                    }
                    LOGGER.error("Email [{}] is not a valid address", email);
                } else {
                    LOGGER.error("Could not locate an LDAP attribute [{}] for [{}] and base DN [{}]",
                            attributeName, filter.format(), ldap.getBaseDn());
                }
                return null;
            }
            LOGGER.error("Could not locate an LDAP entry for [{}] and base DN [{}]", filter.format(), ldap.getBaseDn());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
    
    @Override
    public boolean changeInternal(final Credential credential, final PasswordChangeBean bean) {
        try {
            final PasswordManagementProperties.Ldap ldap = properties.getLdap();
            final UsernamePasswordCredential c = (UsernamePasswordCredential) credential;

            final SearchFilter filter = LdapUtils.newLdaptiveSearchFilter(ldap.getUserFilter(),
                    LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                    CollectionUtils.wrap(c.getId()));
            LOGGER.debug("Constructed LDAP filter [{}] to update account password", filter);

            final ConnectionFactory factory = LdapUtils.newLdaptivePooledConnectionFactory(ldap);
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(factory, ldap.getBaseDn(), filter);
            LOGGER.debug("LDAP response to update password is [{}]", response);

            if (LdapUtils.containsResultEntry(response)) {
                final String dn = response.getResult().getEntry().getDn();
                LOGGER.debug("Updating account password for [{}]", dn);
                if (LdapUtils.executePasswordModifyOperation(dn, factory, c.getPassword(), bean.getPassword(),
                        properties.getLdap().getType())) {
                    LOGGER.debug("Successfully updated the account password for [{}]", dn);
                    return true;
                }
                LOGGER.error("Could not update the LDAP entry's password for [{}] and base DN [{}]", filter.format(), ldap.getBaseDn());
            } else {
                LOGGER.error("Could not locate an LDAP entry for [{}] and base DN [{}]", filter.format(), ldap.getBaseDn());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        final Map<String, String> set = new LinkedHashMap<>();
        try {
            final PasswordManagementProperties.Ldap ldap = properties.getLdap();
            final SearchFilter filter = LdapUtils.newLdaptiveSearchFilter(ldap.getUserFilter(),
                    LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                    CollectionUtils.wrap(username));
            LOGGER.debug("Constructed LDAP filter [{}] to locate security questions", filter);

            final ConnectionFactory factory = LdapUtils.newLdaptivePooledConnectionFactory(ldap);
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(factory, ldap.getBaseDn(), filter);
            LOGGER.debug("LDAP response for security questions [{}]", response);

            if (LdapUtils.containsResultEntry(response)) {
                final LdapEntry entry = response.getResult().getEntry();
                LOGGER.debug("Located LDAP entry [{}] in the response", entry);
                final Map<String, String> qs = properties.getLdap().getSecurityQuestionsAttributes();
                LOGGER.debug("Security question attributes are defined to be [{}]", qs);

                qs.forEach((k, v) -> {
                    final LdapAttribute q = entry.getAttribute(k);
                    final LdapAttribute a = entry.getAttribute(v);
                    if (q != null && a != null && StringUtils.isNotBlank(q.getStringValue())
                            && StringUtils.isNotBlank(a.getStringValue())) {
                        LOGGER.debug("Added security question [{}]", q.getStringValue());
                        set.put(q.getStringValue(), a.getStringValue());
                    }
                });
            } else {
                LOGGER.debug("LDAP response did not contain a result for security questions");
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return set;
    }
}
