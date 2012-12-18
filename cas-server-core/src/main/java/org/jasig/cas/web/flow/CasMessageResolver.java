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
package org.jasig.cas.web.flow;

import java.util.Locale;

import org.jasig.cas.SpringMessageAdapter;
import org.springframework.binding.message.Message;
import org.springframework.binding.message.MessageResolver;
import org.springframework.binding.message.Severity;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;

/**
 * Resolves a message suitable for human display from an abstract CAS message descriptor.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class CasMessageResolver implements MessageResolver {

    private static final Severity DEFAULT_SEVERITY = Severity.INFO;

    private final MessageSourceResolvable message;

    private final Severity severity;

    private final Object source;


    public CasMessageResolver(final org.jasig.cas.Message message) {
        this(message, null, DEFAULT_SEVERITY);
    }

    public CasMessageResolver(final org.jasig.cas.Message message, final Object source) {
        this(message, source, DEFAULT_SEVERITY);
    }


    public CasMessageResolver(final org.jasig.cas.Message message, final Severity severity) {
        this(message, null, severity);
    }

    public CasMessageResolver(final org.jasig.cas.Message message, final Object source, final Severity severity) {
        this.message = new SpringMessageAdapter(message);
        this.source = source;
        this.severity = severity;
    }

    @Override
    public Message resolveMessage(final MessageSource source, final Locale locale) {
        return new Message(this.source, source.getMessage(this.message, locale), this.severity);
    }

    public static CasMessageResolver info(final org.jasig.cas.Message message) {
        return new CasMessageResolver(message, Severity.INFO);
    }

    public static CasMessageResolver warn(final org.jasig.cas.Message message) {
        return new CasMessageResolver(message, Severity.WARNING);
    }

    public static CasMessageResolver error(final org.jasig.cas.Message message) {
        return new CasMessageResolver(message, Severity.ERROR);
    }
}
