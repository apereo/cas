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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ReturnAllAttributeReleasePolicy;
import org.jasig.cas.services.ServicesManager;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.StubPersonAttributeDao;
import org.junit.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;

/**
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class RegisteredServiceValidatorTests {

    private RegisteredServiceValidator getValidator(final boolean returnValue) {
        final Map<String, List<Object>> attrs = new HashMap<String, List<Object>>();
        final List<Object> values = new ArrayList<Object>();
        values.add("value");
        
        attrs.put("k1", values);
        attrs.put("k2", values);
        attrs.put("k3", values);
        
        final IPersonAttributeDao dao = new StubPersonAttributeDao(attrs);
       
        final RegisteredServiceValidator validator = new RegisteredServiceValidator(new TestServicesManager(returnValue), dao);
        return validator;
    }
    
    @Test
    public void testIdExists() {
        checkId(true, 1, "test");
    }

    @Test
    public void testIdDoesNotExist() {
        checkId(false, 0, "test");
    }

    @Test
    public void testIdDoesNotExist2() {
        checkId(true, 0, "test2");
    }

    @Test
    public void testIdDoesNotExist3() {
        checkId(true, 1, "test");
    }

    @Test
    public void testSupports() {
        final Validator validator = getValidator(false);
        assertTrue(validator.supports(RegisteredServiceImpl.class));
        assertFalse(validator.supports(Object.class));
    }

    @Test
    public void testMaxLength() {
        final RegisteredServiceImpl impl = new RegisteredServiceImpl();
        impl.setServiceId("test");
        impl.setDescription("fasdfdsafsafsafdsa");

        final BindException exception = new BindException(impl, "registeredService");

        final RegisteredServiceValidator validator = getValidator(false);
        validator.setMaxDescriptionLength(1);
        validator.validate(impl, exception);

        assertEquals(1, exception.getErrorCount());
    }
    
    @Test
    public void testUsernameAttributeWithAllFilteringPolicy() {
        final RegisteredServiceImpl impl = new RegisteredServiceImpl();
        impl.setServiceId("test");
        impl.setDescription("fasdfdsafsafsafdsa");
        impl.setUsernameAttribute("k3");
        impl.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        
        final BindException exception = new BindException(impl, "registeredService");

        final RegisteredServiceValidator validator = getValidator(false);
        validator.setMaxDescriptionLength(100);
        validator.validate(impl, exception);

        assertEquals(0, exception.getErrorCount());
    }
    
    protected void checkId(final boolean exists, final int expectedErrors, final String name) {
        final Validator validator = getValidator(exists);
        final RegisteredServiceImpl impl = new RegisteredServiceImpl();
        impl.setName("name");
        impl.setDescription("Test service");
        impl.setServiceId(name);

        final BindException exception = new BindException(impl, "registeredService");

        validator.validate(impl, exception);

        assertEquals(expectedErrors, exception.getErrorCount());

    }

    protected class TestServicesManager implements ServicesManager {

        private final boolean returnValue;

        protected TestServicesManager(final boolean returnValue) {
            this.returnValue = returnValue;
        }

        @Override
        public RegisteredService delete(final long id) {
            return null;
        }

        @Override
        public RegisteredService findServiceBy(final long id) {
            return null;
        }

        @Override
        public RegisteredService findServiceBy(final Service service) {
            return null;
        }

        @Override
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

        @Override
        public boolean matchesExistingService(final Service service) {
            return this.returnValue;
        }

        @Override
        public RegisteredService save(final RegisteredService registeredService) {
            return null;
        }
    }
}
