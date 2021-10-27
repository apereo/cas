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

package org.jasig.cas.web.view;

import org.jasig.cas.CasProtocolConstants;

/**
 * Constants interface to host fields
 * related to view rendering and validation model.
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface CasViewConstants {

    /**
     * Represents the flag to note the principal credential used to establish
     * a successful authentication event.
     */
    String MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL = "credential";

    /**
     * Represents the
     * {@link org.jasig.cas.authentication.principal.Principal} object in the view.
     */
    String MODEL_ATTRIBUTE_NAME_PRINCIPAL = "principal";

    /**
     * Represents the chained authentication objects
     * in the view for proxying.
     */
    String MODEL_ATTRIBUTE_NAME_CHAINED_AUTHENTICATIONS = "chainedAuthentications";

    /**
     *  Represents the
     * {@link org.jasig.cas.authentication.Authentication} object in the view.
     **/
    String MODEL_ATTRIBUTE_NAME_PRIMARY_AUTHENTICATION = "primaryAuthentication";

    /** Constant representing the Assertion in the cas validation model. */
    String MODEL_ATTRIBUTE_NAME_ASSERTION = "assertion";

    /** The constant representing the validated service in the response. */
    String MODEL_ATTRIBUTE_NAME_SERVICE = "service";

    /** The constant representing the PGTIOU in the response. */
    String MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET_IOU = CasProtocolConstants.VALIDATION_CAS_MODEL_PROXY_GRANTING_TICKET_IOU;

    /** The constant representing the PGT in the response. */
    String MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET = CasProtocolConstants.VALIDATION_CAS_MODEL_PROXY_GRANTING_TICKET;
}
