/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apereo.cas.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultRegisteredServiceAccessStrategyTests}.
 * 
 * @author Martin Böhmer
 * @since 5.3.8
 */
public class DefaultRegisteredServiceAccessStrategyTests {
    
    @Test
    public void verifyRequiredAttributes() {
        final Map<String, Set<String>> requiredAttributes = new LinkedHashMap();
        requiredAttributes.put("active", new HashSet(Arrays.asList("true")));
        requiredAttributes.put("services", new HashSet(Arrays.asList("service3")));
        final DefaultRegisteredServiceAccessStrategy strategy = new DefaultRegisteredServiceAccessStrategy(requiredAttributes);
        strategy.setRequireAllAttributes(true);
        
        final Map<String, Object> principalAttributes = new LinkedHashMap();
        principalAttributes.put("active", "true");
        principalAttributes.put("services", "service1;service2");
        principalAttributes.put("username", "casuser");
        principalAttributes.put("email", "casuser@example.org");
        
        final boolean allowServiceAccess = strategy.doPrincipalAttributesAllowServiceAccess("casuser", principalAttributes);
        
        assertFalse(allowServiceAccess);
    }
    
}
