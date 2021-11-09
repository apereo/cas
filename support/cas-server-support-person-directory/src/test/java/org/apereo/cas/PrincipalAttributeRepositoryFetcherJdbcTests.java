package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalAttributeRepositoryFetcherCascadeTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.attribute-repository.jdbc[0].attributes.name=PersonName",

    "cas.authn.attribute-repository.jdbc[0].query-attributes.credentialClass=ctype",
    "cas.authn.attribute-repository.jdbc[0].single-row=false",
    "cas.authn.attribute-repository.jdbc[0].column-mappings.attr_name=attr_value",
    "cas.authn.attribute-repository.jdbc[0].sql=SELECT * FROM table_users WHERE {0}",
    "cas.authn.attribute-repository.jdbc[0].username=uid"
})
@Tag("JDBC")
public class PrincipalAttributeRepositoryFetcherJdbcTests extends BaseJdbcAttributeRepositoryTests {

    @Test
    public void verifyOperationWithUsernamePasswordCredentialType() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED))
            .attributeRepository(attributeRepository)
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .resolveAttributes(true)
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context);
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        val p = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertTrue(p.getAttributes().containsKey("PersonName"));
    }

    @Test
    public void verifyOperationWithoutUsernamePasswordCredentialType() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED))
            .attributeRepository(attributeRepository)
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .resolveAttributes(true)
            .returnNullIfNoAttributes(true)
            .build();
        val resolver = new PersonDirectoryPrincipalResolver(context);
        val credential = CoreAuthenticationTestUtils.getHttpBasedServiceCredentials();
        val p = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNull(p);
    }

    @Override
    public void prepareDatabaseTable(final Statement statement) throws Exception {
        statement.execute("create table table_users (uid VARCHAR(255), attr_name VARCHAR(255), attr_value VARCHAR(255), ctype VARCHAR(255));");
        statement.execute("insert into table_users (uid, attr_name, attr_value, ctype) values('casuser', 'name', 'ApereoCAS', 'UsernamePasswordCredential');");
    }
}
