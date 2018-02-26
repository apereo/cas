package org.apereo.cas.web.view.json;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is {@link CasJsonServiceResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@NoArgsConstructor
public class CasJsonServiceResponse {

    private CasJsonServiceResponseAuthenticationFailure authenticationFailure;

    private CasJsonServiceResponseAuthenticationSuccess authenticationSuccess;
}
