package org.apereo.cas.adaptors.radius.authentication;

import net.jradius.exception.TimeoutException;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.RadiusMultifactorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.util.List;

/**
 * The authentication provider for yubikey.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RadiusMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(RadiusMultifactorAuthenticationProvider.class);
    private static final long serialVersionUID = 4789727148634156909L;

    private List<RadiusServer> servers;

    public RadiusMultifactorAuthenticationProvider() {
    }
    
    public RadiusMultifactorAuthenticationProvider(final List<RadiusServer> servers) {
        this.servers = servers;
    }

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), RadiusMultifactorProperties.DEFAULT_IDENTIFIER);
    }
    
    @Override
    protected boolean isAvailable() {
        return canPing();
    }

    @Override
    public String getFriendlyName() {
        return "RADIUS (RSA,WiKID)";
    }

    /**
     * Can ping.
     *
     * @return true/false
     */
    public boolean canPing() {
        final String uidPsw = getClass().getSimpleName();
        for (final RadiusServer server : this.servers) {
            LOGGER.debug("Attempting to ping RADIUS server [{}] via simulating an authentication request. If the server responds "
                    + "successfully, mock authentication will fail correctly.", server);
            try {
                server.authenticate(uidPsw, uidPsw);
            } catch (final TimeoutException | SocketTimeoutException e) {
                LOGGER.debug("Server [{}] is not available", server);
                continue;
            } catch (final Exception e) {
                LOGGER.debug("Pinging RADIUS server was successful. Response [{}]", e.getMessage());
            }
            return true;
        }
        return false;
    }
}
