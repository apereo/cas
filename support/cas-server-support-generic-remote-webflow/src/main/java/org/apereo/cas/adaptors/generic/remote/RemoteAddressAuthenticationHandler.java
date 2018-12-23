package org.apereo.cas.adaptors.generic.remote;

import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

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
@Slf4j
@Setter
@Getter
public class RemoteAddressAuthenticationHandler extends AbstractAuthenticationHandler {

    private static final int HEX_RIGHT_SHIFT_COEFFICIENT = 0xff;

    /**
     * The network netmask.
     */
    private InetAddress inetNetmask;

    /**
     * The network base address.
     */
    private InetAddress inetNetworkRange;

    public RemoteAddressAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                              final PrincipalFactory principalFactory,
                                              final Integer order) {
        super(name, servicesManager, principalFactory, order);
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
        val networkBytes = network.getAddress();
        val netmaskBytes = netmask.getAddress();
        val ipBytes = ip.getAddress();
        /* check IPv4/v6-compatibility or parameters: */
        if (networkBytes.length != netmaskBytes.length || netmaskBytes.length != ipBytes.length) {
            LOGGER.debug("Network address [{}], subnet mask [{}] and/or host address [{}]" + " have different sizes! (return false ...)", network, netmask, ip);
            return false;
        }
        /* Check if the masked network and ip addresses match: */
        for (var i = 0; i < netmaskBytes.length; i++) {
            val mask = netmaskBytes[i] & HEX_RIGHT_SHIFT_COEFFICIENT;
            if ((networkBytes[i] & mask) != (ipBytes[i] & mask)) {
                LOGGER.debug("[{}] is not in [{}]/[{}]", ip, network, netmask);
                return false;
            }
        }
        LOGGER.debug("[{}] is in [{}]/[{}]", ip, network, netmask);
        return true;
    }

    @Override
    public AuthenticationHandlerExecutionResult authenticate(final Credential credential) throws GeneralSecurityException {
        val c = (RemoteAddressCredential) credential;
        if (this.inetNetmask != null && this.inetNetworkRange != null) {
            try {
                val inetAddress = InetAddress.getByName(c.getRemoteAddress().trim());
                if (containsAddress(this.inetNetworkRange, this.inetNetmask, inetAddress)) {
                    return new DefaultAuthenticationHandlerExecutionResult(this, c, this.principalFactory.createPrincipal(c.getId()));
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

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return RemoteAddressCredential.class.isAssignableFrom(clazz);
    }

    /**
     * Sets ip network range.
     *
     * @param ipAddressRange the IP address range that should be allowed trusted logins
     */
    public void configureIpNetworkRange(final String ipAddressRange) {
        if (StringUtils.isNotBlank(ipAddressRange)) {
            val splitAddress = Splitter.on("/").splitToList(ipAddressRange);
            if (splitAddress.size() == 2) {
                val network = splitAddress.get(0).trim();
                val netmask = splitAddress.get(1).trim();

                try {
                    this.inetNetworkRange = InetAddress.getByName(network);
                    LOGGER.debug("InetAddress network: [{}]", this.inetNetworkRange.toString());
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
