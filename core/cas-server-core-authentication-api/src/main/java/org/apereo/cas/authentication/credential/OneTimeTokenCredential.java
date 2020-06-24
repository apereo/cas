package org.apereo.cas.authentication.credential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link OneTimeTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OneTimeTokenCredential extends AbstractCredential {

    private static final long serialVersionUID = -7570600701132111037L;

    private String token;

    @Override
    public String getId() {
        return this.token;
    }
}
