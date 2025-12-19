package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.BaseGrouperConfigurationTests;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasPersonDirectoryGrouperConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Grouper")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseGrouperConfigurationTests.SharedTestConfiguration.class,
    properties = "cas.authn.attribute-repository.grouper.state=ACTIVE")
class CasPersonDirectoryGrouperConfigurationTests {
    @Autowired
    @Qualifier("grouperAttributeRepositories")
    private BeanContainer<PersonAttributeDao> grouperAttributeRepositories;

    @Test
    void verifyOperation() {
        assertNotNull(grouperAttributeRepositories);
    }

}
