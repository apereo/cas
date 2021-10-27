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

import org.jasig.cas.services.ServiceContext;

/**
 * Produces {@link ContextualAuthenticationPolicy} instances that are satisfied iff the given {@link Authentication}
 * was created by authenticating credentials by all handlers named in
 * {@link org.jasig.cas.services.RegisteredService#getRequiredHandlers()}.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class RequiredHandlerAuthenticationPolicyFactory implements ContextualAuthenticationPolicyFactory<ServiceContext> {

    @Override
    public ContextualAuthenticationPolicy<ServiceContext> createPolicy(final ServiceContext context) {
        return new ContextualAuthenticationPolicy<ServiceContext>() {

            @Override
            public ServiceContext getContext() {
                return context;
            }

            @Override
            public boolean isSatisfiedBy(final Authentication authentication) {
                for (final String required : context.getRegisteredService().getRequiredHandlers()) {
                    if (!authentication.getSuccesses().containsKey(required)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
}
