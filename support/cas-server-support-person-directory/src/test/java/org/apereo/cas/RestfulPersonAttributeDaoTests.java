package org.apereo.cas;

import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.util.MockWebServer;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulPersonAttributeDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasPersonDirectoryConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = {
    "cas.authn.attributeRepository.rest[0].method=GET",
    "cas.authn.attributeRepository.rest[0].url=http://localhost:8085"
})
@Tag("RestfulApi")
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
        val person = attributeRepository.getPerson("casuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);
        assertNotNull(person.getAttributes());
        assertFalse(person.getAttributes().isEmpty());
        assertEquals("casuser", person.getAttributeValue("name"));
        assertEquals(29, person.getAttributeValue("age"));
        assertEquals(3, person.getAttributeValues("messages").size());
    }
}
