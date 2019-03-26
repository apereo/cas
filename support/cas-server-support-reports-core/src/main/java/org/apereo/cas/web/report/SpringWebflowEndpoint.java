package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.expression.Expression;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowVariable;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link SpringWebflowEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Endpoint(id = "springWebflow", enableByDefault = false)
public class SpringWebflowEndpoint extends BaseCasActuatorEndpoint {

    private final ApplicationContext applicationContext;

    /**
     * Instantiates a new Base cas mvc endpoint.
     *
     * @param casProperties      the cas properties
     * @param applicationContext the application context
     */
    public SpringWebflowEndpoint(final CasConfigurationProperties casProperties, final ApplicationContext applicationContext) {
        super(casProperties);
        this.applicationContext = applicationContext;
    }

    /**
     * Get SWF report.
     *
     * @param flowId the flow id
     * @return JSON representing the current state of SWF.
     */
    @ReadOperation
    public Map<?, ?> getReport(@Nullable final String flowId) {
        val jsonMap = new LinkedHashMap<String, Object>();
        val map = this.applicationContext.getBeansOfType(FlowDefinitionRegistry.class, false, true);

        map.forEach((k, v) -> Arrays.stream(v.getFlowDefinitionIds())
            .filter(currentId -> {
                if (StringUtils.isNotBlank(flowId)) {
                    return flowId.equalsIgnoreCase(currentId);
                }
                return true;
            })
            .forEach(id -> {
                val flowDetails = new LinkedHashMap<String, Object>();
                val def = Flow.class.cast(v.getFlowDefinition(id));
                flowDetails.put("startState", def.getStartState().getId());

                val states = new LinkedHashMap<String, Map>();
                Arrays.stream(def.getStateIds()).forEach(st -> {

                    val state = (State) def.getState(st);
                    val stateMap = new LinkedHashMap<String, Object>();

                    if (!state.getAttributes().asMap().isEmpty()) {
                        stateMap.put("attributes", CollectionUtils.wrap(state.getAttributes()));
                    }
                    if (StringUtils.isNotBlank(state.getCaption())) {
                        stateMap.put("caption", state.getCaption());
                    }

                    var acts = StreamSupport.stream(state.getEntryActionList().spliterator(), false)
                        .map(Object::toString)
                        .collect(Collectors.toList());

                    if (!acts.isEmpty()) {
                        stateMap.put("entryActions", acts);
                    }

                    if (state instanceof ActionState) {
                        acts = StreamSupport.stream(ActionState.class.cast(state).getActionList().spliterator(), false)
                            .map(Object::toString)
                            .collect(Collectors.toList());
                        if (!acts.isEmpty()) {
                            stateMap.put("actionList", acts);
                        }
                    }

                    if (state instanceof EndState) {
                        stateMap.put("isEndState", Boolean.TRUE);
                    }
                    if (state.isViewState()) {
                        stateMap.put("isViewState", state.isViewState());
                        stateMap.put("isRedirect", ((ViewState) state).getRedirect());

                        acts = StreamSupport.stream(state.getEntryActionList().spliterator(), false)
                            .map(Object::toString)
                            .collect(Collectors.toList());

                        if (!acts.isEmpty()) {
                            stateMap.put("renderActions", ((ViewState) state).getRenderActionList());
                        }

                        acts = Arrays.stream(((ViewState) state).getVariables())
                            .map(value -> value.getName() + " -> " + value.getValueFactory().toString())
                            .collect(Collectors.toList());

                        if (!acts.isEmpty()) {
                            stateMap.put("viewVariables", acts);
                        }

                        val field = ReflectionUtils.findField(((ViewState) state).getViewFactory().getClass(), "viewId");
                        if (field != null) {
                            ReflectionUtils.makeAccessible(field);
                            val exp = (Expression) ReflectionUtils.getField(field, ((ViewState) state).getViewFactory());
                            stateMap.put("viewId", StringUtils.defaultIfBlank(exp.getExpressionString(), exp.getValue(null).toString()));
                        } else {
                            LOGGER.warn("Field viewId cannot be located on view state [{}]", state);
                        }
                    }

                    if (state instanceof TransitionableState) {
                        val stDef = TransitionableState.class.cast(state);

                        acts = StreamSupport.stream(stDef.getExitActionList().spliterator(), false)
                            .map(Object::toString)
                            .collect(Collectors.toList());

                        if (!acts.isEmpty()) {
                            stateMap.put("exitActions", acts);
                        }

                        acts = Arrays.stream(stDef.getTransitions())
                            .map(tr -> tr.getId() + " -> " + tr.getTargetStateId())
                            .collect(Collectors.toList());

                        if (!acts.isEmpty()) {
                            stateMap.put("transitions", acts);
                        }
                    }
                    states.put(st, stateMap);
                });
                flowDetails.put("states", states);
                flowDetails.put("possibleOutcomes", def.getPossibleOutcomes());
                flowDetails.put("stateCount", def.getStateCount());

                var acts = StreamSupport.stream(def.getEndActionList().spliterator(), false)
                    .map(Object::toString)
                    .collect(Collectors.toList());
                if (!acts.isEmpty()) {
                    flowDetails.put("endActions", acts);
                }

                acts = StreamSupport.stream(def.getGlobalTransitionSet().spliterator(), false)
                    .map(tr -> tr.getId() + " -> " + tr.getTargetStateId() + " @ " + tr.getExecutionCriteria().toString())
                    .collect(Collectors.toList());
                if (!acts.isEmpty()) {
                    flowDetails.put("globalTransitions", acts);
                }

                acts = Arrays.stream(def.getExceptionHandlerSet().toArray())
                    .map(Object::toString)
                    .collect(Collectors.toList());
                if (!acts.isEmpty()) {
                    flowDetails.put("exceptionHandlers", acts);
                }

                val vars = Arrays.stream(def.getVariables())
                    .map(FlowVariable::getName)
                    .collect(Collectors.joining(","));

                if (StringUtils.isNotBlank(vars)) {
                    flowDetails.put("variables", vars);
                }

                jsonMap.put(id, flowDetails);
            }));

        return jsonMap;
    }
}
