package org.apereo.cas.adaptors.radius.server;

import org.apereo.cas.adaptors.radius.CasRadiusResponse;
import org.apereo.cas.adaptors.radius.RadiusServer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jradius.client.RadiusClient;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.dictionary.Attr_ClientIPAddress;
import net.jradius.dictionary.Attr_NASIPAddress;
import net.jradius.dictionary.Attr_NASIPv6Address;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_NASPort;
import net.jradius.dictionary.Attr_NASPortId;
import net.jradius.dictionary.Attr_NASPortType;
import net.jradius.dictionary.Attr_State;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.dictionary.vsa_redback.Attr_NASRealPort;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessChallenge;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.Serializable;
import java.security.Security;
import java.util.Optional;

/**
 * Implementation of a RadiusServer that utilizes the JRadius packages available
 * at <a href="http://jradius.sf.net">http://jradius.sf.net</a>.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 3.1
 */
@Slf4j
@ToString
@Getter
@RequiredArgsConstructor
public abstract class AbstractRadiusServer implements RadiusServer {

    /**
     * Default retry count, {@value}.
     **/
    public static final int DEFAULT_RETRY_COUNT = 3;

    private static final long serialVersionUID = -7122734096722096617L;

    static {
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
        Security.addProvider(new BouncyCastleProvider());
    }

    private final RadiusServerConfigurationContext radiusServerConfigurationContext;

    @Override
    public final CasRadiusResponse authenticate(final String username, final String password, final Optional state) throws Exception {
        val attributeList = new AttributeList();

        if (StringUtils.isNotBlank(username)) {
            attributeList.add(new Attr_UserName(username));
        }

        if (StringUtils.isNotBlank(password)) {
            attributeList.add(new Attr_UserPassword(password));
        }

        val clientInfo = ClientInfoHolder.getClientInfo();
        if (clientInfo != null) {
            val clientIpAddress = clientInfo.getClientIpAddress();
            val clientIpAttribute = new Attr_ClientIPAddress(clientIpAddress);
            LOGGER.debug("Adding client IP address attribute [{}]", clientIpAttribute);
            attributeList.add(clientIpAttribute);
        }

        state.ifPresent(value -> attributeList.add(new Attr_State(Serializable.class.cast(value))));

        if (StringUtils.isNotBlank(radiusServerConfigurationContext.getNasIpAddress())) {
            attributeList.add(new Attr_NASIPAddress(radiusServerConfigurationContext.getNasIpAddress()));
        }
        if (StringUtils.isNotBlank(radiusServerConfigurationContext.getNasIpv6Address())) {
            attributeList.add(new Attr_NASIPv6Address(radiusServerConfigurationContext.getNasIpv6Address()));
        }
        if (radiusServerConfigurationContext.getNasPort() != -1) {
            attributeList.add(new Attr_NASPort(radiusServerConfigurationContext.getNasPort()));
        }
        if (radiusServerConfigurationContext.getNasPortId() != -1) {
            attributeList.add(new Attr_NASPortId(radiusServerConfigurationContext.getNasPortId()));
        }
        if (StringUtils.isNotBlank(radiusServerConfigurationContext.getNasIdentifier())) {
            attributeList.add(new Attr_NASIdentifier(radiusServerConfigurationContext.getNasIdentifier()));
        }
        if (radiusServerConfigurationContext.getNasRealPort() != -1) {
            attributeList.add(new Attr_NASRealPort(radiusServerConfigurationContext.getNasRealPort()));
        }
        if (radiusServerConfigurationContext.getNasPortType() != -1) {
            attributeList.add(new Attr_NASPortType(radiusServerConfigurationContext.getNasPortType()));
        }
        val client = radiusServerConfigurationContext.getRadiusClientFactory().newInstance();
        try {
            val request = new AccessRequest(client, attributeList);
            LOGGER.debug("RADIUS access request prepared as [{}]", request.toString(true, true));

            val response = authenticateRequest(client, request);
            LOGGER.debug("RADIUS response from [{}]: [{}] as [{}]", client.getRemoteInetAddress().getCanonicalHostName(),
                response.getClass().getName(), response.toString(true, true));

            if (response instanceof AccessAccept || response instanceof AccessChallenge) {
                val attributes = response.getAttributes().getAttributeList();
                LOGGER.debug("Radius response code [{}] accepted with attributes [{}] and identifier [{}]",
                    response.getCode(), attributes, response.getIdentifier());
                return new CasRadiusResponse(response.getCode(), response.getIdentifier(), attributes);
            }
            LOGGER.warn("Response is not recognized");
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return null;
    }

    /**
     * Gets radius authenticator.
     *
     * @return the radius authenticator
     */
    public RadiusAuthenticator getRadiusAuthenticator() {
        return RadiusClient.getAuthProtocol(radiusServerConfigurationContext.getProtocol().getName());
    }

    /**
     * Authenticate request and produce a response.
     *
     * @param client        the client
     * @param accessRequest the access request
     * @return the radius response
     * @throws Exception the exception
     */
    protected abstract RadiusResponse authenticateRequest(RadiusClient client, AccessRequest accessRequest) throws Exception;

}
