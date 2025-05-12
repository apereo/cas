package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.merger.MultivaluedAttributeMerger;
import org.apereo.cas.authentication.principal.merger.NoncollidingAttributeAdder;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testcase for {@link StubPersonAttributeDao}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Attributes")
class MergingPersonAttributeDaoImplTests {
    private static final String QUERY_ATTR = "username";

    private StubPersonAttributeDao sourceNull;

    private StubPersonAttributeDao sourceOne;

    private StubPersonAttributeDao sourceTwo;

    private StubPersonAttributeDao collidesWithOne;

    private Map<String, List<Object>> oneAndTwo;

    private Map<String, List<Object>> oneAndTwoAndThree;


    @BeforeEach
    protected void setUp() {
        sourceNull = new StubPersonAttributeDao();

        sourceOne = new StubPersonAttributeDao();
        val sourceOneMap = new HashMap<String, List<Object>>();
        sourceOneMap.put("shirtColor", List.of("blue"));
        sourceOneMap.put("favoriteColor", List.of("purple"));
        sourceOne.setBackingMap(sourceOneMap);

        sourceTwo = new StubPersonAttributeDao();
        val sourceTwoMap = new HashMap<String, List<Object>>();
        sourceTwoMap.put("tieColor", List.of("black"));
        sourceTwoMap.put("shoeType", List.of("closed-toe"));
        sourceTwo.setBackingMap(sourceTwoMap);

        oneAndTwo = new HashMap<>();
        oneAndTwo.putAll(sourceOneMap);
        oneAndTwo.putAll(sourceTwoMap);

        collidesWithOne = new StubPersonAttributeDao();
        val collidingMap = new HashMap<String, List<Object>>();
        collidingMap.put("shirtColor", List.of("white"));
        collidingMap.put("favoriteColor", List.of("red"));
        collidesWithOne.setBackingMap(collidingMap);

        oneAndTwoAndThree = new HashMap<>();
        var merger = new MultivaluedAttributeMerger();
        oneAndTwoAndThree = merger.mergeAttributes(oneAndTwoAndThree, sourceOneMap);
        oneAndTwoAndThree = merger.mergeAttributes(oneAndTwoAndThree, sourceTwoMap);
        oneAndTwoAndThree = merger.mergeAttributes(oneAndTwoAndThree, collidingMap);
    }

    @Test
    void testBasics() {
        val attributeSources = new ArrayList<PersonAttributeDao>();

        attributeSources.add(sourceNull);
        attributeSources.add(sourceOne);
        attributeSources.add(sourceNull);
        attributeSources.add(sourceTwo);

        var impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);

        val queryMap = new HashMap<String, List<Object>>();
        queryMap.put(QUERY_ATTR, List.of("awp9"));

