package org.apereo.cas.util.transforms;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;

import java.util.ArrayList;
import java.util.List;


/**
 * A transformer that chains a number of inner transformers together.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ChainingPrincipalNameTransformer implements PrincipalNameTransformer {

    private static final long serialVersionUID = 7586914936775326709L;

    private List<PrincipalNameTransformer> transformers = new ArrayList<>();

    public List<PrincipalNameTransformer> getTransformers() {
        return transformers;
    }

    @Override
    public String transform(final String formUserId) {
        String idToTransform = formUserId;
        for (final PrincipalNameTransformer t : this.transformers) {
            idToTransform = t.transform(idToTransform);
        }
        return idToTransform;
    }

    /**
     * Add transformer.
     *
     * @param transformer the transformer
     */
    public void addTransformer(final PrincipalNameTransformer transformer) {
        this.transformers.add(transformer);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("transformers", this.transformers)
                .toString();
    }

}
