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

import org.jasig.cas.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test cases for {@link MongoServiceRegistryDao}.
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/mongo-context.xml")
public class MongoServiceRegistryDaoTests {

    @Autowired
    private ServiceRegistryDao serviceRegistryDao;

    @Test
    public void verifySaveAndLoad() {
        final List<RegisteredService> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(buildService(i));
            this.serviceRegistryDao.save(list.get(i));
        }
        final List<RegisteredService> results = this.serviceRegistryDao.load();
        assertEquals(results.size(), list.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(list.get(i), results.get(i));
        }
        for (int i = 0; i < 5; i++) {
            this.serviceRegistryDao.delete(results.get(i));
        }
        assertTrue(this.serviceRegistryDao.load().isEmpty());
    }

    private RegisteredService buildService(final int i) {
        return TestUtils.getRegisteredService("^http://www.serviceid" + i + ".org");
    }
}
