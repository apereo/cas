package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.ticket.registry.mysql.MySQLJpaTicketEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.orm.jpa.EntityManagerProxy;
import jakarta.persistence.EntityManager;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Native")
class JpaRuntimeHintsTests {

    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new JpaRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(EntityManager.class, EntityManagerProxy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(MySQLJpaTicketEntity.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onConstructorInvocation(JsonType.class.getDeclaredConstructor()).test(hints));
    }

    @Test
    void verifyDialectSettingsSerialization() throws Exception {
        val hints = new RuntimeHints();
        new JpaRuntimeHints().registerHints(hints, getClass().getClassLoader());
        try (val output = new ObjectOutputStream(new ByteArrayOutputStream()) {
            @Override
            protected void writeClassDescriptor(final ObjectStreamClass descriptor) throws IOException {
                if (descriptor.getName().startsWith("org.apereo.cas.")) {
                    val hint = hints.reflection().getTypeHint(TypeReference.of(descriptor.getName()));
                    assertNotNull(hint, descriptor.getName());
                    assertTrue(hint.hasJavaSerialization(), descriptor.getName());
                }
                super.writeClassDescriptor(descriptor);
            }
        }) {
            output.writeObject(new JpaTicketRegistryProperties());
        }
    }
}
