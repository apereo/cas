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
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.action.ExternalRedirectAction;
import org.springframework.webflow.action.SetAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowVariable;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.support.ActionExecutingViewFactory;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.AnnotatedAction;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
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

        map.forEach((k, value) -> Arrays.stream(value.getFlowDefinitionIds())
            .filter(currentId -> StringUtils.isBlank(flowId) || flowId.equalsIgnoreCase(currentId))
            .forEach(id -> {
                val flowDefinition = Flow.class.cast(value.getFlowDefinition(id));

                val flowDetails = new LinkedHashMap<String, Object>();
                flowDetails.put("startState", flowDefinition.getStartState().getId());

                val startActions = StreamSupport.stream(flowDefinition.getStartActionList().spliterator(), false)
                    .map(SpringWebflowEndpoint::convertActionToString)
                    .collect(Collectors.toList());
                if (!startActions.isEmpty()) {
                    flowDetails.put("startActions", startActions);
                }

                val states = new LinkedHashMap<String, Map>();
                Arrays.stream(flowDefinition.getStateIds()).forEach(st -> {

                    val state = (State) flowDefinition.getState(st);
                    val stateMap = new LinkedHashMap<String, Object>();

                    if (!state.getAttributes().asMap().isEmpty()) {
                        stateMap.put("attributes", CollectionUtils.wrap(state.getAttributes()));
                    }
                    if (StringUtils.isNotBlank(state.getCaption())) {
                        stateMap.put("caption", state.getCaption());
                    }

                    var acts = StreamSupport.stream(state.getEntryActionList().spliterator(), false)
                        .map(SpringWebflowEndpoint::convertActionToString)
                        .collect(Collectors.toList());

                    if (!acts.isEmpty()) {
                        stateMap.put("entryActions", acts);
                    }

                    if (state instanceof ActionState) {
                        acts = StreamSupport.stream(ActionState.class.cast(state).getActionList().spliterator(), false)
                            .map(SpringWebflowEndpoint::convertActionToString)
                            .collect(Collectors.toList());
                        if (!acts.isEmpty()) {
                            stateMap.put("actionList", acts);
                        }
                    }

                    if (state instanceof EndState) {
                        stateMap.put("isEndState", Boolean.TRUE);
                    }
                    if (state.isViewState()) {
                        val viewState = ViewState.class.cast(state);

                        stateMap.put("isViewState", state.isViewState());
                        stateMap.put("isRedirect", viewState.getRedirect());

                        acts = StreamSupport.stream(state.getEntryActionList().spliterator(), false)
                            .map(Object::toString)
                            .collect(Collectors.toList());

                        if (!acts.isEmpty()) {
                            stateMap.put("renderActions", viewState.getRenderActionList());
                        }

                        acts = Arrays.stream(viewState.getVariables())
                            .map(variable -> variable.getName() + " -> " + variable.getValueFactory().toString())
                            .collect(Collectors.toList());

                        if (!acts.isEmpty()) {
                            stateMap.put("viewVariables", acts);
                        }

                        val field = ReflectionUtils.findField(viewState.getViewFactory().getClass(), "viewId");
                        if (field != null) {
                            ReflectionUtils.makeAccessible(field);
                            val exp = (Expression) ReflectionUtils.getField(field, viewState.getViewFactory());
                            stateMap.put("viewId",
                                StringUtils.defaultIfBlank(Objects.requireNonNull(exp).getExpressionString(), exp.getValue(null).toString()));
                        } else if (viewState.getViewFactory() instanceof ActionExecutingViewFactory) {
                            val factory = (ActionExecutingViewFactory) viewState.getViewFactory();
                            if (factory.getAction() instanceof ExternalRedirectAction) {
                                val redirect = (ExternalRedirectAction) factory.getAction();
                                val uri = ReflectionUtils.findField(redirect.getClass(), "resourceUri");
                                ReflectionUtils.makeAccessible(Objects.requireNonNull(uri));
                                val exp = (Expression) ReflectionUtils.getField(uri, redirect);
                                stateMap.put("viewId",
                                    "externalRedirect -> #{" + Objects.requireNonNull(exp).getExpressionString() + '}');
                            } else {
                                stateMap.put("viewId", factory.getAction().toString());
                            }
                        } else {
                            LOGGER.info("Field viewId cannot be located on view state [{}]", state);
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
                flowDetails.put("possibleOutcomes", flowDefinition.getPossibleOutcomes());
                flowDetails.put("stateCount", flowDefinition.getStateCount());

                var acts = StreamSupport.stream(flowDefinition.getEndActionList().spliterator(), false)
                    .map(SpringWebflowEndpoint::convertActionToString)
                    .collect(Collectors.toList());
                if (!acts.isEmpty()) {
                    flowDetails.put("endActions", acts);
                }

                acts = StreamSupport.stream(flowDefinition.getGlobalTransitionSet().spliterator(), false)
                    .map(tr -> tr.getId() + " -> " + tr.getTargetStateId() + " @ " + tr.getExecutionCriteria().toString())
                    .collect(Collectors.toList());
                if (!acts.isEmpty()) {
                    flowDetails.put("globalTransitions", acts);
                }

                acts = Arrays.stream(flowDefinition.getExceptionHandlerSet().toArray())
                    .map(Object::toString)
                    .collect(Collectors.toList());
                if (!acts.isEmpty()) {
                    flowDetails.put("exceptionHandlers", acts);
                }

                val vars = Arrays.stream(flowDefinition.getVariables())
                    .map(FlowVariable::getName)
                    .collect(Collectors.joining(","));

                if (StringUtils.isNotBlank(vars)) {
                    flowDetails.put("variables", vars);
                }

                jsonMap.put(id, flowDetails);
            }));

        return jsonMap;
    }

    private static String convertActionToString(final Action action) {
        if (action instanceof EvaluateAction) {
            return convertEvaluateActionToString(action);
        }
        if (action instanceof AnnotatedAction) {
            val eval = AnnotatedAction.class.cast(action);
            if (eval.getTargetAction() instanceof EvaluateAction) {
                return convertEvaluateActionToString(eval.getTargetAction());
            }
            return eval.getTargetAction().toString();
        }
        if (action instanceof SetAction) {
            val expF = ReflectionUtils.findField(action.getClass(), "nameExpression");
            val resultExpF = ReflectionUtils.findField(action.getClass(), "valueExpression");
            return "set " + stringifyActionField(action, expF) + " = " + stringifyActionField(action, resultExpF);
        }
        return action.toString();
    }

    private static String convertEvaluateActionToString(final Action action) {
        val eval = EvaluateAction.class.cast(action);
        val expF = ReflectionUtils.findField(eval.getClass(), "expression");
        val resultExpF = ReflectionUtils.findField(eval.getClass(), "resultExpression");
        return stringifyActionField(action, expF, resultExpF);
    }

    private static String stringifyActionField(final Action eval, final Field... fields) {
        return Arrays.stream(fields)
            .map(f -> {
                ReflectionUtils.makeAccessible(f);
                val exp = ReflectionUtils.getField(f, eval);
                if (exp != null) {
                    return StringUtils.defaultString(exp.toString());
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.joining(", "));
    }
}
