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

package org.jasig.cas.adaptors.x509.authentication.handler.support;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.cert.X509CRL;
import java.util.Set;

/**
 * Defines operations needed to a fetch a CRL.
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface CRLFetcher {
    /**
     * Fetches a collection of crls from the specified resources
     * and returns a map of CRLs each tracked by its url.
     * @param crls resources to retrieve
     * @return map of crl entries and their urls
     * @throws Exception the exception thrown if resources cant be fetched
     */
    Set<X509CRL> fetch(@NotNull @Size(min=1)  Set<? extends Object> crls) throws Exception;

    /**
     * Fetches a single of crl from the specified resource
     * and returns it.
     * @param crl resources to retrieve
     * @return the CRL entry
     * @throws Exception the exception thrown if resources cant be fetched
     */
    X509CRL fetch(@NotNull Object crl) throws Exception;
}
