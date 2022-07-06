package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.*;

/**
 * This is {@link GroovyPasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.authn.passwordless.accounts.json.location=classpath:PasswordlessAccount.json")
@Tag("FileSystem")
public class JsonPasswordlessUserAccountStoreTests extends BasePasswordlessUserAccountStoreTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).singleValueAsArray(true).build().toObjectMapper();

    @Autowired
    @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Test
    public void verifyAction() {
        val user = passwordlessUserAccountStore.findUser("casuser");
        assertTrue(user.isPresent());
    }

    @Test
    public void verifyReload() throws Exception {
        val file = File.createTempFile("file", ".json");
        FileUtils.writeStringToFile(file, MAPPER.writeValueAsString(new HashMap<>()), StandardCharsets.UTF_8);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                val resource = new JsonPasswordlessUserAccountStore(new FileSystemResource(file));
                assertTrue(resource.getAccounts().isEmpty());
                val account = PasswordlessUserAccount.builder().username("casuser").build();
                val json = MAPPER.writeValueAsString(CollectionUtils.wrap("casuser", account));
                FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
                Files.setLastModifiedTime(file.toPath(), FileTime.from(Instant.now()));
                Thread.sleep(1_000);
                await().untilAsserted(() -> assertFalse(resource.getAccounts().isEmpty()));
            }
        });
    }
}
