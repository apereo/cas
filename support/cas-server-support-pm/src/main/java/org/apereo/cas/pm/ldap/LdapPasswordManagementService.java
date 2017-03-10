package org.apereo.cas.pm.ldap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.util.LdapUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Arrays;
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
            final PasswordManagementProperties.Ldap ldap = passwordManagementProperties.getLdap();
            final SearchFilter filter = Beans.newLdaptiveSearchFilter(ldap.getUserFilter(),
                    Beans.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                    Arrays.asList(username));
            final ConnectionFactory factory = Beans.newLdaptivePooledConnectionFactory(ldap);
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(factory, ldap.getBaseDn(), filter);
            if (LdapUtils.containsResultEntry(response)) {
                final LdapEntry entry = response.getResult().getEntry();
                final LdapAttribute attr = entry.getAttribute(passwordManagementProperties.getReset().getEmailAttribute());
                if (attr != null) {
                    final String email = attr.getStringValue();
                    if (EmailValidator.getInstance().isValid(email)) {
                        return email;
                    }
                }
                return null;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Audit(action = "CHANGE_PASSWORD",
            actionResolverName = "CHANGE_PASSWORD_ACTION_RESOLVER",
            resourceResolverName = "CHANGE_PASSWORD_RESOURCE_RESOLVER")
    @Override
    public boolean change(final Credential credential, final PasswordChangeBean bean) {
        Assert.notNull(credential, "Credential cannot be null");
        Assert.notNull(bean, "PasswordChangeBean cannot be null");

        try {
            final PasswordManagementProperties.Ldap ldap = passwordManagementProperties.getLdap();
            final UsernamePasswordCredential c = (UsernamePasswordCredential) credential;

            final SearchFilter filter = Beans.newLdaptiveSearchFilter(ldap.getUserFilter(),
                    Beans.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                    Arrays.asList(c.getId()));
            final ConnectionFactory factory = Beans.newLdaptivePooledConnectionFactory(ldap);
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(factory, ldap.getBaseDn(), filter);

            if (LdapUtils.containsResultEntry(response)) {
                final String dn = response.getResult().getEntry().getDn();
                LOGGER.debug("Updating account password for [{}]", dn);
                if (LdapUtils.executePasswordModifyOperation(dn, factory, c.getPassword(), bean.getPassword(), 
                        passwordManagementProperties.getLdap().getType())) {
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
            final PasswordManagementProperties.Ldap ldap = passwordManagementProperties.getLdap();
            final SearchFilter filter = Beans.newLdaptiveSearchFilter(ldap.getUserFilter(),
                    Beans.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                    Arrays.asList(username));
            final ConnectionFactory factory = Beans.newLdaptivePooledConnectionFactory(ldap);
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(factory, ldap.getBaseDn(), filter);
            if (LdapUtils.containsResultEntry(response)) {
                final LdapEntry entry = response.getResult().getEntry();
                final Map<String, String> qs = passwordManagementProperties.getLdap().getSecurityQuestionsAttributes();
                qs.forEach((k, v) -> {
                    final LdapAttribute q = entry.getAttribute(k);
                    final LdapAttribute a = entry.getAttribute(v);
                    if (q != null && a != null && StringUtils.isNotBlank(q.getStringValue()) && StringUtils.isNotBlank(a.getStringValue())) {
                        set.put(q.getStringValue(), a.getStringValue());
                    }
                });
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return set;
    }
}
