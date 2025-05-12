package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.configuration.support.JpaBeans;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the {@link SingleRowJdbcPersonAttributeDao} against a dummy DataSource.
 *
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @since 7.1.0
 */
@Tag("JDBCAuthentication")
class SingleRowJdbcPersonAttributeDaoTests {

    private DataSource testDataSource;

    private void setUpSchema() throws Exception {
        try (val con = testDataSource.getConnection()) {
            con.prepareStatement("CREATE TABLE user_table "
                + "(netid VARCHAR(100), "
                + "name VARCHAR(100), "
                + "email VARCHAR(100), "
                + "shirt_color VARCHAR(100))").execute();

            con.prepareStatement("INSERT INTO user_table "
                + "(netid, name, email, shirt_color) "
                + "VALUES ('awp9', 'Andrew', 'andrew.petro@yale.edu', 'blue')").execute();

            con.prepareStatement("INSERT INTO user_table "
                + "(netid, name, email, shirt_color) "
                + "VALUES ('edalquist', 'Eric', 'edalquist@unicon.net', 'blue')").execute();

            con.prepareStatement("INSERT INTO user_table "
                + "(netid, name, email, shirt_color) "
                + "VALUES ('atest', 'Andrew', 'andrew.test@test.net', 'red')").execute();

            con.prepareStatement("INSERT INTO user_table "
                + "(netid, name, email, shirt_color) "
                + "VALUES ('susan', 'Susan', 'susan.test@test.net', null)").execute();
        }
    }

    @BeforeEach
    void setup() throws Exception {
        testDataSource = JpaBeans.newDataSource("org.hsqldb.jdbcDriver", "sa", StringUtils.EMPTY, "jdbc:hsqldb:mem:cas");
        setUpSchema();
    }

    @AfterEach
    void shutdown() throws Exception {
        try (val con = testDataSource.getConnection()) {
            con.prepareStatement("DROP TABLE user_table").execute();
        }
    }

    @Test
    void testNoQueryAttributeMapping() {
        val impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE netid = 'awp9'");
        impl.setUseAllQueryAttributes(false);

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        val columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        val emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        val attribs = impl.getPerson("awp9");
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.getAttributes().get("email"));
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.getAttributes().get("emailAddress"));
        assertEquals(List.of("blue"), attribs.getAttributes().get("dressShirtColor"));
        assertNull(attribs.getAttributes().get("shirt_color"));
        assertEquals(List.of("Andrew"), attribs.getAttributes().get("firstName"));
    }

    @Test
    void testPossibleUserAttributeNames() {
        val impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Map.of("uid", "netid"));

        val columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        val emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        val expectedAttributeNames = new HashSet<String>();
        expectedAttributeNames.add("firstName");
        expectedAttributeNames.add("email");
        expectedAttributeNames.add("emailAddress");
        expectedAttributeNames.add("dressShirtColor");

        val attributeNames = impl.getPossibleUserAttributeNames(PersonAttributeDaoFilter.alwaysChoose());
        assertEquals(attributeNames, expectedAttributeNames);
    }

    @Test
    void testSingleAttrQuery() {
        val impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Map.of("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        val columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        val emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        val attribs = impl.getPerson("awp9").getAttributes();
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.get("email"));
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(List.of("blue"), attribs.get("dressShirtColor"));
        assertNull(attribs.get("shirt_color"));
        assertEquals(List.of("Andrew"), attribs.get("firstName"));
    }

    @Test
    void testSetNullAttributeMapping() {
        val impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Map.of("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        val columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        val emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", null);
        impl.setResultAttributeMapping(columnsToAttributes);

        val attribs = impl.getPerson("awp9").getAttributes();
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.get("email"));
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(List.of("blue"), attribs.get("shirt_color"));
        assertEquals(List.of("Andrew"), attribs.get("firstName"));
    }

    @Test
    void testNullAttrQuery() {
        val impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email, shirt_color FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Map.of("uid", "netid"));
        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        val columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        val attribs = impl.getPerson("susan").getAttributes();
        assertTrue(attribs.get("dressShirtColor").isEmpty());
        assertEquals(List.of("Susan"), attribs.get("firstName"));
    }

    @Test
    void testMultiAttrQuery() {
        val queryAttributeMapping = new LinkedHashMap<String, String>();
        queryAttributeMapping.put("uid", "netid");
        queryAttributeMapping.put("shirtColor", "shirt_color");

        val impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(queryAttributeMapping);

        val columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        val emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setResultAttributeMapping(columnsToAttributes);

        val queryMap = new HashMap<String, List<Object>>();
        queryMap.put("uid", List.of("awp9"));
        queryMap.put("shirtColor", List.of("blue"));
        queryMap.put("Name", List.of("John"));

        val attribsSet = impl.getPeopleWithMultivaluedAttributes(queryMap);
        assertNotNull(attribsSet);
        val attribs = attribsSet.iterator().next();
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.getAttributes().get("email"));
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.getAttributes().get("emailAddress"));
        assertEquals(List.of("Andrew"), attribs.getAttributeValues("firstName"));
    }

    @Test
    void testInsufficientAttrQuery() {
        val queryAttributeMapping = new LinkedHashMap<String, String>();
        queryAttributeMapping.put("uid", "netid");
        queryAttributeMapping.put("shirtColor", "shirt_color");

        val impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(queryAttributeMapping);
        impl.setRequireAllQueryAttributes(true);

        val columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        val emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setResultAttributeMapping(columnsToAttributes);

        val queryMap = new HashMap<String, List<Object>>();
        queryMap.put("uid", List.of("awp9"));
        queryMap.put("Name", List.of("John"));

        val attribs = impl.getPeopleWithMultivaluedAttributes(queryMap);
        assertNull(attribs);
    }

    @Test
    void testMultiPersonQuery() {
        val impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, name, email FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Map.of("shirt", "shirt_color"));
        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));

        val columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");

        val emailAttributeNames = new HashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);

        impl.setResultAttributeMapping(columnsToAttributes);

        val queryMap = new HashMap<String, List<Object>>();
        queryMap.put("shirt", List.of("blue"));

        val results = impl.getPeopleWithMultivaluedAttributes(queryMap);
        if (results.size() <= 1) {
            fail("Repository should have returned multiple people in the set");
        }

    }

    @Test
    void testProperties() {
        val impl = new SingleRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, name, email FROM user_table WHERE {0}");

        impl.setQueryAttributeMapping(Map.of("shirt", "shirt_color"));
        assertEquals(Map.of("shirt", Collections.singleton("shirt_color")), impl.getQueryAttributeMapping());

        val columnsToAttributes = new HashMap<String, Object>();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");

        val expectedColumnsToAttributes = new HashMap<String, Set<String>>();
        expectedColumnsToAttributes.put("netid", Collections.singleton("uid"));
        expectedColumnsToAttributes.put("name", Collections.singleton("firstName"));

        impl.setResultAttributeMapping(columnsToAttributes);
        assertEquals(expectedColumnsToAttributes, impl.getResultAttributeMapping());
    }

}
