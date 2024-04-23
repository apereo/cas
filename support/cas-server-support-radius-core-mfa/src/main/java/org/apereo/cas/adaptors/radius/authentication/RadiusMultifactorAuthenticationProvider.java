package org.apereo.cas.adaptors.radius.authentication;

import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.RadiusMultifactorAuthenticationProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jradius.exception.TimeoutException;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * The authentication provider for yubikey.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class RadiusMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    @Serial
    private static final long serialVersionUID = 4789727148634156909L;

    private List<RadiusServer> servers;

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), RadiusMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
    }

    @Override
    public boolean isAvailable(final RegisteredService service) {
        return canPing();
    }

    @Override
    public String getFriendlyName() {
        return "RADIUS (RSA,WiKID,etc)";
    }

    /**
     * Can ping.
     *
     * @return true/false
     */
    public boolean canPing() {
        val uidPsw = getClass().getSimpleName();
        for (val server : this.servers) {
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
