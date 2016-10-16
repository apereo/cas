package org.apereo.cas.pm.ldap;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * This is {@link LdapPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class LdapPasswordManagementService implements PasswordManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapPasswordManagementService.class);
    
    @Autowired
    private CasConfigurationProperties casProperties;

    private CipherExecutor<String, String> cipherExecutor;

    public LdapPasswordManagementService(final CipherExecutor<String, String> cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }

    @Override
    public String findEmail(final String username) {
        return null;
    }

    @Override
    public void trackToken(final String username, final String token) {

    }

    @Override
    public String createResetUrl(final String token) {
        return null;
    }

    @Override
    public String createToken() {
        final String token = UUID.randomUUID().toString();
        return this.cipherExecutor.encode(token);
    }

    @Override
    public boolean change(final Credential credential, final PasswordChangeBean bean) {
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
                if (LdapUtils.executePasswordModifyOperation(dn, factory, 
                        c.getPassword(), bean.getPassword(),
                        casProperties.getAuthn().getPm().getLdap().getType())) {
                    LOGGER.debug("Successfully updated the account password for {}", dn);
                    return true;
                }
                LOGGER.error("Could not update the LDAP entry's password for {} and base DN {}", filter.format(), ldap.getBaseDn());
            } else {
                LOGGER.error("Could not locate an LDAP entry for {} and base DN {}", filter.format(), ldap.getBaseDn());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
