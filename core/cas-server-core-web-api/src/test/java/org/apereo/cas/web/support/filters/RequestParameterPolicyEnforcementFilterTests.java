package org.apereo.cas.web.support.filters;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link RequestParameterPolicyEnforcementFilter}.
 * <p>
 * First there are test cases for the Filter as a whole against the Filter API.  The advantage of these is that they
 * are testing at the level we care about, the way the filter will actually be used,
 * against the API it really exposes to adopters.  So, great.  The disadvantage of these is that it's
 * <p>
 * Then there are test cases for bits of the implementation of the filter (namely, configuration parsing and policy
 * enforcement).
 *
 * @author Andrew Petro
 * @author Misagh Moayyed
 * @since 6.1
 */
@Tag("Web")
class RequestParameterPolicyEnforcementFilterTests {

    private static void internalTestOnlyPostParameter(final String method) {
        val onlyPostParameters = new HashSet<String>();
        onlyPostParameters.add("username");
        onlyPostParameters.add("password");

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("username", new String[]{"jle"});

        RequestParameterPolicyEnforcementFilter.checkOnlyPostParameters(method, parameterMap, onlyPostParameters);
    }
    
    @Test
    void verifyParseFails() {
        RequestParameterPolicyEnforcementFilter.enforceParameterContentCharacterRestrictions(Set.of(), Set.of(), Map.of());
        assertThrows(RuntimeException.class, () -> RequestParameterPolicyEnforcementFilter.parseParametersList(" ", false));
        assertThrows(RuntimeException.class, () -> RequestParameterPolicyEnforcementFilter.parseParametersList("one *", false));
        assertThrows(RuntimeException.class, () -> RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("  "));
        assertThrows(RuntimeException.class, () -> RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("one"));
        assertThrows(RuntimeException.class,
            () -> RequestParameterPolicyEnforcementFilter.throwIfUnrecognizedParamName(Collections.enumeration(List.of("unknown"))));
        assertThrows(RuntimeException.class,
            () -> RequestParameterPolicyEnforcementFilter.checkOnlyPostParameters("get", Map.of("k", "v"), Set.of("k")));
    }

    @Test
    void verifyUnrecognizedInitParamFailsFilterInit() {
        val filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter("unrecognizedInitParameterName", "whatever");

        val filter = new RequestParameterPolicyEnforcementFilter();
        assertThrows(RuntimeException.class, () -> filter.init(filterConfig));
        filter.destroy();
    }

