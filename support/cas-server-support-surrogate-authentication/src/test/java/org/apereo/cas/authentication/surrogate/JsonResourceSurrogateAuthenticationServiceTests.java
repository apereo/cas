package org.apereo.cas.authentication.surrogate;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.core.io.ClassPathResource;

/**
 * This is {@link JsonResourceSurrogateAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Tag("FileSystem")
public class JsonResourceSurrogateAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {

    private SurrogateAuthenticationService service;

    @BeforeEach
    @SneakyThrows
    public void initTests() {
        val resource = new ClassPathResource("surrogates.json");
        service = new JsonResourceSurrogateAuthenticationService(resource, servicesManager);
    }
}
