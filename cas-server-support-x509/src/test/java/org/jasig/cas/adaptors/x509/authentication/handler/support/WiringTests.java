/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



/**
 * Verifies Spring IOC wiring for X.509 beans.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.4.6
 *
 */
public class WiringTests {
    @Test
    public void testWiring() {
        final ApplicationContext context = new ClassPathXmlApplicationContext("deployerConfigContext.xml");
        Assert.assertTrue(context.getBeanDefinitionCount() > 0);
    }
}
