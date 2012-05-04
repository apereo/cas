/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 * Unit test to verify Spring context wiring.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
    "file:src/main/webapp/WEB-INF/cas-servlet.xml",
    "file:src/main/webapp/WEB-INF/deployerConfigContext.xml",
    "file:src/main/webapp/WEB-INF/spring-configuration/*.xml"
})
public class WiringTests {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testWiring() throws Exception {
        assertTrue(applicationContext.getBeanDefinitionCount() > 0);
    }
}
