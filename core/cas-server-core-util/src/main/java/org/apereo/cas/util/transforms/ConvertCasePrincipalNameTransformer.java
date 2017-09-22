package org.apereo.cas.util.transforms;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;

import javax.annotation.PostConstruct;


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
    public ConvertCasePrincipalNameTransformer(final PrincipalNameTransformer delegate) {
        this.delegateTransformer = delegate;
    }

    /**
     * Init the default delegate transformer.
     */
    @PostConstruct
    public void init() {
        if (this.delegateTransformer == null) {
            this.delegateTransformer = formUserId -> formUserId;
        }
    }

    @Override
    public String transform(final String formUserId) {
        final String result = this.delegateTransformer.transform(formUserId.trim()).trim();
        return this.toUpperCase ? result.toUpperCase(): result.toLowerCase();
    }

    public void setToUpperCase(final boolean toUpperCase) {
        this.toUpperCase = toUpperCase;
    }
}
