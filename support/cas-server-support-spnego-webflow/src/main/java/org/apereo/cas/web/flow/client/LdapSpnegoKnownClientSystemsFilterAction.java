package org.apereo.cas.web.flow.client;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ldaptive.LdapAttribute;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchResponse;
import org.springframework.beans.factory.DisposableBean;

import java.util.regex.Pattern;

/**
 * Peek into an LDAP server and check for the existence of an attribute
 * in order to target invocation of spnego.
 *
 * @author Misagh Moayyed
 * @author Sean Baker
 * @since 4.1
 */
@Slf4j
public class LdapSpnegoKnownClientSystemsFilterAction extends BaseSpnegoKnownClientSystemsFilterAction implements DisposableBean {

    /**
     * The must-have attribute name.
     */
    private final String spnegoAttributeName;

    private final SearchOperation searchOperation;

    /**
     * Instantiates a new action.
     *
     * @param ipsToCheckPattern              the ips to check pattern
     * @param alternativeRemoteHostAttribute the alternative remote host attribute
     * @param dnsTimeout                     # of milliseconds to wait for a DNS request to return
     * @param searchOperation                the search operation
     * @param spnegoAttributeName            the certificate revocation list attribute name
     */
    public LdapSpnegoKnownClientSystemsFilterAction(final Pattern ipsToCheckPattern,
                                                    final String alternativeRemoteHostAttribute,
                                                    final long dnsTimeout,
                                                    final SearchOperation searchOperation,
                                                    final String spnegoAttributeName) {
        super(ipsToCheckPattern, alternativeRemoteHostAttribute, dnsTimeout);
        this.spnegoAttributeName = spnegoAttributeName;
        this.searchOperation = searchOperation;
    }

    @Override
    protected boolean shouldDoSpnego(final String remoteIp) {

        if (StringUtils.isBlank(this.spnegoAttributeName)) {
            LOGGER.warn("Ignoring Spnego. Attribute name is not configured");
            return false;
        }

        if (this.searchOperation == null) {
            LOGGER.warn("Ignoring Spnego. LDAP search operation is not configured");
            return false;
        }

        val ipCheck = ipPatternCanBeChecked(remoteIp);
        if (ipCheck && !ipPatternMatches(remoteIp)) {
            return false;
        }
        LOGGER.debug("Attempting to locate attribute [{}] for [{}]", this.spnegoAttributeName, remoteIp);
        return executeSearchForSpnegoAttribute(remoteIp);
    }

    @Override
    protected String getRemoteHostName(final String remoteIp) {
        if ("localhost".equalsIgnoreCase(remoteIp) || remoteIp.startsWith("127.")) {
            return remoteIp;
        }
        return super.getRemoteHostName(remoteIp);
    }

    /**
     * Searches the ldap instance for the attribute value.
     *
     * @param remoteIp the remote ip
     * @return true/false
     */
    @SneakyThrows
    protected boolean executeSearchForSpnegoAttribute(final String remoteIp) {
        val remoteHostName = getRemoteHostName(remoteIp);
        LOGGER.debug("Resolved remote hostname [{}] based on ip [{}]", remoteHostName, remoteIp);

        val searchOp = SearchOperation.copy(this.searchOperation);
        searchOp.getTemplate().setParameter("host", remoteHostName);

        LOGGER.debug("Using search filter [{}] on baseDn [{}]",
            searchOp.getTemplate().format(),
            searchOp.getRequest().getBaseDn());

        val searchResult = searchOp.execute();
        if (searchResult.isSuccess()) {
            return processSpnegoAttribute(searchResult);
        }
        throw new IllegalArgumentException("Failed to establish a connection ldap. " + searchResult.getDiagnosticMessage());
    }

    /**
     * Verify spnego attribute value.
     *
     * @param searchResult the search result
     * @return true if attribute value exists and has a value
     */
    protected boolean processSpnegoAttribute(final SearchResponse searchResult) {

        if (searchResult == null || searchResult.getEntries().isEmpty()) {
            LOGGER.debug("Spnego attribute is not found in the search results");
            return false;
        }
        val entry = searchResult.getEntry();
        val attribute = entry.getAttribute(this.spnegoAttributeName);
        LOGGER.debug("Spnego attribute [{}] found as [{}] for [{}]", attribute.getName(), attribute.getStringValue(), entry.getDn());
        return verifySpnegoAttributeValue(attribute);
    }

    /**
     * Verify spnego attribute value.
     * This impl simply makes sure the attribute exists and has a value.
     *
     * @param attribute the ldap attribute
     * @return true if available. false otherwise.
     */
    protected boolean verifySpnegoAttributeValue(final LdapAttribute attribute) {
        return attribute != null && StringUtils.isNotBlank(attribute.getStringValue());
    }

    @Override
    public void destroy() {
        this.searchOperation.getConnectionFactory().close();
    }
}
