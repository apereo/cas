package org.apereo.cas.sqrl;

import com.github.dbadia.sqrl.server.SqrlConfig;
import com.github.dbadia.sqrl.server.SqrlServerOperations;
import com.github.dbadia.sqrl.server.util.SqrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SqrlCallbabckController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Controller("sqrlCallbabckController")
public class SqrlCallbabckController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqrlCallbabckController.class);

    private final SqrlConfig sqrlConfig;
    private final SqrlServerOperations sqrlServerOperations;

    public SqrlCallbabckController(final SqrlConfig sqrlConfig, final SqrlServerOperations sqrlServerOperations) {
        this.sqrlConfig = sqrlConfig;
        this.sqrlServerOperations = sqrlServerOperations;
    }

    /**
     * Do callback.
     *
     * @param request  the request
     * @param response the response
     * @throws Exception the exception
     */
    @PostMapping(value = "/sqrlCallback")
    public void doCallback(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(SqrlUtil.buildLogMessageForSqrlClientRequest(request));
        }
        this.sqrlServerOperations.handleSqrlClientRequest(request, response);
    }

}
