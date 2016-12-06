package org.apereo.cas.persondir.support

import org.junit.Ignore
import org.springframework.test.context.TestPropertySource

/**
 * This test class is presently disabled because the ldap data connector is not quite compatible with CAS
 * due to different versions of the ldaptive library. The functionality needs to be verified via
 * the IdP dependencies are upgraded to support the same ldaptive version as CAS.
 */
@TestPropertySource(properties = "cas.shibAttributeResolver.resources=classpath:/attribute-resolver-ldap.xml")
@Ignore
class ShibbolethPersonAttributeDaoLdapResolverTests extends BaseShibbolethPersonAttributeDao {
}
