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

import org.jasig.cas.authentication.RootCasException;

import javax.validation.constraints.NotNull;

/**
 * Represents the root SAML exception.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlException extends RootCasException {
    /** Code description. */
    public static final String CODE = "UNSATISFIED_SAML_REQUEST";

    private static final long serialVersionUID = 801270467754480446L;

    /**
     * Instantiates a new Saml exception.
     *
     * @param code the code
     */
    public SamlException(@NotNull final String code) {
        super(code);
    }

    /**
     * Instantiates a new Saml exception.
     *
     * @param code the code
     * @param msg  the msg
     */
    public SamlException(@NotNull final String code, final String msg) {
        super(code, msg);
    }

    /**
     * Instantiates a new Saml exception.
     *
     * @param code      the code
     * @param throwable the throwable
     */
    public SamlException(@NotNull final String code, final Throwable throwable) {
        super(code, throwable);
    }
}
