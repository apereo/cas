package org.apereo.cas.config;

import org.apereo.cas.BaseGrouperConfigurationTests;

import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasPersonDirectoryGrouperConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("CasConfiguration")
@SpringBootTest(classes = BaseGrouperConfigurationTests.SharedTestConfiguration.class,
    properties = "cas.authn.attribute-repository.grouper.enabled=true")
public class CasPersonDirectoryGrouperConfigurationTests {
    @Autowired
    @Qualifier("grouperAttributeRepositories")
    private List<IPersonAttributeDao> grouperAttributeRepositories;

    @Test
    public void verifyOperation() {
        assertFalse(grouperAttributeRepositories.isEmpty());
    }

}
