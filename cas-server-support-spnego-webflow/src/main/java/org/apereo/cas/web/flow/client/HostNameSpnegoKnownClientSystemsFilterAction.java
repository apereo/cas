package org.apereo.cas.web.flow.client;

import java.util.regex.Pattern;

/**
 * A simple implementation of {@link BaseSpnegoKnownClientSystemsFilterAction} to allow / skip SPNEGO / KRB /
 * NTLM authentication based on a regex match against a reverse DNS lookup of the requesting
 * system.
 *
 * @author Sean Baker
 * @author Misagh Moayyed
 * @since 4.1
 */

public class HostNameSpnegoKnownClientSystemsFilterAction extends BaseSpnegoKnownClientSystemsFilterAction {

    private Pattern hostNamePatternString;

    /**
     * Instantiates a new Host name spnego known client systems filter action.
     */
    public HostNameSpnegoKnownClientSystemsFilterAction() {
    }

    /**
     * Instantiates a new hostname spnego known client systems filter action.
     *
     * @param hostNamePatternString the host name pattern string.
     *                              The pattern to match the retrieved hostname against.
     */
    public HostNameSpnegoKnownClientSystemsFilterAction(
            final String hostNamePatternString) {
        super();
        this.hostNamePatternString = Pattern.compile(hostNamePatternString);
    }

    /**
     * {@inheritDoc}.
     * <p>
     * Checks whether the IP should even be paid attention to,
     * then does a reverse DNS lookup, and if it matches the supplied pattern, performs SPNEGO
     * else skips the process.
     *
     * @param remoteIp The remote ip address to validate
     */
    @Override
    protected boolean shouldDoSpnego(final String remoteIp) {
        final boolean ipCheck = ipPatternCanBeChecked(remoteIp);
        if (ipCheck && !ipPatternMatches(remoteIp)) {
            return false;
        }
        final String hostName = getRemoteHostName(remoteIp);
        logger.debug("Retrieved host name for the remote ip is {}", hostName);
        return this.hostNamePatternString.matcher(hostName).find();
    }
}
