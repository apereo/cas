package org.apereo.cas.interrupt;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import tools.jackson.databind.ObjectMapper;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonResourceInterruptInquirerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("FileSystem")
class JsonResourceInterruptInquirerTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    void verifyResponse() throws Throwable {
        val map = new LinkedHashMap<String, InterruptResponse>();
        var response = new InterruptResponse("Message",
            CollectionUtils.wrap("text", "link", "text2", "link2"), false, true);
        response.setData(CollectionUtils.wrap("field1", List.of("value1", "value2"),
            "field2", List.of("value3", "value4")));
        map.put("casuser", response);

        val jsonFile = Files.createTempFile("interrupt", "json").toFile();
        MAPPER.writeValue(jsonFile, map);
        assertTrue(jsonFile.exists());

        val inquirer = new JsonResourceInterruptInquirer(new FileSystemResource(jsonFile));
        response = inquirer.inquire(CoreAuthenticationTestUtils.getAuthentication("unknown"),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            new MockRequestContext());
        assertFalse(response.isInterrupt());

        response = inquirer.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            new MockRequestContext());
        assertNotNull(response);
        assertFalse(response.isBlock());
        assertTrue(response.isSsoEnabled());
        assertEquals(2, response.getLinks().size());
        assertTrue(response.getData().containsKey("field1"));
        assertTrue(response.getData().containsKey("field2"));

        inquirer.destroy();
    }

    @Test
    void verifyConcurrentInquiriesUseLoadedDefinitions() throws Throwable {
        val json = MAPPER.writeValueAsString(Map.of("casuser", new InterruptResponse("Blocked", true, false)));
        val reads = new AtomicInteger();
        val resource = new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8), "Interrupts") {
            @Override
            public InputStream getInputStream() throws IOException {
                reads.incrementAndGet();
                return super.getInputStream();
            }
        };
        val inquirer = new JsonResourceInterruptInquirer(resource);
        val readsAfterLoad = reads.get();
        assertTrue(readsAfterLoad > 0);
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val results = executor.invokeAll(IntStream.range(0, 50)
                .<Callable<InterruptResponse>>mapToObj(_ -> () -> inquire(inquirer, "casuser"))
                .toList());
            for (val result : results) {
                assertTrue(result.get().isBlock());
            }
        }
        assertEquals(readsAfterLoad, reads.get());
    }

    @Test
    void verifyFileChangesAreApplied(@TempDir final Path directory) {
        val jsonFile = directory.resolve("interrupts.json").toFile();
        MAPPER.writeValue(jsonFile, Map.of("casuser", new InterruptResponse("First")));
        val inquirer = new JsonResourceInterruptInquirer(new FileSystemResource(jsonFile));
        try {
            assertTrue(inquire(inquirer, "casuser").isInterrupt());
            MAPPER.writeValue(jsonFile, Map.of("otheruser", new InterruptResponse("Second")));
            await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
                assertTrue(inquire(inquirer, "otheruser").isInterrupt());
                assertFalse(inquire(inquirer, "casuser").isInterrupt());
            });
        } finally {
            inquirer.destroy();
        }
    }

    private static InterruptResponse inquire(final InterruptInquirer inquirer, final String username) {
        try {
            return inquirer.inquire(CoreAuthenticationTestUtils.getAuthentication(username),
                CoreAuthenticationTestUtils.getRegisteredService(),
                CoreAuthenticationTestUtils.getService(),
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                new MockRequestContext());
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
