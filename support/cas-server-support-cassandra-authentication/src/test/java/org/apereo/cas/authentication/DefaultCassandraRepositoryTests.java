package org.apereo.cas.authentication;

import org.apereo.cas.category.CassandraCategory;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CassandraAuthenticationConfiguration;
import org.apereo.cas.config.CassandraCoreConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultCassandraRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class,
    CassandraCoreConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CassandraAuthenticationConfiguration.class
})
@EnableConfigurationProperties
@TestPropertySource(locations = {"classpath:/cassandra-authn.properties"})
@Category(CassandraCategory.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class DefaultCassandraRepositoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("cassandraAuthenticationHandler")
    private AuthenticationHandler cassandraAuthenticationHandler;

    @Test
    public void verifyUserNotFound() throws Exception {
        final var c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("baduser", "Mellon");
        thrown.expect(AccountNotFoundException.class);
        cassandraAuthenticationHandler.authenticate(c);
    }

    @Test
    public void verifyUserBadPassword() throws Exception {
        final var c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "bad");
        thrown.expect(FailedLoginException.class);
        cassandraAuthenticationHandler.authenticate(c);
    }

    @Test
    public void verifyUser() throws Exception {
        final var c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        final var result = cassandraAuthenticationHandler.authenticate(c);
        assertNotNull(result);
        assertEquals("casuser", result.getPrincipal().getId());
    }
}
