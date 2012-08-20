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
package org.jasig.cas.services;

import java.util.Comparator;

/**
 * Comparator class that compares two registered services based on their evaluation order and name.
 * The name comparison is case insensitive and abides by the rules specified for the {@link String#compareToIgnoreCase(String)} method.
 * 
 * @author Misagh Moayyed
 * @see Comparator
 * @see ServicesManager
 * @see RegisteredService
 */
public class RegisteredServiceComparator implements Comparator<RegisteredService> {
    private static final Comparator<RegisteredService> INSTANCE = new RegisteredServiceComparator();
    
    private RegisteredServiceComparator() {}
    
    public static Comparator<RegisteredService> getInstance() {
        return INSTANCE;
    }
    
    @Override
    public int compare(RegisteredService o1, RegisteredService o2) {
        int result = Integer.valueOf(o1.getEvaluationOrder()).compareTo(Integer.valueOf(o2.getEvaluationOrder()));    
        if (result == 0) {
            result = o1.getName().compareToIgnoreCase(o2.getName());
        }
        return result;
    }
}
