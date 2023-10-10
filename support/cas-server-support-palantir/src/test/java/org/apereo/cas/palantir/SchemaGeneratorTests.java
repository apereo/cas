package org.apereo.cas.palantir;

import org.apereo.cas.palantir.schema.SchemaGenerator;
import org.apereo.cas.services.CasProtocolVersions;
import org.apereo.cas.services.RegisteredServiceAccessStrategyActivationCriteria;
import org.apereo.cas.services.RegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceServiceTicketExpirationPolicy;
import org.apereo.cas.services.RegisteredServiceTicketGrantingTicketExpirationPolicy;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

/**
 * This is {@link SchemaGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Simple")
public class SchemaGeneratorTests {
    @ParameterizedTest
    @MethodSource("getTestParameters")
    void verifyOperation(final Class mainClass, final List excluded, final Resource schemaResource) throws Exception {
        val results = SchemaGenerator.generate(mainClass, excluded);
        val expectedSchema = IOUtils.toString(schemaResource.getInputStream(), StandardCharsets.UTF_8);
        assertEquals(StringUtils.deleteWhitespace(expectedSchema), StringUtils.deleteWhitespace(results.toPrettyString()));
    }

    public static Stream<Arguments> getTestParameters() {
        return Stream.of(
            arguments(SimpleContainer1.class, Collections.emptyList(), getExpectedSchemaResource("SimpleContainer1")),
            arguments(SimpleContainer2.class, Collections.emptyList(), getExpectedSchemaResource("SimpleContainer2")),
            arguments(SimpleContainer3.class, Collections.emptyList(), getExpectedSchemaResource("SimpleContainer3")),
            arguments(SimpleContainer4.class, Collections.emptyList(), getExpectedSchemaResource("SimpleContainer4")),
            arguments(SimpleContainer5.class, Collections.emptyList(), getExpectedSchemaResource("SimpleContainer5"))
        );
    }

    @Nonnull
    private static ClassPathResource getExpectedSchemaResource(final String name) {
        return new ClassPathResource("schema/%s.schema.json".formatted(name));
    }

    @Getter
    @Setter
    static class SimpleContainer1 {
        private RegisteredServiceTicketGrantingTicketExpirationPolicy policy;
        private RegisteredServiceProperty property;
        private Map<String, RegisteredServiceProperty> details;
    }

    @Getter
    @Setter
    static class SimpleContainer2 {
        private RegisteredServiceAccessStrategyActivationCriteria criteria;
    }

    @Getter
    @Setter
    static class SimpleContainer3 {
        private RegisteredServiceAuthenticationPolicyCriteria criteria;
    }

    @Getter
    @Setter
    static class SimpleContainer4 {
        private Set<CasProtocolVersions> supportedProtocols = EnumSet.allOf(CasProtocolVersions.class);
    }

    @Getter
    @Setter
    static class SimpleContainer5 {
        private RegisteredServiceServiceTicketExpirationPolicy policy;
    }
}
