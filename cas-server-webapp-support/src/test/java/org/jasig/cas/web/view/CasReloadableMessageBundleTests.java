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
package org.jasig.cas.web.view;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RunWith(JUnit4.class)
public class CasReloadableMessageBundleTests {
    private CasReloadableMessageBundle loader;
    
    @Before
    public void setup() {
        this.loader  = new CasReloadableMessageBundle();
        this.loader.setFallbackToSystemLocale(false);
        this.loader.setDefaultEncoding("UTF-8");
        this.loader.setBasenames("classpath:messages", "classpath:custom_messages");
    }
    
    @Test
    public void verifyGetMessageFromRequestedBundle() {
        final String msg = this.loader.getMessage("screen.blocked.header", null, Locale.FRENCH);
        assertNotNull(msg);
    }

    @Test
    public void verifyGetMessageFromDefaultBundle() {
        final String msg = this.loader.getMessage("screen.other.message", null, Locale.FRENCH);
        assertEquals(msg, "another");
    }
    
    @Test
    public void verifyUseCodeAsMessageItself() {
        this.loader.setUseCodeAsDefaultMessage(true);
        final String msg = this.loader.getMessage("does.not.exist", null, Locale.FRENCH);
        assertEquals(msg, "does.not.exist");
    }
    
    @Test(expected=NoSuchMessageException.class)
    public void verifyExpectExceptionWhenCodeNotFound() {
        this.loader.setUseCodeAsDefaultMessage(false);
        this.loader.getMessage("does.not.exist", null, Locale.FRENCH);
    }
}
