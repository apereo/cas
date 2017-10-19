package org.apereo.cas.mgmt.services.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

/**
 * This controller is mapped to all allowed paths that the user can type into the Url bar of
 * the browser or hit the refresh button on so the app will stay in the requested state.
 *
 * @author Travis Schmidt
 * @since 5.2.0
 */
@Controller("forwarding")
public class ForwardingController {

    @RequestMapping({"services/{id:.*}","form/{id:.*}", "domains", "duplicate/{id:.*}"})
    public String forward() {
        return "manage.html";
    }
}