    @Test
    void verifyNoOpConfigurationFailsFilterInit() {
        val filter = new RequestParameterPolicyEnforcementFilter();
        val filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS, "true");
        filterConfig.addInitParameter(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID, "none");
        assertThrows(RuntimeException.class, () -> filter.init(filterConfig));
    }
    

    @Test
    void verifyRejectsMultiValuedRequestParameter() {
        val filter = new RequestParameterPolicyEnforcementFilter();
        val filterConfig = new MockFilterConfig();

        try {
            filter.init(filterConfig);
        } catch (final Exception e) {
            fail("Should not have failed filter init.");
        }

        val requestParameterMap = new HashMap<String, String[]>();
        requestParameterMap.put("someName", new String[]{"someValue", "someOtherValue"});

        val request = new MockHttpServletRequest();
        request.addParameters(requestParameterMap);

        val response = new MockHttpServletResponse();
        val chain = new MockFilterChain();
        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, chain));
    }

    @Test
    void verifyAcceptsMultiValuedRequestParameter() throws Exception {
        val filter = new RequestParameterPolicyEnforcementFilter();
        val filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS, "true");
        try {
            filter.init(filterConfig);
        } catch (final Exception e) {
            fail("Should not have failed filter init.");
        }

        val requestParameterMap = new HashMap<String, String[]>();
        requestParameterMap.put("someName", new String[]{"someValue", "someOtherValue"});

        val request = new MockHttpServletRequest();
        request.addParameters(requestParameterMap);

        val response = new MockHttpServletResponse();
        val chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
    }

    @Test
    void verifyRejectsRequestWithIllicitCharacterInCheckedParameter() {
        val filter = new RequestParameterPolicyEnforcementFilter();
        val filterConfig = new MockFilterConfig();
        
        try {
            filter.init(filterConfig);
        } catch (final Exception e) {
            fail("Should not have failed filter init.");
        }

        val requestParameterMap = new HashMap<String, String[]>();
        requestParameterMap.put("someName", new String[]{"someValue%40gmail.com"});

        val request = new MockHttpServletRequest();
        request.addParameters(requestParameterMap);

        val response = new MockHttpServletResponse();
        val chain = new MockFilterChain();
        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, chain));
    }

    @Test
    void verifyAllowsUncheckedParametersToHaveIllicitCharacters() throws Exception {
        val filter = new RequestParameterPolicyEnforcementFilter();
        val filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK, "ticket");
        
        try {
            filter.init(filterConfig);
        } catch (final Exception e) {
            fail("Should not have failed filter init.");
        }


        val requestParameterMap = new HashMap<String, String[]>();
        requestParameterMap.put("uncheckedName", new String[]{"someValue%40gmail.com"});
        val request = new MockHttpServletRequest();
        request.addParameters(requestParameterMap);

        val response = new MockHttpServletResponse();
        val chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
    }

    @Test
    void verifyAcceptsExpectedParameterNames() {

        val parameterNames = new HashSet<String>();
        parameterNames.add(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID);
        parameterNames.add(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK);
        parameterNames.add(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS);
        val parameterNamesEnumeration = Collections.enumeration(parameterNames);

        RequestParameterPolicyEnforcementFilter.throwIfUnrecognizedParamName(parameterNamesEnumeration);
    }

    @Test
    void verifyRejectsUnExpectedParameterName() {

        val parameterNames = new HashSet<String>();
        parameterNames.add(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID);
        parameterNames.add(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK);
        parameterNames.add(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS);
        parameterNames.add("unexpectedParameterName");
        val parameterNamesEnumeration = Collections.enumeration(parameterNames);

        assertThrows(RuntimeException.class, () -> RequestParameterPolicyEnforcementFilter.throwIfUnrecognizedParamName(parameterNamesEnumeration));
    }

    @Test
    void verifyParsesNullToEmptySet() {
        val returnedSet = RequestParameterPolicyEnforcementFilter.parseParametersList(null, true);
        assertTrue(returnedSet.isEmpty());
    }

    @Test
    void verifyParsesCommaDelimitedStringToSet() {
        val parameterValue = "service,renew, gateway";
        val expectedSet = new HashSet<String>();
        expectedSet.add("service");
        expectedSet.add("renew");
        expectedSet.add("gateway");

        val returnedSet = RequestParameterPolicyEnforcementFilter.parseParametersList(parameterValue, true);

        assertEquals(expectedSet, returnedSet);
    }

    @Test
    void verifyParsingBlankParametersToCheckThrowsException() {
        assertThrows(Exception.class, () -> RequestParameterPolicyEnforcementFilter.parseParametersList("   ", true));
    }

    @Test
    void verifyAsteriskParsesIfAllowedToEmptySetOfParametersToCheck() {
        val expectedSet = new HashSet<String>();
        val returnedSet = RequestParameterPolicyEnforcementFilter.parseParametersList("*", true);
        assertEquals(expectedSet, returnedSet);
    }

    @Test
    void verifyAsteriskParseIfNotAllowedThrowsException() {
        assertThrows(Exception.class, () -> RequestParameterPolicyEnforcementFilter.parseParametersList("*", false));
    }

    @Test
    void verifyParsingAsteriskWithOtherTokensThrowsException() {
        assertThrows(Exception.class, () -> RequestParameterPolicyEnforcementFilter.parseParametersList("renew * gateway", true));
    }

    @Test
    void verifyParsingNullYieldsPercentHashAmpersandAndQuestionMark() {
        val expected = new HashSet<Character>();
        expected.add('%');
        expected.add('#');
        expected.add('&');
        expected.add('?');
        val actual = RequestParameterPolicyEnforcementFilter.parseCharactersToForbid(null);
        assertEquals(expected, actual);
    }

    @Test
    void verifyParsingBlankThrowsException() {
        assertThrows(Exception.class, () -> RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("   "));
    }

    @Test
    void verifyParsesLiteralNoneToEmptySet() {
        val expected = new HashSet<Character>();
        val actual = RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("none");
        assertEquals(expected, actual);
    }

    @Test
    void verifyParsingSomeCharactersWorks() {
        val expected = new HashSet<Character>();
        expected.add('&');
        expected.add('%');
        expected.add('*');
        expected.add('#');
        expected.add('@');
        expected.add('\u0000');
        expected.add('\u0010');
        expected.add('\u0001');

        val actual = RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("& % * # @ \u0000 \u0010 \u0001");
        assertEquals(expected, actual);
    }

    @Test
    void verifyParsingMulticharacterTokensThrows() {
        assertThrows(Exception.class, () -> RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("& %*# @"));
    }

    @Test
    void verifyRequireNotMultiValueBlocksMultiValue() {
        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("dogName");

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("dogName", new String[]{"Johan", "Cubby"});

        assertThrows(RuntimeException.class, () -> RequestParameterPolicyEnforcementFilter.requireNotMultiValued(parametersToCheck, parameterMap));
    }

    @Test
    void verifyRequireNotMultiValuedAllowsSingleValued() {
        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("dogName");

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("dogName", new String[]{"Abbie"});
        RequestParameterPolicyEnforcementFilter.requireNotMultiValued(parametersToCheck, parameterMap);
    }

    @Test
    void verifyRequireNotMultiValuedIgnoresMissingParameter() {
        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("dogName");

        val parameterMap = new HashMap<String, String[]>();
        RequestParameterPolicyEnforcementFilter.requireNotMultiValued(parametersToCheck, parameterMap);
    }

    @Test
    void verifyRequireNotMultiValueAllowsUncheckedMultiValue() {
        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("dogName");

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("catName", new String[]{"Reggie", "Shenanigans"});

        RequestParameterPolicyEnforcementFilter.requireNotMultiValued(parametersToCheck, parameterMap);
    }

    @Test
    void verifyAllowsParametersNotContainingForbiddenCharacters() {
        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("catName");
        parametersToCheck.add("dogName");

        val charactersToForbid = new HashSet<Character>();
        charactersToForbid.add('&');
        charactersToForbid.add('%');

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("catName", new String[]{"Reggie", "Shenanigans"});
        parameterMap.put("dogName", new String[]{"Brutus", "Johan", "Cubby", "Abbie"});
        parameterMap.put("plantName", new String[]{"Rex"});

        RequestParameterPolicyEnforcementFilter.enforceParameterContentCharacterRestrictions(parametersToCheck,
            charactersToForbid, parameterMap);
    }

    @Test
    void verifyThrowsOnParameterContainingForbiddenCharacter() {

        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("catName");
        parametersToCheck.add("plantName");

        val charactersToForbid = new HashSet<Character>();
        charactersToForbid.add('&');
        charactersToForbid.add('%');

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("catName", new String[]{"Reggie", "Shenanigans"});
        parameterMap.put("dogName", new String[]{"Brutus", "Johan", "Cubby", "Abbie"});

        parameterMap.put("plantName", new String[]{"Rex&p0wned=true"});

        assertThrows(Exception.class, () -> RequestParameterPolicyEnforcementFilter.enforceParameterContentCharacterRestrictions(parametersToCheck,
            charactersToForbid, parameterMap));

    }

    @Test
    void verifyThrowsOnMultipleParameterContainingForbiddenCharacter() {

        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("catName");
        parametersToCheck.add("dogName");

        val charactersToForbid = new HashSet<Character>();
        charactersToForbid.add('!');
        charactersToForbid.add('$');

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("catName", new String[]{"Reggie", "Shenanigans"});

        parameterMap.put("dogName", new String[]{"Brutus", "Johan", "Cub!by", "Abbie"});
        parameterMap.put("plantName", new String[]{"Rex"});

        assertThrows(Exception.class,
            () -> RequestParameterPolicyEnforcementFilter.enforceParameterContentCharacterRestrictions(parametersToCheck,
                charactersToForbid, parameterMap));

    }

    @Test
    void verifyAllowsUncheckedParameterContainingForbiddenCharacter() {

        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("catName");
        parametersToCheck.add("dogName");

        val charactersToForbid = new HashSet<Character>();
        charactersToForbid.add('&');
        charactersToForbid.add('$');

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("catName", new String[]{"Reggie", "Shenanigans"});
        parameterMap.put("dogName", new String[]{"Brutus", "Johan", "Cubby", "Abbie"});


        parameterMap.put("plantName", new String[]{"Rex&ownage=true"});

        RequestParameterPolicyEnforcementFilter.enforceParameterContentCharacterRestrictions(parametersToCheck,
            charactersToForbid, parameterMap);

    }

    @Test
    void verifyAllowsCheckedParameterNotPresent() {

        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("catName");
        parametersToCheck.add("dogName");

        val charactersToForbid = new HashSet<Character>();
        charactersToForbid.add('&');
        charactersToForbid.add('$');

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("dogName", new String[]{"Brutus", "Johan", "Cubby", "Abbie"});

        RequestParameterPolicyEnforcementFilter.enforceParameterContentCharacterRestrictions(parametersToCheck,
            charactersToForbid, parameterMap);

    }

    @Test
    void verifyOnlyPostParameterInPostRequest() {
        internalTestOnlyPostParameter("POST");
    }

    @Test
    void verifyOnlyPostParameterInGetRequest() {
        assertThrows(Exception.class, () -> internalTestOnlyPostParameter("GET"));
    }

    @Test
    void verifyBlocksRequestByPattern() throws Throwable {
        val filter = new RequestParameterPolicyEnforcementFilter();
        val filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(RequestParameterPolicyEnforcementFilter.PATTERN_TO_BLOCK, ".+example\\.(com|org).*#fragment$");
        filter.init(filterConfig);

        val requestParameterMap = new HashMap<String, String[]>();
        requestParameterMap.put("someName", new String[]{"someValue"});

        val request = new MockHttpServletRequest();
        request.setRequestURI("https://www.example.biz?hello=world#fragment");
        request.addParameters(requestParameterMap);
        val response = new MockHttpServletResponse();
        val chain = new MockFilterChain();
        filter.doFilter(request, response, chain);

        request.setRequestURI("https://www.example.org?hello=world#fragment");
        request.addParameters(requestParameterMap);
        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, chain));
    }
}
