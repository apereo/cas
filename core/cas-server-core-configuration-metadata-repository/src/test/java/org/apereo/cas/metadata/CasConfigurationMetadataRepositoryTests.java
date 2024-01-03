package org.apereo.cas.metadata;

import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.gauth.GoogleAuthenticatorMultifactorProperties;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationMetadataRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("CasConfiguration")
class CasConfigurationMetadataRepositoryTests {

    @Test
    void verifyOperation() throws Throwable {
        val repository = new CasConfigurationMetadataRepository();
        var properties = repository.getPropertiesWithType(LdapAuthenticationProperties.class);
        assertTrue(properties.isEmpty());
        properties = repository.getPropertiesWithType(GoogleAuthenticatorMultifactorProperties.class);
        assertTrue(properties.isEmpty());
        properties = repository.getPropertiesWithType(Set.class);
        assertFalse(properties.isEmpty());
    }

    @Test
    void verifyQueryOperation() throws Throwable {
        var properties = CasConfigurationMetadataCatalog.query(ConfigurationMetadataCatalogQuery
            .builder()
            .build());
        assertFalse(properties.properties().isEmpty());

        val file = File.createTempFile("config", ".yml");
        CasConfigurationMetadataCatalog.export(file, properties);
        assertTrue(file.exists());

        properties = CasConfigurationMetadataCatalog.query(ConfigurationMetadataCatalogQuery
            .builder()
            .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.CAS)
            .build());
        assertTrue(properties.properties().isEmpty());

        properties = CasConfigurationMetadataCatalog.query(ConfigurationMetadataCatalogQuery
            .builder()
            .modules(List.of("some-module-name"))
            .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.THIRD_PARTY)
            .build());
        assertTrue(properties.properties().isEmpty());
    }

    @Test
    void verifyRequiredProps() throws Throwable {
        val query = ConfigurationMetadataCatalogQuery
            .builder()
            .modules(List.of("cas-server-support-ldap"))
            .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.CAS)
            .requiredPropertiesOnly(Boolean.TRUE)
            .build();
        val file = Arrays.stream(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:META-INF/spring-configuration-metadata.json"))
            .filter(Unchecked.predicate(r -> r.isFile() && r.getURL().getFile().contains("cas-server-core-api-configuration-model")))
            .findFirst()
            .orElseThrow()
            .getFile();
        val rootDir = new File(file.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile(), "build/libs");
        val configModelJar = rootDir.listFiles(filter -> filter.getName().matches("cas-server-core-api-configuration-model.+-SNAPSHOT.jar"))[0];
        try (val jarFile = new JarFile(configModelJar.getCanonicalPath())) {
            val entry = jarFile.getJarEntry("META-INF/spring-configuration-metadata.json");
            try (val inputStream = jarFile.getInputStream(entry)) {
                val repository = new CasConfigurationMetadataRepository(new ByteArrayResource(inputStream.readAllBytes()));
                val properties = CasConfigurationMetadataCatalog.query(query, repository);
                assertFalse(properties.properties().isEmpty());
            }
        }
    }
}
