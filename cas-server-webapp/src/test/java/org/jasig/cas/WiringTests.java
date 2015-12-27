package org.jasig.cas;

import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import static org.junit.Assert.*;

/**
 * Unit test to verify Spring context wiring.
 *
 * @author Middleware Services
 * @since 3.0.0
 */
public class WiringTests {
    private XmlWebApplicationContext applicationContext;

    @Before
    public void setUp() {
        applicationContext = new XmlWebApplicationContext();
        applicationContext.setConfigLocations(
                "classpath:/webappContext.xml",
                "file:src/main/webapp/WEB-INF/cas-servlet.xml",
                "file:src/main/webapp/WEB-INF/deployerConfigContext.xml",
        "file:src/main/webapp/WEB-INF/spring-configuration/*.xml");
        applicationContext.setServletContext(new MockServletContext(new ResourceLoader() {
            @Override
            public Resource getResource(final String location) {
                return new FileSystemResource("src/main/webapp" + location);
            }

            @Override
            public ClassLoader getClassLoader() {
                return getClassLoader();
            }
        }));
        applicationContext.refresh();
    }

    @Test
    public void verifyWiring() throws Exception {
        assertTrue(applicationContext.getBeanDefinitionCount() > 0);
    }

    @Test
    public void checkPrincipalFactory() throws Exception {
        final PrincipalFactory factory1 =
                applicationContext.getBean("principalFactory", PrincipalFactory.class);
        final PrincipalFactory factory2 =
                applicationContext.getBean("principalFactory", PrincipalFactory.class);
        assertEquals("principal factories should be equal instances", factory1, factory2);
    }
}
