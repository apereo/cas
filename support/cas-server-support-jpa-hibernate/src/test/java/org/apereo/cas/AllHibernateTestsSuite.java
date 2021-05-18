package org.apereo.cas;

import org.apereo.cas.hibernate.CasHibernateJpaBeanFactoryTests;
import org.apereo.cas.hibernate.CasHibernatePhysicalNamingStrategyTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
@Suite
public class AllHibernateTestsSuite {
}
