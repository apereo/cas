package org.apereo.cas.web.flow.client;

import org.apereo.cas.support.spnego.util.ReverseDNSRunnable;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.regex.Pattern;

/**
 * Abstract class for defining a simple binary filter to determine whether a
 * given client system should be prompted for SPNEGO / KRB / NTLM credentials.
 * <p>
 * Envisioned implementations would include LDAP and DNS based determinations,
 * but of course others may have value as well for local architectures.
 *
 * @author Sean Baker sean.baker@usuhs.edu
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@ToString
@Setter
@Getter
@AllArgsConstructor
public class BaseSpnegoKnownClientSystemsFilterAction extends AbstractAction {

    /**
     * Pattern of ip addresses to check.
     **/
    private Pattern ipsToCheckPattern;

    /**
     * Alternative remote host attribute.
     **/
    private String alternativeRemoteHostAttribute;

    /**
     * Set timeout (ms) for DNS requests; valuable for heterogeneous environments employing
     * fall-through authentication mechanisms.
     */
    private long timeout;

    /**
     * Instantiates a new Base.
     *
     * @param ipsToCheckPattern the ips to check pattern
     */
    public BaseSpnegoKnownClientSystemsFilterAction(final String ipsToCheckPattern) {
        setIpsToCheckPattern(RegexUtils.createPattern(ipsToCheckPattern));
    }


    /**
     * {@inheritDoc}
     * Gets the remote ip from the request, and invokes spnego if it isn't filtered.
     *
     * @param context the request context
     * @return {@link #yes()} if spnego should be invoked and ip isn't filtered,
     * {@link #no()} otherwise.
     */
    @Override
    protected Event doExecute(final RequestContext context) {
        val remoteIp = getRemoteIp(context);
        LOGGER.debug("Current user IP [{}]", remoteIp);
        if (shouldDoSpnego(remoteIp)) {
            LOGGER.info("Spnego should be activated for [{}]", remoteIp);
            return yes();
        }
        LOGGER.info("Spnego should is skipped for [{}]", remoteIp);
        return no();
    }

    /**
     * Default implementation -- simply check the IP filter.
     *
     * @param remoteIp the remote ip
     * @return true boolean
     */
    protected boolean shouldDoSpnego(final String remoteIp) {
        return ipPatternCanBeChecked(remoteIp) && ipPatternMatches(remoteIp);
    }

    /**
     * Base class definition for whether the IP should be checked or not; overridable.
     *
     * @param remoteIp the remote ip
     * @return whether or not the IP can / should be matched against the pattern
     */
    protected boolean ipPatternCanBeChecked(final String remoteIp) {
        return this.ipsToCheckPattern != null && StringUtils.isNotBlank(remoteIp);
    }

    /**
     * Simple pattern match to determine whether an IP should be checked.
     * Could stand to be extended to support "real" IP addresses and patterns, but
     * for the local / first implementation regex made more sense.
     *
     * @param remoteIp the remote ip
     * @return whether the remote ip received should be queried
     */
    protected boolean ipPatternMatches(final String remoteIp) {
        val matcher = this.ipsToCheckPattern.matcher(remoteIp);
        if (matcher.find()) {
            LOGGER.debug("Remote IP address [{}] should be checked based on the defined pattern [{}]", remoteIp, this.ipsToCheckPattern.pattern());
            return true;
        }
        LOGGER.debug("No pattern or remote IP defined, or pattern does not match remote IP [{}]", remoteIp);
        return false;
    }

    /**
     * Pulls the remote IP from the current HttpServletRequest, or grabs the value
     * for the specified alternative attribute (say, for proxied requests).  Falls
     * back to providing the "normal" remote address if no value can be retrieved
     * from the specified alternative header value.
     *
     * @param context the context
     * @return the remote ip
     */
    private String getRemoteIp(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        var userAddress = request.getRemoteAddr();
        LOGGER.debug("Remote Address = [{}]", userAddress);
        if (StringUtils.isNotBlank(this.alternativeRemoteHostAttribute)) {
            userAddress = request.getHeader(this.alternativeRemoteHostAttribute);
            LOGGER.debug("Header Attribute [{}] = [{}]", this.alternativeRemoteHostAttribute, userAddress);
            if (StringUtils.isBlank(userAddress)) {
                userAddress = request.getRemoteAddr();
                LOGGER.warn("No value could be retrieved from the header [{}]. Falling back to [{}].", this.alternativeRemoteHostAttribute, userAddress);
            }
        }
        return userAddress;
    }

    /**
     * Convenience method to perform a reverse DNS lookup. Threads the request
     * through a custom Runnable class in order to prevent inordinately long
     * user waits while performing reverse lookup.
     *
     * @param remoteIp the remote ip
     * @return the remote host name
     */
    protected String getRemoteHostName(final String remoteIp) {
        val revDNS = new ReverseDNSRunnable(remoteIp);
        val t = new Thread(revDNS);
        t.start();
        try {
            t.join(this.timeout);
        } catch (final InterruptedException e) {
            LOGGER.debug("Threaded lookup failed. Defaulting to IP [{}].", remoteIp, e);
            Thread.currentThread().interrupt();
        }
        val remoteHostName = revDNS.getHostName();
        LOGGER.debug("Found remote host name [{}].", remoteHostName);
        return StringUtils.isNotBlank(remoteHostName) ? remoteHostName : remoteIp;
    }
}
