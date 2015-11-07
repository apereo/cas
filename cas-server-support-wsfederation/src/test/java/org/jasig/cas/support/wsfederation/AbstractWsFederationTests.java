package org.jasig.cas.support.wsfederation;

import org.jasig.cas.support.saml.AbstractOpenSamlTests;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Abstract class, provides resources to run wsfed tests.
 * @author Misagh Moayyed
 * @since 4.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/applicationContext.xml", "classpath*:/META-INF/spring/*.xml"})
public class AbstractWsFederationTests extends AbstractOpenSamlTests {

    @Autowired
    protected WsFederationHelper wsFederationHelper;
}

