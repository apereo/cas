package org.apereo.cas.adaptors.radius.server;

import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;

import lombok.Setter;
import lombok.ToString;
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
@ToString
@Setter
public class BlockingRadiusServer extends AbstractRadiusServer {
    private static final long serialVersionUID = -2567137135937670129L;

    public BlockingRadiusServer(final RadiusProtocol protocol, final RadiusClientFactory radiusClientFactory) {
        super(RadiusServerConfigurationContext.builder()
            .protocol(protocol)
            .radiusClientFactory(radiusClientFactory)
            .retries(1)
            .build()
        );
    }

    public BlockingRadiusServer(final RadiusServerConfigurationContext radiusServerConfigurationContext) {
        super(radiusServerConfigurationContext);
    }

    @Override
    protected RadiusResponse authenticateRequest(final RadiusClient client, final AccessRequest accessRequest) throws Exception {
        return client.authenticate(accessRequest, getRadiusAuthenticator(), getRadiusServerConfigurationContext().getRetries());
    }
}
