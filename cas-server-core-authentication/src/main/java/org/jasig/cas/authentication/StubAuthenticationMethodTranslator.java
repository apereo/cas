package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A stub translator that receives its legend as a map. The key for the map
 * should be the set of received authentication methods, and the value is a single
 * string to define the new authentication method name.
 * @author Misagh Moayyed
 * @since 4.3
 */
@Component("stubAuthenticationMethodTranslator")
public class StubAuthenticationMethodTranslator implements AuthenticationMethodTranslator {
    @Autowired(required=false)
    @Qualifier("stubAuthenticationMethodsTranslationMap")
    private final Map<Set<String>, String> translationMap;

    private boolean ignoreIfNoMatchIsFound = true;

    /**
     * Instantiates a new Stub authentication method translator.
     */
    public StubAuthenticationMethodTranslator() {
        this(Collections.EMPTY_MAP);
    }

    /**
     * Instantiates a new Sutb authentication method translator.
     *
     * @param translationMap the translation map
     */
    public StubAuthenticationMethodTranslator(final Map<Set<String>, String> translationMap) {
        this.translationMap = translationMap;
    }

    @Autowired
    public void setIgnoreIfNoMatchIsFound(@Value("${cas.mfa.stub.authn.method.translator.ignore.mismatch:false}")
                                              final boolean ignoreIfNoMatchIsFound) {
        this.ignoreIfNoMatchIsFound = ignoreIfNoMatchIsFound;
    }

    @Override
    public String translate(final WebApplicationService targetService, final String receivedAuthenticationMethod) {
        for (final Map.Entry<Set<String>, String> setStringEntry : this.translationMap.entrySet()) {
            if (setStringEntry.getKey().contains(receivedAuthenticationMethod)) {
                return this.translationMap.get(setStringEntry.getKey());
            }
        }

        if (this.ignoreIfNoMatchIsFound) {
            return receivedAuthenticationMethod;
        }
        throw new UnrecognizedAuthenticationMethodException(receivedAuthenticationMethod);
    }
}
