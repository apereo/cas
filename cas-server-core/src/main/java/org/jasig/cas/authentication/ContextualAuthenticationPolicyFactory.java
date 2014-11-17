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
package org.jasig.cas.authentication;

/**
 * A factory for producing (stateful) authentication policies based on arbitrary context data.
 * This component provides a way to inject stateless factories into components that produce stateful
 * authentication policies that can leverage arbitrary contextual information to evaluate security policy.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public interface ContextualAuthenticationPolicyFactory<T> {

    /**
     * Creates a contextual (presumably stateful) authentication policy based on provided context data.
     *
     * @param context Context data used to create an authentication policy.
     *
     * @return Contextual authentication policy object. The returned object should be assumed to be stateful
     * and not thread safe unless explicitly noted otherwise.
     */
    ContextualAuthenticationPolicy<T> createPolicy(T context);
}
