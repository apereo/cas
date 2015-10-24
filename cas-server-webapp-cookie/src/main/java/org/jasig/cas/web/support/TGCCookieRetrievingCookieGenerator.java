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

package org.jasig.cas.web.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Generates the tgc cookie.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("ticketGrantingTicketCookieGenerator")
public class TGCCookieRetrievingCookieGenerator extends CookieRetrievingCookieGenerator {

    /**
     * Instantiates a new TGC cookie retrieving cookie generator.
     *
     * @param casCookieValueManager the cas cookie value manager
     */
    @Autowired
    public TGCCookieRetrievingCookieGenerator(@Qualifier("defaultCookieValueManager")
        final CookieValueManager casCookieValueManager) {
        super(casCookieValueManager);
    }

    @Override
    @Autowired
    public void setCookieName(@Value("${tgc.name:TGC}")
                                  final String cookieName) {
        super.setCookieName(cookieName);
    }

    @Override
    @Autowired
    public void setCookiePath(@Value("${tgc.path:/cas}")
                                  final String cookiePath) {
        super.setCookiePath(cookiePath);
    }

    @Override
    @Autowired
    public void setCookieMaxAge(@Value("${tgc.maxAge:-1}")
                                    final Integer cookieMaxAge) {
        super.setCookieMaxAge(cookieMaxAge);
    }

    @Override
    @Autowired
    public void setCookieSecure(@Value("${tgc.secure:true}")
                                    final boolean cookieSecure) {
        super.setCookieSecure(cookieSecure);
    }


}
