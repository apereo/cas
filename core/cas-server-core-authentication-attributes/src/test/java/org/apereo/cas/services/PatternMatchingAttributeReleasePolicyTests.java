package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PatternMatchingAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("AttributeRelease")
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreScriptingAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class PatternMatchingAttributeReleasePolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifySerializeToJson() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val policy = new PatternMatchingAttributeReleasePolicy();
        assertNotNull(policy.getName());
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern("CN=(\\w+),OU=(\\w+)")
                .setTransform("${1}/${2}"));
        MAPPER.writeValue(jsonFile, policy);
        val policyRead = MAPPER.readValue(jsonFile, PatternMatchingAttributeReleasePolicy.class);
        assertEquals(policy, policyRead);
    }

    @Test
    void verifyPatternTransform() throws Throwable {
        val policy = new PatternMatchingAttributeReleasePolicy();
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern("^CN=(\\w+),\\s*OU=(\\w+),\\s*DC=(\\w+)")
                .setTransform("${1}@${2}/${3}"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal(
                Map.of("memberOf", List.of("CN=g1,OU=example,DC=org", "CN=g2,OU=example,DC=org"),
                    "another", List.of("CN=g3", "CN=g4"))))
            .build();
        val attributes = policy.getAttributes(releasePolicyContext);
        assertTrue(attributes.containsKey("memberOf"));
        assertFalse(attributes.containsKey("another"));

        val values = attributes.get("memberOf");
        assertTrue(values.contains("g1@example/org"));
        assertTrue(values.contains("g2@example/org"));
    }

    @Test
    void verifyTransformEntireMatch() throws Throwable {
        val policy = new PatternMatchingAttributeReleasePolicy();
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern("^CN=(\\w+),\\s*OU=(\\w+),\\s*DC=(\\w+)")
                .setTransform("${0}/org"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal(
                Map.of("memberOf", List.of("CN=g1,OU=example,DC=org"),
                    "another", List.of("CN=g3", "CN=g4"))))
            .build();
        val attributes = policy.getAttributes(releasePolicyContext);
        assertTrue(attributes.containsKey("memberOf"));
        assertFalse(attributes.containsKey("another"));

        val values = attributes.get("memberOf");
        assertTrue(values.contains("CN=g1,OU=example,DC=org/org"));
    }

    @Test
    void verifyGroovyTransformationRule() throws Throwable {
        val policy = new PatternMatchingAttributeReleasePolicy();
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern(".*CN=(\\w+),OU=example.*")
                .setTransform("""
                    groovy {
                        logger.info("Matched value is [{}] for [{}]", matched, context.principal.id)
                        def value = matchedGroup1 == 'g1' ? 'group1' : 'group2'
                        return [mem: [value]]
                    }
                """)
        );

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal(
                Map.of("memberOf", List.of("CN=g1,OU=example,DC=org", "CN=g2,OU=example,DC=org"),
                    "another", List.of("CN=g3", "CN=g4"))))
            .build();
        val attributes = policy.getAttributes(releasePolicyContext);
        assertEquals(1, attributes.size());
        assertTrue(attributes.containsKey("mem"));
        assertTrue(attributes.get("mem").contains("group1"));
        assertTrue(attributes.get("mem").contains("group2"));
    }

    @Test
    void verifyGroovyTransformationRuleReturningSingleValues() throws Throwable {
        val policy = new PatternMatchingAttributeReleasePolicy();
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern(".*CN=(\\w+),OU=example.*")
                .setTransform("groovy { return [mem: matchedGroup1] }"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal(
                Map.of("memberOf", List.of("CN=g1,OU=example,DC=org", "CN=g2,OU=example,DC=org"))))
            .build();

        val values = policy.getAttributes(releasePolicyContext).get("mem");
        assertEquals(2, values.size());
        assertTrue(values.containsAll(List.of("g1", "g2")),
            "A rule that returns a single value must release it as a collection");
    }

    @Test
    void verifyGroovyTransformationRuleReturningUnusableEntries() throws Throwable {
        val policy = new PatternMatchingAttributeReleasePolicy();
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern(".*CN=(\\w+),OU=example.*")
                .setTransform("groovy { return [mem: null, '': ['g0'], other: []] }"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal(
                Map.of("memberOf", List.of("CN=g1,OU=example,DC=org"))))
            .build();

        val attributes = assertDoesNotThrow(() -> policy.getAttributes(releasePolicyContext));
        assertTrue(attributes.isEmpty(), "Entries without a usable name or value must be dropped");
    }

    @Test
    void verifyGroovyTransformationRulesCombineIntoSameAttribute() throws Throwable {
        val transform = "groovy { return [mem: [matchedGroup1]] }";
        val policy = new PatternMatchingAttributeReleasePolicy();
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern(".*CN=(\\w+),OU=example.*")
                .setTransform(transform));
        policy.getAllowedAttributes().put("another",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern(".*CN=(\\w+),OU=sample.*")
                .setTransform(transform));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal(
                Map.of("memberOf", List.of("CN=g1,OU=example,DC=org"),
                    "another", List.of("CN=g3,OU=sample,DC=org"))))
            .build();

        val attributes = policy.getAttributes(releasePolicyContext);
        assertEquals(1, attributes.size());
        assertTrue(attributes.get("mem").containsAll(List.of("g1", "g3")),
            "Rules that map onto the same attribute must be combined rather than overwrite each other");
    }

    @Test
    void verifyRuleWithoutTransformationIsIgnored() throws Throwable {
        val policy = new PatternMatchingAttributeReleasePolicy();
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern(".*CN=(\\w+),OU=example.*"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal(
                Map.of("memberOf", List.of("CN=g1,OU=example,DC=org"))))
            .build();

        val attributes = assertDoesNotThrow(() -> policy.getAttributes(releasePolicyContext));
        assertTrue(attributes.isEmpty());
    }

    @Test
    void verifyGroovyTransformationRuleIsCompiledOnce() throws Throwable {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val cacheManager = ApplicationContextProvider.getScriptResourceCacheManager().orElseThrow();

        val transform = "groovy { return [mem: [matchedGroup1]] }";
        val policy = new PatternMatchingAttributeReleasePolicy();
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern(".*CN=(\\w+),OU=example.*")
                .setTransform(transform));

        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        val cacheKey = cacheManager.computeKey(scriptFactory.getInlineScript(transform.trim().stripIndent()).orElseThrow());
        cacheManager.remove(cacheKey);
        assertFalse(cacheManager.containsKey(cacheKey));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal(
                Map.of("memberOf", List.of("CN=g1,OU=example,DC=org", "CN=g2,OU=example,DC=org"))))
            .build();

        assertTrue(policy.getAttributes(releasePolicyContext).get("mem").contains("g1"));
        val compiledScript = cacheManager.get(cacheKey);
        assertNotNull(compiledScript, "The transformation script must be compiled and stored in the script cache");

        assertTrue(policy.getAttributes(releasePolicyContext).get("mem").contains("g2"));
        assertSame(compiledScript, cacheManager.get(cacheKey),
            "The transformation script must be compiled once and served from the script cache");
    }
}
