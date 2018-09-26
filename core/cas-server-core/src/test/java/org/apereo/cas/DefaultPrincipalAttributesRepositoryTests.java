package org.apereo.cas;

import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

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

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultPrincipalAttributesRepository.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("principalFactory")
    private ObjectProvider<PrincipalFactory> principalFactory;

    @Test
    public void checkDefaultAttributes() {
        val rep = new DefaultPrincipalAttributesRepository();
        assertEquals(3, rep.getAttributes(this.principalFactory.getIfAvailable().createPrincipal("uid")).size());
    }

    @Test
    public void checkInitialAttributes() {
        val p = this.principalFactory.getIfAvailable().createPrincipal("uid", Collections.singletonMap("mail", "final@example.com"));
        val rep = new DefaultPrincipalAttributesRepository();
        assertEquals(1, rep.getAttributes(p).size());
        assertTrue(rep.getAttributes(p).containsKey("mail"));
    }

    @Test
    public void verifySerializeADefaultPrincipalAttributesRepositoryToJson() throws IOException {
        val repositoryWritten = new DefaultPrincipalAttributesRepository();
        MAPPER.writeValue(JSON_FILE, repositoryWritten);
        val repositoryRead = MAPPER.readValue(JSON_FILE, DefaultPrincipalAttributesRepository.class);
        assertEquals(repositoryWritten, repositoryRead);
    }
}
