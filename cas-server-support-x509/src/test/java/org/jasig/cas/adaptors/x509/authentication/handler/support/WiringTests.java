package org.jasig.cas.adaptors.x509.authentication.handler.support;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Verifies Spring IOC wiring for X.509 beans.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public class WiringTests {
    @Test
    public void verifyWiring() {
        try (final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("deployerConfigContext.xml")) {
            Assert.assertTrue(context.getBeanDefinitionCount() > 0);
        }
    }
}
