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

/**
 * RADIUS protocol enumeration.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public enum RadiusProtocol {
    CHAP("chap"),
    EAP_MD5("eap-md5"),
    EAP_MSCHAPv2("eap-mschapv2"),
    EAP_TLS("eap-tls"),
    EAP_TTLS_PAP("eap-ttls:innerProtocol=pap"),
    EAP_TTLS_EAP_MD5("eap-ttls:innerProtocol=eap-md5"),
    EAP_TTLS_EAP_MSCHAPv2("eap-ttls:innerProtocol=eap-mschapv2"),
    MSCHAPv1("mschapv1"),
    MSCHAPv2("mschapv2"),
    PAP("pap"),
    PEAP("peap");

    private final String name;

    RadiusProtocol(final String name) {
        this.name = name;
    }

    /**
     * Gets the radius protocol name required by {@link net.jradius.client.RadiusClient.getAuthProtocol(String)}.
     *
     * @return RADIUS protocol name known to {@link net.jradius.client.RadiusClient}.
     */
    public String getName() {
        return this.name;
    }
}
