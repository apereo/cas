package org.apereo.cas.web.report;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.expression.Expression;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowVariable;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link SpringWebflowReportController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SpringWebflowReportController extends BaseCasMvcEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringWebflowReportController.class);
    
    /**
     * Instantiates a new Base cas mvc endpoint.
     *
     * @param casProperties the cas properties
     */
    public SpringWebflowReportController(final CasConfigurationProperties casProperties) {
        super("swfReport", "/swf", casProperties.getMonitor().getEndpoints().getSpringWebflowReport(), casProperties);
    }

    /**
     * Get SWF report.
     *
     * @return JSON representing the current state of SWF.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<?, ?> getReport() {
        final Map<String, Object> jsonMap = new HashMap<>();
        final Map<String, FlowDefinitionRegistry> map =
                this.applicationContext.getBeansOfType(FlowDefinitionRegistry.class, false, true);

        map.forEach((k, v) -> Arrays.stream(v.getFlowDefinitionIds()).forEach(id -> {
            final Map<String, Object> flowDetails = new HashMap<>();
            final Flow def = Flow.class.cast(v.getFlowDefinition(id));

            final Map<String, Map> states = new HashMap<>();
            Arrays.stream(def.getStateIds()).forEach(st -> {

                final State state = (State) def.getState(st);
                final Map<String, Object> stateMap = new HashMap<>();

                if (!state.getAttributes().asMap().isEmpty()) {
                    stateMap.put("attributes", CollectionUtils.wrap(state.getAttributes()));
                }
                if (StringUtils.isNotBlank(state.getCaption())) {
                    stateMap.put("caption", state.getCaption());
                }

                List acts = StreamSupport.stream(state.getEntryActionList().spliterator(), false)
                        .map(Object::toString)
                        .collect(Collectors.toList());

                if (!acts.isEmpty()) {
                    stateMap.put("entryActions", acts);
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

                    final Field field = ReflectionUtils.findField(((ViewState) state).getViewFactory().getClass(), "viewId");
                    if (field != null) {
                        ReflectionUtils.makeAccessible(field);
                        final Expression exp = (Expression) ReflectionUtils.getField(field, ((ViewState) state).getViewFactory());
                        stateMap.put("viewId", StringUtils.defaultIfBlank(exp.getExpressionString(), exp.getValue(null).toString()));
                    } else {
                        LOGGER.warn("Could not detect field viewId for state [{}]", state);
                    }
                }

                if (state instanceof TransitionableState) {
                    final TransitionableState stDef = TransitionableState.class.cast(state);

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
            flowDetails.put("startState", def.getStartState().getId());
            flowDetails.put("possibleOutcomes", def.getPossibleOutcomes());
            flowDetails.put("stateCount", def.getStateCount());


            List acts = StreamSupport.stream(def.getEndActionList().spliterator(), false)
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

            final String vars = Arrays.stream(def.getVariables())
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
