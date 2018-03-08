package org.apereo.cas.web.view.json;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * This is {@link CasJsonServiceResponseAuthenticationSuccess}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@NoArgsConstructor
public class CasJsonServiceResponseAuthenticationSuccess {

    private String user;

    private String proxyGrantingTicket;

    private List proxies;

    private Map attributes;
}
