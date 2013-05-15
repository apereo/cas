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
package org.jasig.cas.adaptors.ldap.lppe;

import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of the {@link LdapDateConverter} which defines common
 * settings to all converters, namely the specification and configuration of the
 * {@link #setTimeZone(DateTimeZone)}.
 * 
 * @author Misagh Moayyed
 * @version 4.0.0
 */
public abstract class AbstractLdapDateConverter implements LdapDateConverter {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private DateTimeZone timeZone = DateTimeZone.UTC;
    
    public void setTimeZone(final DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public DateTimeZone getTimeZone() {
       return this.timeZone;
    }
}

