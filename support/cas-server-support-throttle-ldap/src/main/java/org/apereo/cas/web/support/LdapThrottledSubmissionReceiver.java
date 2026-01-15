package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.throttle.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.ReturnAttributes;

/**
 * This is {@link LdapThrottledSubmissionReceiver}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class LdapThrottledSubmissionReceiver implements ThrottledSubmissionReceiver {
    private final LdapConnectionFactory connectionFactory;

    private final ThrottledSubmissionHandlerConfigurationContext context;

    @Override
    public void receive(final ThrottledSubmission submission) throws Exception {
        val ldapProperties = context.getCasProperties().getAuthn().getThrottle().getLdap();

        val searchFilter = '(' + ldapProperties.getSearchFilter() + ')';
        val filter = LdapUtils.newLdaptiveSearchFilter(searchFilter, CollectionUtils.wrapList(submission.getUsername()));
        val response = connectionFactory.executeSearchOperation(ldapProperties.getBaseDn(),
            filter, ldapProperties.getPageSize(), ReturnAttributes.NONE.value());
        if (LdapUtils.containsResultEntry(response)) {
            val entry = response.getEntry();
            LOGGER.debug("Locating LDAP entry [{}]", entry);
            val attrMap = new HashMap<String, Set<String>>();
            attrMap.put(ldapProperties.getAccountLockedAttribute(), Set.of(Boolean.TRUE.toString()));
            connectionFactory.executeModifyOperation(entry.getDn(), CollectionUtils.wrap(attrMap));
        }
    }
}
