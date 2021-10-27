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
package org.jasig.cas.authentication.principal;

import java.io.Serializable;

/**
 * Deprecated credential marker interface provides transition path from CAS 3.x to 4.x authentication components.
 *
 * @author Marvin S. Addison
 * @deprecated This component has been renamed to {@link org.jasig.cas.authentication.Credential} in CAS 4.0.
 * @since 4.0.0
 */
@Deprecated
public interface Credentials extends Serializable {}
