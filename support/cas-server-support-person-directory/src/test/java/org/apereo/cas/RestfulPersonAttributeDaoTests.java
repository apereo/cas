package org.apereo.cas;

import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link RestfulPersonAttributeDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasPersonDirectoryConfiguration.class,
    RefreshAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.attributeRepository.rest[0].method=GET",
    "cas.authn.attributeRepository.rest[0].url=http://localhost:8085"
})
@Category(RestfulApiCategory.class)
public class RestfulPersonAttributeDaoTests {
    @Autowired
    @Qualifier("attributeRepository")
    protected IPersonAttributeDao attributeRepository;

    private MockWebServer webServer;

    @BeforeEach
    public void initialize() {
        val data = '{'
            + "   \"name\" :\"casuser\","
            + "\"age\" : 29,"
            + "\"messages\": [\"msg 1\", \"msg 2\", \"msg 3\"]      "
            + '}';
        this.webServer = new MockWebServer(8085,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        this.webServer.start();
    }

    @AfterEach
    public void cleanup() {
        this.webServer.stop();
    }

    @Test
    public void verifyRestAttributeRepository() {
        assertNotNull(attributeRepository);
        val person = attributeRepository.getPerson("casuser");
        assertNotNull(person);
        assertNotNull(person.getAttributes());
        assertFalse(person.getAttributes().isEmpty());
        assertTrue(person.getAttributeValue("name").equals("casuser"));
        assertTrue(person.getAttributeValue("age").equals(29));
        assertEquals(3, person.getAttributeValues("messages").size());
    }
}