        val result = impl.getPeopleWithMultivaluedAttributes(queryMap);
        val attributes = new HashMap<>(oneAndTwo);
        attributes.putAll(queryMap);
        assertEquals(attributes, result.iterator().next().getAttributes());
        assertTrue(impl.getId().length > 0);
    }

    @Test
    void testAttributeNames() {
        val attributeSources = new ArrayList<PersonAttributeDao>();
        attributeSources.add(sourceOne);
        attributeSources.add(sourceTwo);

        var impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);

        var attributeNames = impl.getPossibleUserAttributeNames(PersonAttributeDaoFilter.alwaysChoose());
        assertEquals(oneAndTwo.keySet(), attributeNames);
    }

    @Test
    void verifyStopOnSuccess() {
        val attributeSources = new ArrayList<PersonAttributeDao>();
        attributeSources.add(sourceOne);
        attributeSources.add(sourceTwo);

        val impl = new MergingPersonAttributeDaoImpl();
        impl.setStopOnSuccess(true);
        impl.setPersonAttributeDaos(attributeSources);

        val queryMap = new HashMap<String, List<Object>>();
        queryMap.put(QUERY_ATTR, List.of("awp9"));
        val person = impl.getPeopleWithMultivaluedAttributes(queryMap, PersonAttributeDaoFilter.alwaysChoose());
        assertEquals(1, person.size());
    }

    @Test
    void testExceptionHandling() {
        val attributeSources = new ArrayList<PersonAttributeDao>();

        attributeSources.add(sourceOne);
        attributeSources.add(sourceTwo);
        attributeSources.add(collidesWithOne);

        val impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);

        val queryMap = new HashMap<String, List<Object>>();
        queryMap.put(QUERY_ATTR, List.of("awp9"));

        var result = impl.getPeopleWithMultivaluedAttributes(queryMap);
        val attributes = new HashMap<>(oneAndTwoAndThree);
        attributes.putAll(queryMap);
        assertEquals(attributes, result.iterator().next().getAttributes());
    }

    @Test
    void testExceptionHandlingRecoveryDisabled() {
        val attributeSources = new ArrayList<PersonAttributeDao>();

        val failingDao = mock(PersonAttributeDao.class);
        when(failingDao.getPeopleWithMultivaluedAttributes(any(), any(), any()))
            .thenThrow(new UnauthorizedAuthenticationException("Failed"));
        
        attributeSources.add(failingDao);

        val impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        impl.setRecoverExceptions(false);

        val queryMap = new HashMap<String, List<Object>>();
        queryMap.put(QUERY_ATTR, List.of("awp9"));

        assertThrows(UnauthorizedAuthenticationException.class,
            () -> impl.getPeopleWithMultivaluedAttributes(queryMap));
    }

    @Test
    void testNullAttribNames() {
        val attributeSources = new ArrayList<PersonAttributeDao>();

        attributeSources.add(sourceOne);
        attributeSources.add(sourceTwo);
        attributeSources.add(collidesWithOne);

        var impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);

        var attribNames = impl.getPossibleUserAttributeNames(PersonAttributeDaoFilter.alwaysChoose());

        val expectedAttribNames = new HashSet<String>();
        expectedAttribNames.addAll(sourceOne.getPossibleUserAttributeNames(PersonAttributeDaoFilter.alwaysChoose()));
        expectedAttribNames.addAll(sourceTwo.getPossibleUserAttributeNames(PersonAttributeDaoFilter.alwaysChoose()));
        expectedAttribNames.addAll(collidesWithOne.getPossibleUserAttributeNames(PersonAttributeDaoFilter.alwaysChoose()));

        assertEquals(expectedAttribNames, attribNames);

        var attributes = impl.getAvailableQueryAttributes(PersonAttributeDaoFilter.alwaysChoose());
        assertTrue(attributes.isEmpty());

        val failingDao = mock(PersonAttributeDao.class);
        when(failingDao.getAvailableQueryAttributes(any()))
            .thenThrow(new UnauthorizedAuthenticationException("Failed"));
        val impl2 = new MergingPersonAttributeDaoImpl();
        impl2.setPersonAttributeDaos(List.of(failingDao));
        impl2.setRecoverExceptions(false);

        assertThrows(UnauthorizedAuthenticationException.class,
            () -> impl2.getAvailableQueryAttributes(PersonAttributeDaoFilter.alwaysChoose()));
    }

    @Test
    void testAlternativeMerging() {
        val attributeSources = new ArrayList<PersonAttributeDao>();

        attributeSources.add(sourceOne);
        attributeSources.add(sourceTwo);
        attributeSources.add(collidesWithOne);

        var impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);
        impl.setAttributeMerger(new NoncollidingAttributeAdder());

        val queryMap = new HashMap<String, List<Object>>();
        queryMap.put(QUERY_ATTR, List.of("awp9"));

        var result = impl.getPeopleWithMultivaluedAttributes(queryMap);
        val attributes = new HashMap<>(oneAndTwo);
        attributes.putAll(queryMap);
        assertEquals(attributes, result.iterator().next().getAttributes());
    }

    @Test
    void testUsernameWildcardQuery() {
        val attributeSources = new ArrayList<PersonAttributeDao>();

        var complexSourceOne = new ComplexPersonAttributeDao();
        val backingMapOne = new HashMap<String, Map<String, List<Object>>>();

        val loHomeAttrs = new HashMap<String, List<Object>>();
        loHomeAttrs.put("username", List.of("lo-home"));
        loHomeAttrs.put("givenName", List.of("Home"));
        loHomeAttrs.put("familyName", List.of("Layout Owner"));
        backingMapOne.put("lo-home", loHomeAttrs);

        val loWelcomeAttrs = new HashMap<String, List<Object>>();
        loWelcomeAttrs.put("username", List.of("lo-welcome"));
        loWelcomeAttrs.put("givenName", List.of("Welcome"));
        loWelcomeAttrs.put("familyName", List.of("Layout Owner"));
        backingMapOne.put("lo-welcome", loWelcomeAttrs);

        complexSourceOne.setBackingMap(backingMapOne);
        attributeSources.add(complexSourceOne);

        var complexSourceTwo = new ComplexPersonAttributeDao();
        val backingMapTwo = new HashMap<String, Map<String, List<Object>>>();

        val edalquistAttrs = new HashMap<String, List<Object>>();
        edalquistAttrs.put("username", List.of("edalquist"));
        edalquistAttrs.put("givenName", List.of("Eric"));
        edalquistAttrs.put("familyName", List.of("Dalquist"));
        backingMapTwo.put("edalquist", edalquistAttrs);

        val jshomeAttrs = new HashMap<String, List<Object>>();
        jshomeAttrs.put("username", List.of("jshome"));
        jshomeAttrs.put("givenName", List.of("Joe"));
        jshomeAttrs.put("familyName", List.of("Shome"));
        backingMapTwo.put("jshome", jshomeAttrs);

        complexSourceTwo.setBackingMap(backingMapTwo);
        attributeSources.add(complexSourceTwo);


        var impl = new MergingPersonAttributeDaoImpl();
        impl.setPersonAttributeDaos(attributeSources);

        var layoutOwners = impl.getPeople(Map.of("username", "lo-*"));

        val expectedLayoutOwners = new HashSet<PersonAttributes>();
        expectedLayoutOwners.add(new SimplePersonAttributes("lo-welcome", loWelcomeAttrs));
        expectedLayoutOwners.add(new SimplePersonAttributes("lo-home", loHomeAttrs));

        assertEquals(expectedLayoutOwners, layoutOwners);

        var homeUsers = impl.getPeople(Map.of("username", "*home"));
        val expectedHomeUsers = new HashSet<PersonAttributes>();
        expectedHomeUsers.add(new SimplePersonAttributes("jshome", jshomeAttrs));
        expectedHomeUsers.add(new SimplePersonAttributes("lo-home", loHomeAttrs));

        assertEquals(expectedHomeUsers, homeUsers);
    }
}
