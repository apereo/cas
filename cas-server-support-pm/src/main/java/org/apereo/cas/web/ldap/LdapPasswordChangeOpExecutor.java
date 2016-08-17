package org.apereo.cas.web.ldap;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.web.PasswordChangeBean;
import org.apereo.cas.web.PasswordChangeOpExecutor;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is {@link LdapPasswordChangeOpExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class LdapPasswordChangeOpExecutor implements PasswordChangeOpExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapPasswordChangeOpExecutor.class);
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Override
    public boolean execute(final Credential credential, final PasswordChangeBean bean) {
        try {
            final PasswordManagementProperties.Ldap ldap = casProperties.getAuthn().getPm().getLdap();
            final UsernamePasswordCredential c = (UsernamePasswordCredential) credential;

            final SearchFilter filter = Beans.newSearchFilter(ldap.getUserFilter(), c.getId());
            final ConnectionFactory factory = Beans.newPooledConnectionFactory(ldap);
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(factory,
                    ldap.getBaseDn(), filter);

            if (LdapUtils.containsResultEntry(response)) {
                final String dn = response.getResult().getEntry().getDn();
                LOGGER.debug("Updating account password for {}", dn);
                if (LdapUtils.executePasswordModifyOperation(dn, factory, c.getPassword(), bean.getPassword())) {
                    LOGGER.debug("Successfully updated the account password for {}", dn);
                    return true;
                }
                LOGGER.error("Could not update the LDAP entry's password for {} and base DN {}", filter.format(), ldap.getBaseDn());
            } else {
                LOGGER.error("Could not locate an LDAP entry for {} and base DN {}", filter.format(), ldap.getBaseDn());
            }
        } catch (final Exception e) {
            LOGGER.error("Update failed", e.getMessage());
        }
        return false;
    }
}
