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
package org.jasig.cas;

/**
 * Class that exposes relevant constants and parameters to
 * the CAS protocol. These include attribute names, pre-defined
 * values and expected request parameter names as is specified
 * by the protocol.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface CasProtocolConstants {

    /** CAS Protocol Validation Fields. **/

    /** Constant representing the PGT in the cas validation model. */
    String VALIDATION_CAS_MODEL_PROXY_GRANTING_TICKET = "proxyGrantingTicket";

    /** Constant representing the PGTIOU in the cas validation model. */
    String VALIDATION_CAS_MODEL_PROXY_GRANTING_TICKET_IOU = "pgtIou";

    /** Constant representing the remember-me long term token in the validation payload. */
    String VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME = "longTermAuthenticationRequestTokenUsed";

    /** Represents the collection of attributes in the view. */
    String VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_ATTRIBUTES = "attributes";

    /** Represents the authentication date object in the view. */
    String VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE = "authenticationDate";

    /** Represents the flag to note whether assertion is backed by new login. */
    String VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN = "isFromNewLogin";

    /** CAS Protocol Parameters. **/

    /** Constant representing the proxy callback url parameter in the request. */
    String PARAMETER_PROXY_CALLBACK_URL = "pgtUrl";

    /** Constant representing the renew parameter in the request. */
    String PARAMETER_RENEW = "renew";

    /** CAS Protocol Error Codes. **/

    /** Constant representing an invalid request for validation. */
    String ERROR_CODE_INVALID_REQUEST = "INVALID_REQUEST";
    
    /** Constant representing an invalid proxy callback for validation. */
    String ERROR_CODE_INVALID_PROXY_CALLBACK = "INVALID_PROXY_CALLBACK";
    
    /** Constant representing an invalid ticket for validation. */
    String ERROR_CODE_INVALID_TICKET = "INVALID_TICKET";
}
