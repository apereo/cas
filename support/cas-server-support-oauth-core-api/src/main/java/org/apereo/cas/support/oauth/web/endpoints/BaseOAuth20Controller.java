package org.apereo.cas.support.oauth.web.endpoints;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

/**
 * This controller is the base controller for wrapping OAuth protocol in CAS.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Controller
@RequiredArgsConstructor
@Getter
public abstract class BaseOAuth20Controller {
    private final OAuth20ConfigurationContext oAuthConfigurationContext;
}
