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

package org.jasig.cas.services;

import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * This is test cases for
 * {@link DefaultRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
public class DefaultRegisteredServiceAccessStrategyTests {
    @Test
     public void checkDefaultAuthzStrategyConfig() {
        final RegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        assertTrue(authz.isServiceAccessAllowed());
        assertTrue(authz.isServiceAccessAllowedForSso());
    }

    @Test
    public void checkDisabledAuthzStrategyConfig() {
        final RegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy(false, true);
        assertFalse(authz.isServiceAccessAllowed());
        assertTrue(authz.isServiceAccessAllowedForSso());
    }

    @Test
    public void checkDisabledSsoAuthzStrategyConfig() {
        final RegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy(true, false);
        assertTrue(authz.isServiceAccessAllowed());
        assertFalse(authz.isServiceAccessAllowedForSso());
    }

    @Test
    public void setAuthzStrategyConfig() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy(false, false);
        authz.setEnabled(true);
        authz.setSsoEnabled(true);
        assertTrue(authz.isServiceAccessAllowed());
        assertTrue(authz.isServiceAccessAllowedForSso());
        assertTrue(authz.isRequireAllAttributes());
    }

    @Test
    public void checkAuthzPrincipalNoAttrRequirements() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(new HashMap<String, Object>()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsEmptyPrincipal() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(this.getRequiredAttributes());
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(new HashMap<String, Object>()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsAll() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(this.getRequiredAttributes());
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(
                this.getPrincipalAttributes()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsMissingOne() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(this.getRequiredAttributes());

        final Map<String, Object> pAttrs = this.getPrincipalAttributes();
        pAttrs.remove("cn");

        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsMissingOneButNotAllNeeded() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(this.getRequiredAttributes());
        authz.setRequireAllAttributes(false);
        final Map<String, Object> pAttrs = this.getPrincipalAttributes();
        pAttrs.remove("cn");

        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsNoValueMatch() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        final Map<String, Set<String>>  reqs = this.getRequiredAttributes();
        reqs.remove("phone");
        authz.setRequiredAttributes(reqs);
        authz.setRequireAllAttributes(false);
        final Map<String, Object> pAttrs = this.getPrincipalAttributes();
        pAttrs.remove("cn");
        pAttrs.put("givenName", "theName");
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrValueCaseSensitiveComparison() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();

        final Map<String, Set<String>>  reqs = this.getRequiredAttributes();
        reqs.remove("phone");
        authz.setRequiredAttributes(reqs);

        final Map<String, Object> pAttrs = this.getPrincipalAttributes();
        pAttrs.put("cn", "CAS");
        pAttrs.put("givenName", "kaz");
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(pAttrs));
    }

    @Test
    public void checkAuthorizationByRangePass() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy(true, true);
        authz.setStartingDateTime(DateTime.now().toString());
        authz.setEndingDateTime(DateTime.now().plusMinutes(10).toString());
        assertTrue(authz.isServiceAccessAllowed());

    }

    @Test
    public void checkAuthorizationByRangeFailStartTime() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy(true, true);
        authz.setStartingDateTime(DateTime.now().plusDays(1).toString());
        authz.setEndingDateTime(DateTime.now().plusMinutes(10).toString());
        assertFalse(authz.isServiceAccessAllowed());

    }

    @Test
    public void checkAuthorizationByRangePassEndTime() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy(true, true);
        authz.setStartingDateTime(DateTime.now().toString());
        authz.setEndingDateTime(DateTime.now().plusSeconds(30).toString());
        assertTrue(authz.isServiceAccessAllowed());
    }

    private Map<String, Set<String>> getRequiredAttributes() {


        final Map<String, Set<String>> map = new HashMap<>();
        map.put("cn", Sets.newHashSet("cas", "SSO"));
        map.put("givenName", Sets.newHashSet("CAS", "KAZ"));
        map.put("phone", Sets.newHashSet("\\d\\d\\d-\\d\\d\\d-\\d\\d\\d"));
        return map;
    }

    private Map<String, Object> getPrincipalAttributes() {
        final Map<String, Object> map = new HashMap<>();
        map.put("cn", "cas");
        map.put("givenName", Arrays.asList("cas", "KAZ"));
        map.put("sn", "surname");
        map.put("phone", "123-456-7890");

        return map;
    }

}
