package org.apereo.cas.web.flow.configurer;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.engine.model.AbstractActionModel;
import org.springframework.webflow.engine.model.AbstractStateModel;
import org.springframework.webflow.engine.model.FlowModel;
import org.springframework.webflow.engine.model.TransitionModel;
import org.springframework.webflow.engine.model.VarModel;
import org.springframework.webflow.engine.model.builder.FlowModelBuilder;
import org.springframework.webflow.engine.model.builder.FlowModelBuilderException;

import java.util.LinkedList;

/**
 * This is {@link DynamicFlowModelBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DynamicFlowModelBuilder implements FlowModelBuilder {
    /**
     * The Flow model.
     */
    private FlowModel flowModel;

    /**
     * Instantiates a new Dynamic flow model builder.
     */
    public DynamicFlowModelBuilder() {
        init();
    }

    @Override
    public void init() throws FlowModelBuilderException {
        if (this.flowModel == null) {
            this.flowModel = new FlowModel();
        }
    }

    @Override
    public void build() throws FlowModelBuilderException {
    }

    /**
     * Sets on start actions.
     *
     * @param vars the vars
     */
    public void setOnStartActions(final LinkedList<AbstractActionModel> vars) {
        this.flowModel.setOnStartActions(vars);
    }

    /**
     * Sets start state id.
     *
     * @param id the id
     */
    public void setStartStateId(final String id) {
        this.flowModel.setStartStateId(id);
    }

    /**
     * Sets vars.
     *
     * @param vars the vars
     */
    public void setVars(final LinkedList<VarModel> vars) {
        this.flowModel.setVars(vars);
    }

    /**
     * Sets global transitions.
     *
     * @param vars the vars
     */
    public void setGlobalTransitions(final LinkedList<TransitionModel> vars) {
        this.flowModel.setGlobalTransitions(vars);
    }

    /**
     * Sets states.
     *
     * @param states the states
     */
    public void setStates(final LinkedList<AbstractStateModel> states) {
        this.flowModel.setStates(states);
    }

    @Override
    public FlowModel getFlowModel() throws FlowModelBuilderException {
        return this.flowModel;
    }

    @Override
    public void dispose() throws FlowModelBuilderException {
        this.flowModel = null;
    }

    @Override
    public Resource getFlowModelResource() {
        return new ClassPathResource("src/main/resources");
    }

    @Override
    public boolean hasFlowModelResourceChanged() {
        return false;
    }
}
