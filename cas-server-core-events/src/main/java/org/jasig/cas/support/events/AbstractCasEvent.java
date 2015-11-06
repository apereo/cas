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
package org.jasig.cas.support.events;

import org.springframework.context.ApplicationEvent;

import java.io.Serializable;

/**
 * Base Spring {@code ApplicationEvent} representing a abstract single sign on action executed within running CAS server.
 * This event encapsulates {@link org.jasig.cas.authentication.Authentication} that is associated with an SSO action
 * executed in a CAS server and an SSO session
 * token in the form of ticket granting ticket id.
 * More concrete events are expected to subclass this abstract type.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public abstract class AbstractCasEvent extends ApplicationEvent implements Serializable {

    private static final long serialVersionUID = 8059647975948452375L;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source                 the source
     */
    public AbstractCasEvent(final Object source) {
        super(source);
    }

}
