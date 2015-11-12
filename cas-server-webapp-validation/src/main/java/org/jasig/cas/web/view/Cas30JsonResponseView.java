package org.jasig.cas.web.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

/**
 * Renders the model prepared by CAS in JSON format.
 * Automatically sets the response type and formats
 * the output for pretty printing. The class relies on
 * {@link MappingJackson2JsonView} to handle most of the
 * model processing and as such, does not do anything special.
 * It is meant and kept to provide a facility for adopters
 * so that the JSON view can be augmented easily in overlays.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("cas3ServiceJsonView")
public class Cas30JsonResponseView extends MappingJackson2JsonView {
    /**
     * Logger instance.
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Instantiates a new json response view.
     * Forces pretty printing of the JSON view.
     */
    public Cas30JsonResponseView() {
        super();
        setPrettyPrint(true);
        setDisableCaching(true);
        logger.debug("Rendering CAS JSON view");
    }
}
