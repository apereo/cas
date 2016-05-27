package org.apereo.cas.persondir.support

import net.shibboleth.idp.attribute.resolver.AttributeDefinition
import net.shibboleth.idp.attribute.resolver.AttributeResolver
import net.shibboleth.idp.attribute.resolver.DataConnector
import net.shibboleth.idp.attribute.resolver.ResolutionException
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext
import org.apereo.cas.config.ShibbolethAttributeResolverConfiguration
import org.apereo.cas.config.TestShibbolethAttributeResolverConfiguration
import org.apereo.services.persondir.IPersonAttributeDao
import org.apereo.services.persondir.support.NamedPersonImpl
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import javax.annotation.Nonnull
import javax.annotation.Resource

@ContextConfiguration(classes = [TestShibbolethAttributeResolverConfiguration, ShibbolethAttributeResolverConfiguration])
class ShibbolethPersonAttributeDaoSpec extends Specification {
    @Resource(name = 'shibbolethPersonAttributeDao')
    IPersonAttributeDao iPersonAttributeDao

    def setupSpec() {}

    @Unroll
    def "static attributes from custom spring configuration"() {
        expect:
        iPersonAttributeDao.getPerson(a) == b && iPersonAttributeDao.getPerson(a).attributes == b.attributes
        where:
        a       | b
        'test1' | new NamedPersonImpl('test1', ['eduPersonScopedAffiliation': ['member']])
        'test2' | new NamedPersonImpl('test2', ['eduPersonScopedAffiliation': ['member']])
    }

    def "induce a resolution exception"() {
        when:
        def dao = new ShibbolethPersonAttributeDao(attributeResolver: new AttributeResolver() {
            @Override
            Map<String, AttributeDefinition> getAttributeDefinitions() {
                return null
            }

            @Override
            Map<String, DataConnector> getDataConnectors() {
                return null
            }

            @Override
            void resolveAttributes(@Nonnull AttributeResolutionContext resolutionContext) throws ResolutionException {
                throw new ResolutionException()
            }

            @Override
            String getId() {
                return null
            }
        })
        dao.getPerson("test")
        then:
        thrown(RuntimeException)
    }

    @Unroll
    def "check to make sure that all the bad methods just throw exceptions"() {
        when:
        b ? iPersonAttributeDao."${a}"(b) : iPersonAttributeDao."${a}"()
        then:
        thrown(c)
        where:
        a                                    | b            | c
        'getPeople'                          | [:]          | UnsupportedOperationException
        'getPeopleWithMultivaluedAttributes' | [:]          | UnsupportedOperationException
        'getPossibleUserAttributeNames'      | null         | UnsupportedOperationException
        'getAvailableQueryAttributes'        | null         | UnsupportedOperationException
        'getMultivaluedUserAttributes'       | ['here': []] | UnsupportedOperationException
        'getMultivaluedUserAttributes'       | 'hahah!'     | UnsupportedOperationException
        'getUserAttributes'                  | ['here': []] | UnsupportedOperationException
        'getUserAttributes'                  | 'hahah!'     | UnsupportedOperationException
    }
}
