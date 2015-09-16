/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
 * the SAML protocol. These include attribute names, pre-defined
 * values and expected request parameter names as is specified
 * by the protocol.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface SamlProtocolConstants {
    /** Constant representing the saml request. */
    String PARAMETER_SAML_REQUEST = "SAMLRequest";

    /** Constant representing the saml response. */
    String PARAMETER_SAML_RESPONSE = "SAMLResponse";

    /** Constant representing the saml relay state. */
    String PARAMETER_SAML_RELAY_STATE = "RelayState";

    /** Constant representing artifact. */
    String CONST_PARAM_ARTIFACT = "SAMLart";

    /** Constant representing service. */
    String CONST_PARAM_TARGET = "TARGET";

}
