package org.apereo.cas.persondir.support

import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = "cas.shibAttributeResolver.resources=classpath:/attribute-resolver-static.xml")
class ShibbolethPersonAttributeDaoStaticResolverTests extends BaseShibbolethPersonAttributeDao {
}
