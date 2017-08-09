package org.apereo.cas.adaptors.generic.remote;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

/**
 * Checks if the remote address is in the range of allowed addresses.
 *
 * @author David Harrison
 * @author Scott Battaglia
 * @since 3.2.1
 */
public class RemoteAddressAuthenticationHandler extends AbstractAuthenticationHandler {

    private static final int HEX_RIGHT_SHIFT_COEFFICIENT = 0xff;
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteAddressAuthenticationHandler.class);

    /**
     * The network netmask.
     */
    private InetAddress inetNetmask;

    /**
     * The network base address.
     */
    private InetAddress inetNetwork;

    public RemoteAddressAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                              final PrincipalFactory principalFactory) {
        super(name, servicesManager, principalFactory, null);
    }

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException {
        final RemoteAddressCredential c = (RemoteAddressCredential) credential;
        if (this.inetNetmask != null && this.inetNetwork != null) {
            try {
                final InetAddress inetAddress = InetAddress.getByName(c.getRemoteAddress().trim());
                if (containsAddress(this.inetNetwork, this.inetNetmask, inetAddress)) {
                    return new DefaultHandlerResult(this, c, this.principalFactory.createPrincipal(c.getId()));
                }
            } catch (final UnknownHostException e) {
                LOGGER.debug("Unknown host [{}]", c.getRemoteAddress());
            }
        }
        throw new FailedLoginException(c.getRemoteAddress() + " not in allowed range.");
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof RemoteAddressCredential;
    }

    /**
     * Checks if a subnet contains a specific IP address.
     *
     * @param network The network address.
     * @param netmask The subnet mask.
     * @param ip      The IP address to check.
     * @return A boolean value.
     */
    private static boolean containsAddress(final InetAddress network, final InetAddress netmask, final InetAddress ip) {
        LOGGER.debug("Checking IP address: [{}] in [{}] by [{}]", ip, network, netmask);

        final byte[] networkBytes = network.getAddress();
        final byte[] netmaskBytes = netmask.getAddress();
        final byte[] ipBytes = ip.getAddress();

        /* check IPv4/v6-compatibility or parameters: */
        if (networkBytes.length != netmaskBytes.length
                || netmaskBytes.length != ipBytes.length) {
            LOGGER.debug("Network address [{}], subnet mask [{}] and/or host address [{}]"
                    + " have different sizes! (return false ...)", network, netmask, ip);
            return false;
        }

        /* Check if the masked network and ip addresses match: */
        for (int i = 0; i < netmaskBytes.length; i++) {
            final int mask = netmaskBytes[i] & HEX_RIGHT_SHIFT_COEFFICIENT;
            if ((networkBytes[i] & mask) != (ipBytes[i] & mask)) {
                LOGGER.debug("[{}] is not in [{}]/[{}]", ip, network, netmask);
                return false;
            }
        }
        LOGGER.debug("[{}] is in [{}]/[{}]", ip, network, netmask);
        return true;
    }

    /**
     * Sets ip network range.
     *
     * @param ipAddressRange the IP address range that should be allowed trusted logins
     */
    public void setIpNetworkRange(final String ipAddressRange) {

        if (StringUtils.isNotBlank(ipAddressRange)) {

            final String[] splitAddress = ipAddressRange.split("/");

            if (splitAddress.length == 2) {
                // A valid ip address/netmask was supplied parse values
                final String network = splitAddress[0].trim();
                final String netmask = splitAddress[1].trim();

                try {
                    this.inetNetwork = InetAddress.getByName(network);
                    LOGGER.debug("InetAddress network: [{}]", this.inetNetwork.toString());
                } catch (final UnknownHostException e) {
                    LOGGER.error("The network address was not valid: [{}]", e.getMessage());
                }

                try {
                    this.inetNetmask = InetAddress.getByName(netmask);
                    LOGGER.debug("InetAddress netmask: [{}]", this.inetNetmask.toString());
                } catch (final UnknownHostException e) {
                    LOGGER.error("The network netmask was not valid: [{}]", e.getMessage());
                }
            }
        }
    }
}
