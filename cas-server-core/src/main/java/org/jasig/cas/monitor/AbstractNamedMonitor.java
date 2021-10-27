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
package org.jasig.cas.monitor;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

/**
 * Base class for all monitors that support configurable naming.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public abstract class AbstractNamedMonitor<S extends Status> implements Monitor<S> {
    /** Monitor name. */
    protected String name;


    /**
     * @return Monitor name.
     */
    public String getName() {
        return StringUtils.defaultIfEmpty(this.name, getClass().getSimpleName());
    }

    /**
     * @param n Monitor name.
     */
    public void setName(final String n) {
        Assert.hasText(n, "Monitor name cannot be null or empty.");
        this.name = n;
    }
}
