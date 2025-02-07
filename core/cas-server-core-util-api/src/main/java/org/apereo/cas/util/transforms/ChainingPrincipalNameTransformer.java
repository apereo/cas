package org.apereo.cas.util.transforms;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

/**
 * A transformer that chains a number of inner transformers together.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChainingPrincipalNameTransformer implements PrincipalNameTransformer {

    private List<PrincipalNameTransformer> transformers = new ArrayList<>();

    @Override
    public String transform(final String formUserId) throws Throwable{
        var idToTransform = formUserId;
        for (val transformer : this.transformers) {
            idToTransform = transformer.transform(idToTransform);
        }
        return idToTransform;
    }

    /**
     * Add transformer.
     *
     * @param transformer the transformer
     * @return the chaining principal name transformer
     */
    @CanIgnoreReturnValue
    public ChainingPrincipalNameTransformer addTransformer(final PrincipalNameTransformer transformer) {
        return addTransformers(List.of(transformer));
    }

    /**
     * Add transformers chaining principal name transformer.
     *
     * @param input the input
     * @return the chaining principal name transformer
     */
    @CanIgnoreReturnValue
    public ChainingPrincipalNameTransformer addTransformers(final List<PrincipalNameTransformer> input) {
        transformers.addAll(input);
        return this;
    }
    
}
