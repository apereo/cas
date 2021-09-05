package org.apereo.cas.acct;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAccountRegistrationPropertyLoaderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("FileSystem")
public class DefaultAccountRegistrationPropertyLoaderTests {
    @Test
    public void verifyOperation() throws Exception {
        val map = new HashMap<String, AccountRegistrationProperty>();
        map.put("username", AccountRegistrationProperty.builder()
            .name("username")
            .label("cas.screen.acct.label.username")
            .required(true)
            .build());
        map.put("firstName", AccountRegistrationProperty.builder()
            .name("firstName")
            .label("cas.screen.acct.label.firstName")
            .required(true)
            .build());
        map.put("lastName", AccountRegistrationProperty.builder()
            .name("lastName")
            .label("cas.screen.acct.label.lastName")
            .required(true)
            .build());
        val resource = new FileSystemResource(File.createTempFile("accounts", ".json"));
        val loader = new DefaultAccountRegistrationPropertyLoader(resource);
        loader.store(map);
        assertFalse(loader.load().isEmpty());
    }
}
