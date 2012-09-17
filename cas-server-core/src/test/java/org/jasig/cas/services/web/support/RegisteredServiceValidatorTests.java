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
package org.jasig.cas.services.web.support;

import java.util.ArrayList;
import java.util.Collection;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.springframework.validation.BindException;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class RegisteredServiceValidatorTests extends TestCase {

    private RegisteredServiceValidator validator;
    
    protected void setUp() throws Exception {
        this.validator = new RegisteredServiceValidator();
        this.validator.setMaxDescriptionLength(1);
    }
    
    public void testIdExists() {
        checkId(true, 1, "test");
    }
    
    public void testIdDoesNotExist() {
        checkId(false, 0, "test");
    }
    
    public void testIdDoesNotExist2() {
        checkId(true, 0, "test2");
    }
    
    public void testIdDoesNotExist3() {
        checkId(true, 0, null);
    }
    
    public void testSupports() {
        assertTrue(this.validator.supports(RegisteredServiceImpl.class));
        assertFalse(this.validator.supports(Object.class));
    }

    
    public void testMaxLength() {
        this.validator.setServicesManager(new TestServicesManager(false));
        final RegisteredServiceImpl impl = new RegisteredServiceImpl();
        impl.setServiceId("test");
        impl.setDescription("fasdfdsafsafsafdsa");
        
        final BindException exception = new BindException(impl, "registeredService");
        
        this.validator.validate(impl, exception);
        
        assertEquals(1, exception.getErrorCount()); 
    }
    
    protected void checkId(final boolean exists, final int expectedErrors, final String name) {
        this.validator.setServicesManager(new TestServicesManager(exists));
        final RegisteredServiceImpl impl = new RegisteredServiceImpl();
        impl.setServiceId(name);
        
        final BindException exception = new BindException(impl, "registeredService");
        
        this.validator.validate(impl, exception);
        
        assertEquals(expectedErrors, exception.getErrorCount());
        
    }


    
    protected class TestServicesManager implements ServicesManager {
        
        private final boolean returnValue;
        
        protected TestServicesManager(final boolean returnValue) {
            this.returnValue = returnValue;
        }

        public RegisteredService delete(long id) {
            return null;
        }

        public RegisteredService findServiceBy(long id) {
            return null;
        }

        public RegisteredService findServiceBy(Service service) {
            return null;
        }

        public Collection<RegisteredService> getAllServices() {
            if (!this.returnValue) {
                return new ArrayList<RegisteredService>();
            }
            final RegisteredServiceImpl r = new RegisteredServiceImpl();
            r.setServiceId("test");
            r.setId(1000);
            
            final ArrayList<RegisteredService> list = new ArrayList<RegisteredService>();
            list.add(r);
            
            return list;
        }

        public boolean matchesExistingService(final Service service) {
            return this.returnValue;
        }

        public RegisteredService save(final RegisteredService registeredService) {
            return null;
        }
    }
}
