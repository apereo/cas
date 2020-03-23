package org.apereo.cas;

import org.apereo.cas.hibernate.CasHibernateJpaBeanFactoryTests;
import org.apereo.cas.hibernate.CasHibernatePhysicalNamingStrategyTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllHibernateTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    CasHibernatePhysicalNamingStrategyTests.class,
    CasHibernateJpaBeanFactoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllHibernateTestsSuite {
}
