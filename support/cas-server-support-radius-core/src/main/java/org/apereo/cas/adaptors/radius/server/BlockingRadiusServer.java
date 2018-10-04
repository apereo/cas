package org.apereo.cas.adaptors.radius.server;

import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.jradius.client.RadiusClient;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusResponse;

/**
 * Implementation of a RadiusServer that utilizes the JRadius packages available
 * at <a href="http://jradius.sf.net">http://jradius.sf.net</a>.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@ToString
@Setter
public class BlockingRadiusServer extends AbstractRadiusServer {
    private static final long serialVersionUID = -2567137135937670129L;

    public BlockingRadiusServer(final RadiusProtocol protocol, final RadiusClientFactory radiusClientFactory) {
        super(protocol, radiusClientFactory, 1, null,
            null, -1, -1, null, -1, -1);
    }

    public BlockingRadiusServer(final RadiusProtocol protocol, final RadiusClientFactory clientFactory, final int retries,
                                final String nasIpAddress, final String nasIpv6Address, final long nasPort,
                                final long nasPortId, final String nasIdentifier, final long nasRealPort, final long nasPortType) {
        super(protocol, clientFactory, retries, nasIpAddress, nasIpv6Address, nasPort, nasPortId, nasIdentifier, nasRealPort, nasPortType);
    }

    @Override
    protected RadiusResponse authenticateRequest(final RadiusClient client, final AccessRequest accessRequest) throws Exception {
        return client.authenticate(accessRequest, getRadiusAuthenticator(), getRetries());
    }
}
