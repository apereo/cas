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
package org.jasig.cas;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.util.Assert;

/**
 * Implements the Spring {@link org.springframework.context.MessageSourceResolvable} interface to adapt
 * a CAS {@link Message} onto the corresponding component in the Spring framework.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class SpringMessageAdapter implements MessageSourceResolvable {

    private final Message message;

    public SpringMessageAdapter(final Message message) {
        Assert.notNull(message, "Message cannot be null.");
        this.message = message;
    }

    @Override
    public String[] getCodes() {
        return new String[] { this.message.getCode() };
    }

    @Override
    public Object[] getArguments() {
        return this.message.getParams();
    }

    @Override
    public String getDefaultMessage() {
        return this.message.getDefaultMessage();
    }
}
