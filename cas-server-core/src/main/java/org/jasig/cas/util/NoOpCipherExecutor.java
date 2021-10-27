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

package org.jasig.cas.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-Op cipher executor that does nothing for encryption/decryption.
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class NoOpCipherExecutor extends AbstractCipherExecutor<String, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpCipherExecutor.class);

    /**
     * Instantiates a new No-Op cipher executor.
     * Issues a warning on safety.
     */
    public NoOpCipherExecutor() {
        super(NoOpCipherExecutor.class.getName());
        LOGGER.warn("[{}] does no encryption and may NOT be safe in a production environment. "
                + "Consider using other choices, such as [{}] that handle encryption, signing and verification of "
                + "all appropriate values.", this.getClass().getName(), BaseStringCipherExecutor.class.getName());
    }

    @Override
    public String encode(final String value) {
        return value;
    }

    @Override
    public String decode(final String value) {
        return value;
    }
}
