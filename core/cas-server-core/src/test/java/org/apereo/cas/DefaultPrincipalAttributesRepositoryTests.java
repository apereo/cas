package org.apereo.cas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link DefaultPrincipalAttributesRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreWebConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasAuthenticationEventExecutionPlanTestConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class
})
public class DefaultPrincipalAttributesRepositoryTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultPrincipalAttributesRepository.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    @Qualifier("principalFactory")
    private PrincipalFactory principalFactory;

    @Test
    public void checkDefaultAttributes() {
        final PrincipalAttributesRepository rep = new DefaultPrincipalAttributesRepository();
        assertEquals(rep.getAttributes(this.principalFactory.createPrincipal("uid")).size(), 3);
    }

    @Test
    public void checkInitialAttributes() {
        final Principal p = this.principalFactory.createPrincipal("uid", Collections.singletonMap("mail", "final@example.com"));
        final PrincipalAttributesRepository rep = new DefaultPrincipalAttributesRepository();
        assertEquals(rep.getAttributes(p).size(), 1);
        assertTrue(rep.getAttributes(p).containsKey("mail"));
    }

    @Test
    public void verifySerializeADefaultPrincipalAttributesRepositoryToJson() throws IOException {
        final PrincipalAttributesRepository repositoryWritten = new DefaultPrincipalAttributesRepository();
        MAPPER.writeValue(JSON_FILE, repositoryWritten);
        final PrincipalAttributesRepository repositoryRead = MAPPER.readValue(JSON_FILE, DefaultPrincipalAttributesRepository.class);
        assertEquals(repositoryWritten, repositoryRead);
    }
}
