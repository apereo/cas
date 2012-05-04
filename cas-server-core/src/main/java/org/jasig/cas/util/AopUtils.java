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
package org.jasig.cas.util;

import org.aspectj.lang.JoinPoint;


/**
 * Utility class to assist with AOP operations.
 *
 * @author Marvin S. Addison
 * @version $Revision$ $Date$
 * @since 3.4
 *
 */
public final class AopUtils {

    /**
     * Unwraps a join point that may be nested due to layered proxies.
     *
     * @param point Join point to unwrap.
     * @return Innermost join point; if not nested, simply returns the argument.
     */
    public static JoinPoint unWrapJoinPoint(final JoinPoint point) {
        JoinPoint naked = point;
        while (naked.getArgs().length > 0 && naked.getArgs()[0] instanceof JoinPoint) {
            naked = (JoinPoint) naked.getArgs()[0];
        }
        return naked;
    }
}
