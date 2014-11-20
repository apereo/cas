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

package org.jasig.cas.support.saml;

/**
 * Class that exposes relevant constants and parameters to
 * the Saml protocol. These include attribute names, pre-defined
 * values and expected request parameter names as is specified
 * by the protocol.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface SamlProtocolConstants {
    /** Constant representing the SAML request. */
    String SAML2_PARAM_SERVICE = "SAMLRequest";

    /** Constant representing the SAML relay state. */
    String SAML2_RELAY_STATE = "RelayState";

    /** Constant representing service. */
    String SAML1_PARAM_SERVICE = "TARGET";

    /** Constant representing artifact. */
    String SAML1_PARAM_TICKET = "SAMLart";
}

