package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.web.PasswordChangeBean;
import org.apereo.cas.web.support.WebUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordChangeAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PasswordChangeAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordChangeAction.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        try {
            final PasswordManagementProperties.Ldap ldap = casProperties.getAuthn().getPm().getLdap();
            final UsernamePasswordCredential c = (UsernamePasswordCredential) WebUtils.getCredential(requestContext);
            final PasswordChangeBean bean = requestContext.getFlowScope().get("password", PasswordChangeBean.class);

            final SearchFilter filter = Beans.newSearchFilter(ldap.getUserFilter(), c.getId());
            final ConnectionFactory factory = Beans.newPooledConnectionFactory(ldap);
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(factory,
                    ldap.getBaseDn(), filter);

            if (LdapUtils.containsResultEntry(response)) {
                final String dn = response.getResult().getEntry().getDn();
                LOGGER.debug("Updating account password for {}", dn);
                if (LdapUtils.executePasswordModifyOperation(dn, factory, c.getPassword(), bean.getPassword())) {
                    LOGGER.debug("Successfully updated the account password for {}", dn);
                    return new EventFactorySupport().event(this, "passwordUpdateSuccess");
                }
                LOGGER.error("Could not update the LDAP entry's password for {} and base DN {}", filter.format(), ldap.getBaseDn());
            } else {
                LOGGER.error("Could not locate an LDAP entry for {} and base DN {}", filter.format(), ldap.getBaseDn());
            }
        } catch (final Exception e) {
            LOGGER.error("Update failed", e.getMessage());
        }
        requestContext.getMessageContext().addMessage(new MessageBuilder().error().code("pm.updateFailure").
                defaultText("Could not update the account password").build());
        return error();
    }
}
