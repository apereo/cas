package org.apereo.cas.redis;

import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.RedisAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisPersonAttributeDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Redis")
@EnabledIfPortOpen(port = 6379)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    RedisAuthenticationConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class
},
    properties = {
        "cas.authn.attributeRepository.redis[0].host=localhost",
        "cas.authn.attributeRepository.redis[0].port=6379"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisPersonAttributeDaoTests {
    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeEach
    public void initialize() {
        val redis = casProperties.getAuthn().getAttributeRepository().getRedis().get(0);
        val conn = RedisObjectFactory.newRedisConnectionFactory(redis, true);
        val template = RedisObjectFactory.newRedisTemplate(conn);
        template.afterPropertiesSet();
        val attr = new HashMap<>();
        attr.put("name", CollectionUtils.wrapList("John", "Jon"));
        attr.put("age", CollectionUtils.wrapList("42"));
        template.opsForHash().putAll("casuserredis", attr);
    }

    @Test
    public void verifyAttributes() {
        val person = attributeRepository.getPerson("casuserredis", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);
        val attributes = person.getAttributes();
        assertEquals("casuserredis", person.getName());
        assertTrue(attributes.containsKey("name"));
        assertTrue(attributes.containsKey("age"));
    }
}
