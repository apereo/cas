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
package org.jasig.cas.web.flow;

import org.springframework.webflow.execution.Event;

/**
 * Maps exceptions that arise from default {@link org.jasig.cas.authentication.AuthenticationManager} components onto
 * Webflow events.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class AuthenticationErrorStateResolver
        extends AbstractAuthenticationErrorResolver<Event> implements ErrorStateResolver {

    private static final String DEFAULT_STATE_ID = "error";

    private Object source;

    /**
     * Sets the source used for creating events.
     *
     * @param source Event source.
     */
    public void setSource(final Object source) {
        this.source = source;
    }

    @Override
    protected Event getDefault() {
        return new Event(this.source, DEFAULT_STATE_ID);
    }

    @Override
    protected Event convertErrorCode(final String code) {
        return new Event(this.source, code);
    }
}
