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
package org.jasig.cas.audit.spi;

import com.github.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.util.AopUtils;

/**
 * Converts the Credential object into a String resource identifier.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public final class CredentialsAsFirstParameterResourceResolver implements AuditResourceResolver {

    private static final String PREFIX = "supplied credentials: [";

    public String[] resolveFrom(final JoinPoint joinPoint, final Object retval) {
        return new String[] { resolveFromInternal(joinPoint) };
    }

    public String[] resolveFrom(final JoinPoint joinPoint, final Exception exception) {
        return new String[] { resolveFromInternal(joinPoint) };
    }

    protected String resolveFromInternal(final JoinPoint joinPoint) {
        final Credential[] credentials = (Credential [])AopUtils.unWrapJoinPoint(joinPoint).getArgs()[0];
        final StringBuilder sb = new StringBuilder(PREFIX);
        int i = 0;
        for (final Credential c : credentials) {
            sb.append(c);
            if (i++ > 0) {
                sb.append(", ");
            }
        }
        return sb.append(']').toString();
    }
}
