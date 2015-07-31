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

package org.jasig.cas.services.web.beans;

import java.io.Serializable;

/**
 * Registered service public key options.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServicePublicKeyEditBean implements Serializable {
    private static final long serialVersionUID = 2553270792452015226L;

    /**
     * The enum Algorithm types.
     */
    public enum AlgorithmTypes {
        /**
         * rsa type.
         */
        RSA("RSA");

        private final String value;

        /**
         * Instantiates a new AlgorithmTypes.
         *
         * @param value the value
         */
        AlgorithmTypes(final String value) {
            this.value = value;
        }
    }

    private String location;
    private AlgorithmTypes algorithm = AlgorithmTypes.RSA;

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public AlgorithmTypes getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(final AlgorithmTypes algorithm) {
        this.algorithm = algorithm;
    }
}
