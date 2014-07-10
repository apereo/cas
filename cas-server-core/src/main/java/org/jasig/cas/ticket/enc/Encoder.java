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

package org.jasig.cas.ticket.enc;

import java.io.Serializable;

/**
 * Describes an encoding strategy to generate a string representation of a
 * serializable object.  One-way hashing and encryption are potential
 * implementations.
 *
 * @author Marvin S. Addison
 * @since 3.4.4
 *
 */
public interface Encoder {

    /**
     * Encodes the given serializable object as a string.
     *
     * @param object Serializable source object MUST NOT be null.
     *
     * @return String representation of given object.
     */
    String encode(Serializable object);

    /**
     * Encodes raw byte data into a string representation.
     *
     * @param bytes Input bytes.
     *
     * @return Encoded bytes.
     */
    String encode(byte[] bytes);
}
