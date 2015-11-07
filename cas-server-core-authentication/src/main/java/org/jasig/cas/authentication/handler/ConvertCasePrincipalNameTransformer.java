package org.jasig.cas.authentication.handler;

import javax.validation.constraints.NotNull;


/**
 * A transformer that converts the form uid to either lowercase or
 * uppercase. The result is also trimmed. The transformer is also able
 * to accept and work on the result of a previous transformer that might
 * have modified the uid, such that the two can be chained.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class ConvertCasePrincipalNameTransformer implements PrincipalNameTransformer {
    private boolean toUpperCase;
    
    @NotNull
    private final PrincipalNameTransformer delegateTransformer;
    
    /**
     * Instantiates a new transformer, while initializing the
     * inner delegate to {@link NoOpPrincipalNameTransformer}.
     */
    public ConvertCasePrincipalNameTransformer() {
        this.delegateTransformer = new NoOpPrincipalNameTransformer();
    }
    
    /**
     * Instantiates a new transformer, accepting an inner delegate.
     *
     * @param delegate the delegate
     */
    public ConvertCasePrincipalNameTransformer(final PrincipalNameTransformer delegate) {
        this.delegateTransformer = delegate;
    }
    
    
    @Override
    public String transform(final String formUserId) {
        final String result = this.delegateTransformer.transform(formUserId.trim()).trim();
        return this.toUpperCase ? result.toUpperCase(): result.toLowerCase();
    }

    public final void setToUpperCase(final boolean toUpperCase) {
        this.toUpperCase = toUpperCase;
    }

}
