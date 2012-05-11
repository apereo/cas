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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract class providing common functionality for checking the account's
 * expiration time.
 *
 * @author Eric Pierce
 * @author Jan Van der Velpen
 * @version 1.1 3/30/2009 11:47:37
 *
 */
public abstract class AbstractPasswordPolicyEnforcer implements PasswordPolicyEnforcer, InitializingBean {
    protected Logger logger = LoggerFactory.getLogger(getClass());

}
