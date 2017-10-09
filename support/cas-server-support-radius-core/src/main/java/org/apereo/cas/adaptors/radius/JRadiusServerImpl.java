package org.apereo.cas.adaptors.radius;


import net.jradius.client.RadiusClient;
import net.jradius.dictionary.Attr_NASIPAddress;
import net.jradius.dictionary.Attr_NASIPv6Address;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_NASPort;
import net.jradius.dictionary.Attr_NASPortId;
import net.jradius.dictionary.Attr_NASPortType;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.dictionary.vsa_redback.Attr_NASRealPort;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.util.List;

/**
 * Implementation of a RadiusServer that utilizes the JRadius packages available
 * at <a href="http://jradius.sf.net">http://jradius.sf.net</a>.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 3.1
 */
public class JRadiusServerImpl implements RadiusServer {

    /**
     * Default retry count, {@value}.
     **/
    public static final int DEFAULT_RETRY_COUNT = 3;

    /** Logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(JRadiusServerImpl.class);

    /** RADIUS protocol. */
    
    private final RadiusProtocol protocol;

    /** Produces RADIUS client instances for authentication. */
    
    private final RadiusClientFactory radiusClientFactory;

    /** Number of times to retry authentication when no response is received. */
    
    private int retries = DEFAULT_RETRY_COUNT;

    private final String nasIpAddress;
    
    private final String nasIpv6Address;
    
    private long nasPort = -1;
    
    private long nasPortId = -1;
    
    private final String nasIdentifier;
    
    private long nasRealPort = -1;
    
    private long nasPortType = -1;
    
    static {
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Instantiates a new J radius server.
     *
     * @param protocol            the protocol
     * @param radiusClientFactory the radius client factory
     */
    public JRadiusServerImpl(final RadiusProtocol protocol, final RadiusClientFactory radiusClientFactory) {
        this(protocol, radiusClientFactory, 1, null, null, -1, -1, null, -1);
    }

    /**
     * Instantiates a new server implementation
     * with the radius protocol and client factory specified. 
     *
     * @param protocol the protocol
     * @param clientFactory the client factory
     * @param retries the new retries
     * @param nasIpAddress the new nas ip address
     * @param nasIpv6Address the new nas ipv6 address
     * @param nasPort the new nas port
     * @param nasPortId the new nas port id
     * @param nasIdentifier the new nas identifier
     * @param nasRealPort the new nas real port
     */
    public JRadiusServerImpl(final RadiusProtocol protocol, final RadiusClientFactory clientFactory, final int retries, final String nasIpAddress,
                             final String nasIpv6Address, final long nasPort, final long nasPortId, final String nasIdentifier, final long nasRealPort) {
        this.protocol = protocol;
        this.radiusClientFactory = clientFactory;
        this.retries = retries;
        this.nasIpAddress = nasIpAddress;
        this.nasIpv6Address = nasIpv6Address;
        this.nasPort = nasPort;
        this.nasPortId = nasPortId;
        this.nasIdentifier = nasIdentifier;
        this.nasRealPort = nasRealPort;
    }

    @Override
    public RadiusResponse authenticate(final String username, final String password) throws Exception {

        final AttributeList attributeList = new AttributeList();
        
        attributeList.add(new Attr_UserName(username));
        attributeList.add(new Attr_UserPassword(password));

        if (StringUtils.isNotBlank(this.nasIpAddress)) {
            attributeList.add(new Attr_NASIPAddress(this.nasIpAddress));
        }
        if (StringUtils.isNotBlank(this.nasIpv6Address)) {
            attributeList.add(new Attr_NASIPv6Address(this.nasIpv6Address));
        }

        if (this.nasPort != -1) {
            attributeList.add(new Attr_NASPort(this.nasPort));
        }
        if (this.nasPortId != -1) {
            attributeList.add(new Attr_NASPortId(this.nasPortId));
        }
        if (StringUtils.isNotBlank(this.nasIdentifier)) {
            attributeList.add(new Attr_NASIdentifier(this.nasIdentifier));
        }
        if (this.nasRealPort != -1) {
            attributeList.add(new Attr_NASRealPort(this.nasRealPort));
        }
        if (this.nasPortType != -1) {
            attributeList.add(new Attr_NASPortType(this.nasPortType));
        }
        
        RadiusClient client = null;
        try {
            client = this.radiusClientFactory.newInstance();
            final AccessRequest request = new AccessRequest(client, attributeList);
            final RadiusPacket response = client.authenticate(
                    request,
                    RadiusClient.getAuthProtocol(this.protocol.getName()),
                    this.retries);

            LOGGER.debug("RADIUS response from [{}]: [{}]",
                    client.getRemoteInetAddress().getCanonicalHostName(),
                    response.getClass().getName());

            if (response instanceof AccessAccept) {
                final List<RadiusAttribute> attributes = response.getAttributes().getAttributeList();
                LOGGER.debug("Radius response code [{}] accepted with attributes [{}] and identifier [{}]",
                        response.getCode(), attributes, response.getIdentifier());
                
                return new RadiusResponse(response.getCode(),
                        response.getIdentifier(),
                        attributes);
            }
            LOGGER.debug("Response is not recognized");
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return null;
    }

    /**
     * Sets the nas port type.
     *
     * @param nasPortType the new nas port type
     * @since 4.1.0
     */
    public void setNasPortType(final long nasPortType) {
        this.nasPortType = nasPortType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("protocol", this.protocol)
                .append("radiusClientFactory", this.radiusClientFactory)
                .append("retries", this.retries)
                .append("nasIpAddress", this.nasIpAddress)
                .append("nasIpv6Address", this.nasIpv6Address)
                .append("nasPort", this.nasPort)
                .append("nasPortId", this.nasPortId)
                .append("nasIdentifier", this.nasIdentifier)
                .append("nasRealPort", this.nasRealPort)
                .append("nasPortType", this.nasPortType)
                .toString();
    }
}
