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
package org.jasig.cas.web.support;

import javax.servlet.http.HttpServletRequest;

/**
 * Throttles access attempts for failed logins by IP Address.  This stores the attempts in memory.  This is not good for a
 * clustered environment unless the intended behavior is that this blocking is per-machine.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3.5
 */
public final class InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter {

    @Override
    protected String constructKey(final HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
