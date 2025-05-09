package org.apereo.cas.heimdall;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.resource.policy.JdbcAuthorizationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcAuthorizationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@EnabledIfListeningOnPort(port = 3306)
@Tag("Authorization")
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(classes = BaseHeimdallTests.SharedTestConfiguration.class, properties = {
    "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/heimdalloidc.jwks",
    "cas.heimdall.json.location=classpath:/policies"
})
class JdbcAuthorizationPolicyTests {
    @Autowired
    @Qualifier(AuthorizableResourceRepository.BEAN_NAME)
    private AuthorizableResourceRepository authorizableResourceRepository;

    @Test
    void verifyOperation() throws Throwable {
        val strategy = new JdbcAuthorizationPolicy();
        strategy.setUrl("jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE");
        strategy.setUsername("root");
        strategy.setPassword("password");
        strategy.setQuery("SELECT authorized FROM policies WHERE uri = :uri AND method = :method AND namespace = :namespace");

        val jdbcTemplate = strategy.buildJdbcTemplate();
        jdbcTemplate.getJdbcOperations().execute("DROP TABLE IF EXISTS policies");
        jdbcTemplate.getJdbcOperations().execute("""
            CREATE TABLE policies (
                id INT AUTO_INCREMENT PRIMARY KEY,
                uri VARCHAR(255)   NOT NULL,
                method VARCHAR(10) NOT NULL,
                namespace VARCHAR(100) NOT NULL,
                authorized BOOLEAN NOT NULL,
                UNIQUE KEY uk_policy (uri, method, namespace)
            );
            """);

        val insertSql = """
            INSERT INTO policies (uri, method, namespace, authorized)
                VALUES (:uri, :method, :namespace, :authorized)
            """;
        val dataSet = List.of(
            new MapSqlParameterSource()
                .addValue("uri", "/api/claims")
                .addValue("method", "PUT")
                .addValue("namespace", "API_CLAIMS")
                .addValue("authorized", true)
        );
        dataSet.forEach(params -> jdbcTemplate.update(insertSql, params));

        val authzRequest = AuthorizationRequest.builder()
            .uri("/api/claims")
            .method("PUT")
            .namespace("API_CLAIMS")
            .build()
            .withPrincipal(RegisteredServiceTestUtils.getPrincipal());

        val resource = authorizableResourceRepository.find(authzRequest).orElseThrow();
        assertTrue(strategy.evaluate(resource, authzRequest).authorized());
    }
}
