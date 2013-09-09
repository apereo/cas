/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.adaptors.radius;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import net.jradius.client.RadiusClient;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.exception.RadiusException;
import net.jradius.exception.UnknownAttributeException;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import org.jasig.cas.authentication.PreventedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a RadiusServer that utilizes the JRadius packages available
 * at <a href="http://jradius.sf.net">http://jradius.sf.net</a>.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public final class JRadiusServerImpl implements RadiusServer {

    /** Default retry count, {@value}. */
    public static final int DEFAULT_RETRY_COUNT = 3;

    /** Logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(JRadiusServerImpl.class);

    /** RADIUS protocol. */
    @NotNull
    private final RadiusProtocol protocol;

    /** Produces RADIUS client instances for authentication. */
    @NotNull
    private final RadiusClientFactory radiusClientFactory;

    /** Number of times to retry authentication when no response is received. */
    @Min(0)
    private int retries = DEFAULT_RETRY_COUNT;

    /** Load the dictionary implementation. */
    static {
        AttributeFactory
        .loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
    }

    public JRadiusServerImpl(final RadiusProtocol protocol, final RadiusClientFactory clientFactory) {
        this.protocol = protocol;
        this.radiusClientFactory = clientFactory;
    }

    @Override
    public boolean authenticate(final String username, final String password) throws PreventedException {

        final AttributeList attributeList = new AttributeList();
        attributeList.add(new Attr_UserName(username));
        attributeList.add(new Attr_UserPassword(password));

        final RadiusClient client = this.radiusClientFactory.newInstance();
        try {
            final AccessRequest request = new AccessRequest(client, attributeList);
            final RadiusPacket response = client.authenticate(
                    request,
                    RadiusClient.getAuthProtocol(this.protocol.getName()),
                    this.retries);

            LOGGER.debug("RADIUS response from {}: {}",
                    client.getRemoteInetAddress().getCanonicalHostName(),
                    response.getClass().getName());

            if (response instanceof AccessAccept) {
                return true;
            }
        } catch (final UnknownAttributeException e) {
            throw new PreventedException(e);
        } catch (final RadiusException e) {
            throw new PreventedException(e);
        } finally {
            client.close();
        }
        return false;
    }
}
