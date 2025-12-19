package org.apereo.cas.redis;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRedisAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisPersonAttributeDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Redis")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 6379)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasRedisAuthenticationAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class
},
    properties = {
        "cas.authn.attribute-repository.redis[0].host=localhost",
        "cas.authn.attribute-repository.redis[0].port=6379"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
class RedisPersonAttributeDaoTests {
    private static final String USER_ID = UUID.randomUUID().toString();

    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    private PersonAttributeDao attributeRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeEach
    void initialize() throws Exception {
        val redis = casProperties.getAuthn().getAttributeRepository().getRedis().getFirst();
        val conn = RedisObjectFactory.newRedisConnectionFactory(redis, true, CasSSLContext.disabled());
        val template = RedisObjectFactory.newRedisTemplate(conn);
        template.initialize();
        val attr = new HashMap<String, List<Object>>();
        attr.put("name", CollectionUtils.wrapList("John", "Jon"));
        attr.put("age", CollectionUtils.wrapList("42"));
        template.opsForHash().putAll(USER_ID, attr);
    }

    @Test
    void verifyAttributes() {
        val person = attributeRepository.getPerson(USER_ID);
        assertNotNull(person);
        val attributes = person.getAttributes();
        assertEquals(USER_ID, person.getName());
        assertTrue(attributes.containsKey("name"));
        assertTrue(attributes.containsKey("age"));
    }
}
