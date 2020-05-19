package org.apereo.cas.web.support.filters;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link RequestParameterPolicyEnforcementFilter}.
 * <p>
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
@Tag("Simple")
public class RequestParameterPolicyEnforcementFilterTests {

    private static void internalTestOnlyPostParameter(final String method) {
        val onlyPostParameters = new HashSet<String>();
        onlyPostParameters.add("username");
        onlyPostParameters.add("password");

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("username", new String[]{"jle"});

        RequestParameterPolicyEnforcementFilter.checkOnlyPostParameters(method, parameterMap, onlyPostParameters);
    }

    @BeforeEach
    public void setup() {
        RequestParameterPolicyEnforcementFilter.setThrowOnErrors(true);
    }

    @Test
    public void verifyUnrecognizedInitParamFailsFilterInit() {

        val filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter("unrecognizedInitParameterName", "whatever");

        val filter = new RequestParameterPolicyEnforcementFilter();
        assertThrows(RuntimeException.class, () -> filter.init(filterConfig));
    }

    @Test
    public void verifyNoOpConfigurationFailsFilterInit() {
        val filter = new RequestParameterPolicyEnforcementFilter();

        val initParameterNames = new HashSet<String>();
        initParameterNames.add(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS);
        initParameterNames.add(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID);
        val parameterNamesEnumeration = Collections.enumeration(initParameterNames);
        val filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS))
            .thenReturn("true");
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID))
            .thenReturn("none");
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK))
            .thenReturn(null);

        assertThrows(RuntimeException.class, () -> filter.init(filterConfig));
    }

    @Test
    public void verifySettingFailSafeTrueFromInitParam() {

        val filter = new RequestParameterPolicyEnforcementFilter();

        val initParameterNames = new HashSet<String>();
        initParameterNames.add(RequestParameterPolicyEnforcementFilter.THROW_ON_ERROR);
        val parameterNamesEnumeration = Collections.enumeration(initParameterNames);

        val filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.THROW_ON_ERROR)).thenReturn("true");

        filter.init(filterConfig);
        assertTrue(AbstractSecurityFilter.isThrowOnErrors());
    }

    @Test
    public void verifySettingFailSafeFalseFromInitParam() {

        val filter = new RequestParameterPolicyEnforcementFilter();

        val initParameterNames = new HashSet<String>();
        initParameterNames.add(RequestParameterPolicyEnforcementFilter.THROW_ON_ERROR);
        val parameterNamesEnumeration = Collections.enumeration(initParameterNames);

        val filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.THROW_ON_ERROR))
            .thenReturn("false");

        filter.init(filterConfig);
        assertFalse(AbstractSecurityFilter.isThrowOnErrors());
    }

    @Test
    public void verifyRejectsMultiValuedRequestParameter() {

        val filter = new RequestParameterPolicyEnforcementFilter();

        val initParameterNames = new HashSet<String>();
        val parameterNamesEnumeration = Collections.enumeration(initParameterNames);
        val filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS))
            .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID))
            .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK))
            .thenReturn(null);


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
    public void verifyAcceptsMultiValuedRequestParameter() throws IOException, ServletException {
        val filter = new RequestParameterPolicyEnforcementFilter();

        val initParameterNames = new HashSet<String>();
        initParameterNames.add(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS);
        val parameterNamesEnumeration = Collections.enumeration(initParameterNames);
        val filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS))
            .thenReturn("true");
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID))
            .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK))
            .thenReturn(null);

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
    public void verifyRejectsRequestWithIllicitCharacterInCheckedParameter() {

        val filter = new RequestParameterPolicyEnforcementFilter();

        val initParameterNames = new HashSet<String>();
        val parameterNamesEnumeration = Collections.enumeration(initParameterNames);
        val filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS))
            .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID))
            .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK))
            .thenReturn(null);


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
    public void verifyAllowsUncheckedParametersToHaveIllicitCharacters() throws IOException, ServletException {
        val filter = new RequestParameterPolicyEnforcementFilter();

        val initParameterNames = new HashSet<String>();
        initParameterNames.add(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK);
        val parameterNamesEnumeration = Collections.enumeration(initParameterNames);
        val filterConfig = mock(FilterConfig.class);
        when(filterConfig.getInitParameterNames()).thenReturn(parameterNamesEnumeration);

        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS))
            .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID))
            .thenReturn(null);
        when(filterConfig.getInitParameter(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK))
            .thenReturn("ticket");


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
    public void verifyAcceptsExpectedParameterNames() {

        val parameterNames = new HashSet<String>();
        parameterNames.add(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID);
        parameterNames.add(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK);
        parameterNames.add(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS);
        val parameterNamesEnumeration = Collections.enumeration(parameterNames);

        RequestParameterPolicyEnforcementFilter.throwIfUnrecognizedParamName(parameterNamesEnumeration);
    }

    @Test
    public void verifyRejectsUnExpectedParameterName() {

        val parameterNames = new HashSet<String>();
        parameterNames.add(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID);
        parameterNames.add(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK);
        parameterNames.add(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS);
        parameterNames.add("unexpectedParameterName");
        val parameterNamesEnumeration = Collections.enumeration(parameterNames);

        assertThrows(RuntimeException.class, () -> RequestParameterPolicyEnforcementFilter.throwIfUnrecognizedParamName(parameterNamesEnumeration));
    }

    @Test
    public void verifyParsesNullToEmptySet() {
        val returnedSet = RequestParameterPolicyEnforcementFilter.parseParametersList(null, true);
        assertTrue(returnedSet.isEmpty());
    }

    @Test
    public void verifyParsesWhiteSpaceDelimitedStringToSet() {
        val parameterValue = "service renew gateway";
        val expectedSet = new HashSet<String>();
        expectedSet.add("service");
        expectedSet.add("renew");
        expectedSet.add("gateway");

        val returnedSet = RequestParameterPolicyEnforcementFilter.parseParametersList(parameterValue, true);

        assertEquals(expectedSet, returnedSet);
    }

    @Test
    public void verifyParsingBlankParametersToCheckThrowsException() {
        assertThrows(Exception.class, () -> RequestParameterPolicyEnforcementFilter.parseParametersList("   ", true));
    }

    @Test
    public void verifyAsteriskParsesIfAllowedToEmptySetOfParametersToCheck() {
        val expectedSet = new HashSet<String>();
        val returnedSet = RequestParameterPolicyEnforcementFilter.parseParametersList("*", true);
        assertEquals(expectedSet, returnedSet);
    }

    @Test
    public void verifyAsteriskParseIfNotAllowedThrowsException() {
        assertThrows(Exception.class, () -> RequestParameterPolicyEnforcementFilter.parseParametersList("*", false));
    }

    @Test
    public void verifyParsingAsteriskWithOtherTokensThrowsException() {
        assertThrows(Exception.class, () -> RequestParameterPolicyEnforcementFilter.parseParametersList("renew * gateway", true));
    }

    @Test
    public void verifyParsingNullYieldsPercentHashAmpersandAndQuestionMark() {
        val expected = new HashSet<Character>();
        expected.add('%');
        expected.add('#');
        expected.add('&');
        expected.add('?');
        val actual = RequestParameterPolicyEnforcementFilter.parseCharactersToForbid(null);
        assertEquals(expected, actual);
    }

    @Test
    public void verifyParsingBlankThrowsException() {
        assertThrows(Exception.class, () -> RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("   "));
    }

    @Test
    public void verifyParsesLiteralNoneToEmptySet() {
        val expected = new HashSet<Character>();
        val actual = RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("none");
        assertEquals(expected, actual);
    }

    @Test
    public void verifyParsingSomeCharactersWorks() {
        val expected = new HashSet<Character>();
        expected.add('&');
        expected.add('%');
        expected.add('*');
        expected.add('#');
        expected.add('@');

        val actual = RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("& % * # @");
        assertEquals(expected, actual);
    }

    @Test
    public void verifyParsingMulticharacterTokensThrows() {
        assertThrows(Exception.class, () -> RequestParameterPolicyEnforcementFilter.parseCharactersToForbid("& %*# @"));
    }

    @Test
    public void verifyRequireNotMultiValueBlocksMultiValue() {
        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("dogName");

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("dogName", new String[]{"Johan", "Cubby"});

        assertThrows(RuntimeException.class, () -> RequestParameterPolicyEnforcementFilter.requireNotMultiValued(parametersToCheck, parameterMap));
    }

    @Test
    public void verifyRequireNotMultiValuedAllowsSingleValued() {
        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("dogName");

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("dogName", new String[]{"Abbie"});
        RequestParameterPolicyEnforcementFilter.requireNotMultiValued(parametersToCheck, parameterMap);
    }

    @Test
    public void verifyRequireNotMultiValuedIgnoresMissingParameter() {
        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("dogName");

        val parameterMap = new HashMap<String, String[]>();
        RequestParameterPolicyEnforcementFilter.requireNotMultiValued(parametersToCheck, parameterMap);
    }

    @Test
    public void verifyRequireNotMultiValueAllowsUncheckedMultiValue() {
        val parametersToCheck = new HashSet<String>();
        parametersToCheck.add("dogName");

        val parameterMap = new HashMap<String, String[]>();
        parameterMap.put("catName", new String[]{"Reggie", "Shenanigans"});

        RequestParameterPolicyEnforcementFilter.requireNotMultiValued(parametersToCheck, parameterMap);
    }

    @Test
    public void verifyAllowsParametersNotContainingForbiddenCharacters() {
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
    public void verifyThrowsOnParameterContainingForbiddenCharacter() {

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
    public void verifyThrowsOnMultipleParameterContainingForbiddenCharacter() {

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
    public void verifyAllowsUncheckedParameterContainingForbiddenCharacter() {

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
    public void verifyAllowsCheckedParameterNotPresent() {

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
    public void verifyOnlyPostParameterInPostRequest() {
        internalTestOnlyPostParameter("POST");
    }

    @Test
    public void verifyOnlyPostParameterInGetRequest() {
        assertThrows(Exception.class, () -> internalTestOnlyPostParameter("GET"));
    }

    @Test
    public void verifyBlocksRequestByPattern() throws Exception {
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
