package org.jasig.cas.authentication.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * A transformer that converts the form uid to either lowercase or
 * uppercase. The result is also trimmed. The transformer is also able
 * to accept and work on the result of a previous transformer that might
 * have modified the uid, such that the two can be chained.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Component("convertCasePrincipalNameTransformer")
public class ConvertCasePrincipalNameTransformer implements PrincipalNameTransformer {
    private boolean toUpperCase;

    private PrincipalNameTransformer delegateTransformer;
    
    /**
     * Instantiates a new transformer.
     */
    public ConvertCasePrincipalNameTransformer() {}
    
    /**
     * Instantiates a new transformer, accepting an inner delegate.
     *
     * @param delegate the delegate
     */
    @Autowired(required = false)
    public ConvertCasePrincipalNameTransformer(@Qualifier("delegateTransformer")
                                                   final PrincipalNameTransformer delegate) {
        this.delegateTransformer = delegate;
    }

    /**
     * Init the default delegate transformer.
     */
    @PostConstruct
    public void init() {
        if (this.delegateTransformer == null) {
            this.delegateTransformer = new NoOpPrincipalNameTransformer();
        }
    }

    @Override
    public String transform(final String formUserId) {
        final String result = this.delegateTransformer.transform(formUserId.trim()).trim();
        return this.toUpperCase ? result.toUpperCase(): result.toLowerCase();
    }

    @Autowired
    public final void setToUpperCase(@Value("${cas.principal.transform.upperCase:false}") final boolean toUpperCase) {
        this.toUpperCase = toUpperCase;
    }

}
