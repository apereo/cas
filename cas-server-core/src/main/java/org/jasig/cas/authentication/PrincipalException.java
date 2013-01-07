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
package org.jasig.cas.authentication;

/**
 * Describes a generic principal resolution failure condition. This class is abstract to require a concrete
 * subclass for each specific cause.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public abstract class PrincipalException extends Exception {

    private static final long serialVersionUID = 4557481605060568728L;

    protected PrincipalException() {}

    protected PrincipalException(final String message) {
        super(message);
    }
}
