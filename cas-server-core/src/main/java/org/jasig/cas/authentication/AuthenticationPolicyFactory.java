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
 * A factory for producing authentication policies based on arbitrary context data.
 * The policies created by these components may be stateful; for exmaple, their state may be based
 * on data provided by the context used to create the policy.
 *
 * @author Marvin S. Addison
 * @version 4.0
 */
public interface AuthenticationPolicyFactory<T> {

    /**
     * Creates a (possibly stateful) authentication policy based on provided context data.
     *
     * @param context Context data used to create an authentication policy.
     *
     * @return Authentication policy object. In many cases this object will be stateful and therefore not
     * thread safe unless explicitly noted otherwise.
     */
    AuthenticationPolicy createPolicy(T context);
}
