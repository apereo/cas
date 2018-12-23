package org.apereo.cas.config;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.StubPersonAttributeDao;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

/**
 * This is {@link CasPersonDirectoryTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestConfiguration("casPersonDirectoryTestConfiguration")
public class CasPersonDirectoryTestConfiguration {
    @Bean
    public List<IPersonAttributeDao> attributeRepositories() {
        return CollectionUtils.wrap(attributeRepository());
    }

    @ConditionalOnMissingBean(name = "attributeRepository")
    @Bean
    public IPersonAttributeDao attributeRepository() {
        val attrs = CollectionUtils.wrap("uid", CollectionUtils.wrap("uid"),
                "eduPersonAffiliation", CollectionUtils.wrap("developer"),
                "groupMembership", CollectionUtils.wrap("adopters"));
        return new StubPersonAttributeDao((Map) attrs);
    }
}
