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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Test the {@link MultiRowJdbcPersonAttributeDao} against a dummy DataSource.
 *
 * @author andrew.petro@yale.edu
 * @author Eric Dalquist
 * @since 7.1.0
 */
@Tag("JDBCAuthentication")
class MultiRowJdbcPersonAttributeDaoTests {
    private DataSource testDataSource;

    private void setUpSchema() throws Exception {
        try (val con = testDataSource.getConnection()) {
            con.prepareStatement("CREATE TABLE user_table "
                + "(netid VARCHAR(50), "
                + "attr_name VARCHAR(50), "
                + "attr_val VARCHAR(50))").execute();
            con.prepareStatement("INSERT INTO user_table "
                + "(netid, attr_name, attr_val) "
                + "VALUES ('awp9', 'name', 'Andrew')").execute();
            con.prepareStatement("INSERT INTO user_table "
                + "(netid, attr_name, attr_val) "
                + "VALUES ('awp9', 'email', 'andrew.petro@yale.edu')").execute();
            con.prepareStatement("INSERT INTO user_table "
                + "(netid, attr_name, attr_val) "
                + "VALUES ('awp9', 'shirt_color', 'blue')").execute();
            con.prepareStatement("INSERT INTO user_table "
                + "(netid, attr_name, attr_val) "
                + "VALUES ('edalquist', 'name', 'Eric')").execute();
            con.prepareStatement("INSERT INTO user_table "
                + "(netid, attr_name, attr_val) "
                + "VALUES ('edalquist', 'email', 'edalquist@unicon.net')").execute();
            con.prepareStatement("INSERT INTO user_table "
                + "(netid, attr_name, attr_val) "
                + "VALUES ('edalquist', 'shirt_color', 'blue')").execute();
            con.prepareStatement("INSERT INTO user_table "
                + "(netid, attr_name, attr_val) "
                + "VALUES ('atest', 'name', 'Andrew')").execute();
            con.prepareStatement("INSERT INTO user_table "
                + "(netid, attr_name, attr_val) "
                + "VALUES ('atest', 'email', 'andrew.test@test.net')").execute();
            con.prepareStatement("INSERT INTO user_table "
                + "(netid, attr_name, attr_val) "
                + "VALUES ('atest', 'shirt_color', 'red')").execute();
            con.prepareStatement("INSERT INTO user_table "
                + "(netid, attr_name, attr_val) "
                + "VALUES ('susan', 'name', 'Susan')").execute();
            con.prepareStatement("INSERT INTO user_table "
                + "(netid, attr_name, attr_val) "
                + "VALUES ('susan', 'email', 'susan.test@test.net')").execute();
            con.prepareStatement("INSERT INTO user_table "
                + "(netid, attr_name, attr_val) "
                + "VALUES ('susan', 'shirt_color', null)").execute();
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
        val impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE netid = 'awp9'");
        impl.setUseAllQueryAttributes(false);

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");

        val columnsToAttributes = new LinkedHashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");
        val emailAttributeNames = new LinkedHashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);
        impl.setNameValueColumnMappings(Map.of("attr_name", "attr_val"));

        val attribs = impl.getPerson("awp9").getAttributes();
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.get("email"));
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(List.of("blue"), attribs.get("dressShirtColor"));
        assertNull(attribs.get("shirt_color"));
        assertEquals(List.of("Andrew"), attribs.get("firstName"));
    }

    @Test
    void testPossibleUserAttributeNames() {
        val impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Map.of("uid", "netid"));

        val columnsToAttributes = new LinkedHashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        val emailAttributeNames = new LinkedHashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        val expectedAttributeNames = new LinkedHashSet<String>();
        expectedAttributeNames.add("firstName");
        expectedAttributeNames.add("email");
        expectedAttributeNames.add("emailAddress");
        expectedAttributeNames.add("dressShirtColor");

        val attributeNames = impl.getPossibleUserAttributeNames(PersonAttributeDaoFilter.alwaysChoose());
        assertEquals(expectedAttributeNames, attributeNames);
    }

    @Test
    void testSingleAttrQuery() {
        val impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Map.of("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");

        val columnsToAttributes = new LinkedHashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        val emailAttributeNames = new LinkedHashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", "dressShirtColor");
        impl.setResultAttributeMapping(columnsToAttributes);

        impl.setNameValueColumnMappings(Map.of("attr_name", "attr_val"));

        val attribs = impl.getPerson("awp9").getAttributes();
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.get("email"));
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(List.of("blue"), attribs.get("dressShirtColor"));
        assertNull(attribs.get("shirt_color"));
        assertEquals(List.of("Andrew"), attribs.get("firstName"));
    }

    @Test
    void testInvalidColumnName() {
        val impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Map.of("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");

        val columnsToAttributes = new LinkedHashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        columnsToAttributes.put("email", "emailAddress");
        impl.setResultAttributeMapping(columnsToAttributes);

        impl.setNameValueColumnMappings(Map.of("attr_nam", "attr_val"));
        assertThrows(Exception.class, () -> impl.getPerson("awp9"));

        impl.setNameValueColumnMappings(Map.of("attr_name", "attr_va"));
        assertThrows(Exception.class, () -> impl.getPerson("awp9"));
    }

    @Test
    void testSetNullAttributeMapping() {
        val impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(Map.of("uid", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");

        val columnsToAttributes = new LinkedHashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        val emailAttributeNames = new LinkedHashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        columnsToAttributes.put("shirt_color", null);
        impl.setResultAttributeMapping(columnsToAttributes);

        impl.setNameValueColumnMappings(Map.of("attr_name", "attr_val"));

        val attribs = impl.getPerson("awp9").getAttributes();
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.get("email"));
        assertEquals(List.of("andrew.petro@yale.edu"), attribs.get("emailAddress"));
        assertEquals(List.of("blue"), attribs.get("shirt_color"));
        assertEquals(List.of("Andrew"), attribs.get("firstName"));
    }

    @Test
    void testMultiAttrQuery() {
        val queryAttributeMapping = new LinkedHashMap<String, String>();
        queryAttributeMapping.put("uid", "netid");
        queryAttributeMapping.put("shirtColor", "attr_val");

        val impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(queryAttributeMapping);

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");

        val columnsToAttributes = new LinkedHashMap<String, Object>();
        columnsToAttributes.put("shirt_color", "color");
        impl.setResultAttributeMapping(columnsToAttributes);

        impl.setNameValueColumnMappings(Map.of("attr_name", "attr_val"));

        val queryMap = new LinkedHashMap<String, List<Object>>();
        queryMap.put("uid", List.of("awp9"));
        queryMap.put("shirtColor", List.of("blue"));
        queryMap.put("Name", List.of("John"));

        val attribsSet = impl.getPeopleWithMultivaluedAttributes(queryMap);
        assertEquals(List.of("blue"), attribsSet.iterator().next().getAttributes().get("color"));
    }

    @Test
    void testInsufficientAttrQuery() {
        val queryAttributeMapping = new LinkedHashMap<String, String>();
        queryAttributeMapping.put("uid", "netid");
        queryAttributeMapping.put("shirtColor", "attr_val");

        var impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, attr_name, attr_val FROM user_table WHERE {0}");
        impl.setQueryAttributeMapping(queryAttributeMapping);

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("uid"));
        impl.setUnmappedUsernameAttribute("netid");
        impl.setRequireAllQueryAttributes(true);

        val columnsToAttributes = new LinkedHashMap<String, Object>();
        columnsToAttributes.put("name", "firstName");

        val emailAttributeNames = new LinkedHashSet<String>();
        emailAttributeNames.add("email");
        emailAttributeNames.add("emailAddress");
        columnsToAttributes.put("email", emailAttributeNames);
        impl.setResultAttributeMapping(columnsToAttributes);

        impl.setNameValueColumnMappings(Map.of("attr_name", "attr_val"));

        val queryMap = new LinkedHashMap<String, List<Object>>();
        queryMap.put("uid", List.of("awp9"));
        queryMap.put("Name", List.of("John"));

        val attribsSet = impl.getPeopleWithMultivaluedAttributes(queryMap);
        assertNull(attribsSet);
    }

    @Test
    void testProperties() {
        val impl = new MultiRowJdbcPersonAttributeDao(testDataSource, "SELECT netid, name, email FROM user_table WHERE shirt_color = ?");
        impl.setQueryAttributeMapping(Map.of("shirt", "netid"));

        impl.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("shirt"));
        impl.setUnmappedUsernameAttribute("netid");

        val columnsToAttributes = new LinkedHashMap<String, Object>();
        columnsToAttributes.put("netid", "uid");
        columnsToAttributes.put("name", "firstName");

        val expectedColumnsToAttributes = new LinkedHashMap<String, Object>();
        expectedColumnsToAttributes.put("netid", Set.of("uid"));
        expectedColumnsToAttributes.put("name", Set.of("firstName"));

        assertNull(impl.getResultAttributeMapping());
        impl.setResultAttributeMapping(columnsToAttributes);
        assertEquals(expectedColumnsToAttributes, impl.getResultAttributeMapping());

        impl.setNameValueColumnMappings(Map.of("attr_name", "attr_val"));
        assertEquals(Map.of("attr_name", Set.of("attr_val")), impl.getNameValueColumnMappings());

    }

}
