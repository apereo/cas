package org.apereo.cas.web.flow;

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
    private FlowModel flowModel;

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

    public void setOnStartActions(final LinkedList<AbstractActionModel> vars) {
        this.flowModel.setOnStartActions(vars);
    }

    public void setStartStateId(final String id) {
        this.flowModel.setStartStateId(id);
    }

    public void setVars(final LinkedList<VarModel> vars) {
        this.flowModel.setVars(vars);
    }

    public void setGlobalTransitions(final LinkedList<TransitionModel> vars) {
        this.flowModel.setGlobalTransitions(vars);
    }

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
