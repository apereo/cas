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

package org.jasig.cas.web.support;


import org.apache.commons.lang3.NotImplementedException;
import org.jasig.cas.authentication.principal.WebApplicationService;

import javax.servlet.http.HttpServletRequest;

/**
 * @deprecated As of 4.2, use {@link DefaultArgumentExtractor}.
 * Implements the traditional CAS2 protocol.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Deprecated
public final class CasArgumentExtractor extends AbstractArgumentExtractor {
    @Override
    public WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        throw new NotImplementedException("This operation is not supported. "
                + "The class is deprecated and will be removed in future versions");
    }
}


