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
package org.jasig.cas.authentication.handler;


/**
 * Strategy pattern component for transforming principal names in the authentication pipeline.
 *
 * @author Howard Gilbert
 * @since 3.3.6
 */
public interface PrincipalNameTransformer {

    /**
     * Transform the string typed into the login form into a tentative Principal Name to be
     * validated by a specific type of Authentication Handler.
     *
     * <p>The Principal Name eventually assigned by the PrincipalResolver may
     * be unqualified ("AENewman"). However, validation of the Principal name against a
     * particular backend source represented by a particular Authentication Handler may
     * require transformation to a temporary fully qualified format such as
     * AENewman@MAD.DCCOMICS.COM or MAD\AENewman. After validation, this form of the
     * Principal name is discarded in favor of the choice made by the Resolver.
     *
     * @param formUserId The raw userid typed into the login form
     * @return the string that the Authentication Handler should lookup in the backend system
     */
    String transform(String formUserId);
}

