package org.apereo.cas.util.spring;

import lombok.Getter;
import lombok.val;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.Operation;
import org.springframework.boot.actuate.endpoint.annotation.AbstractDiscoveredEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.DiscoveredOperationMethod;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvoker;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotations;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link RestActuatorEndpointDiscoverer}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class RestActuatorEndpointDiscoverer extends EndpointDiscoverer<RestActuatorControllerEndpoint, Operation> {
    private final List<PathMapper> endpointPathMappers;

    public RestActuatorEndpointDiscoverer(final ApplicationContext applicationContext, final List<PathMapper> endpointPathMappers,
                                          final Collection<EndpointFilter<RestActuatorControllerEndpoint>> filters) {
        super(applicationContext, ParameterValueMapper.NONE, List.of(), filters, List.of());
        this.endpointPathMappers = List.copyOf(endpointPathMappers);
    }

    @Override
    protected boolean isEndpointTypeExposed(final Class<?> beanType) {
        val annotations = MergedAnnotations.from(beanType, MergedAnnotations.SearchStrategy.SUPERCLASS);
        return annotations.isPresent(RestActuatorEndpoint.class) && annotations.isPresent(Endpoint.class);
    }


    @Override
    protected RestActuatorControllerEndpoint createEndpoint(final Object endpointBean, final EndpointId id,
                                                            final Access defaultAccess, final Collection<Operation> operations) {

        val rootPath = PathMapper.getRootPath(this.endpointPathMappers, id);
        return new DiscoveredRestActuatorEndpoint(this, endpointBean, id, rootPath, defaultAccess);
    }

    @Override
    protected Operation createOperation(final EndpointId endpointId, final DiscoveredOperationMethod operationMethod, final OperationInvoker invoker) {
        throw new IllegalStateException("RestActuatorEndpoint %s must not declare operations".formatted(endpointId.toString()));
    }

    @Override
    protected EndpointDiscoverer.OperationKey createOperationKey(final Operation operation) {
        throw new IllegalStateException("RestActuatorEndpoint must not declare operation: %s".formatted(operation.toString()));
    }

    @Override
    protected boolean isInvocable(final RestActuatorControllerEndpoint endpoint) {
        val annotation = AnnotationUtils.findAnnotation(endpoint.getEndpointBean().getClass(), RestActuatorEndpoint.class);
        return annotation != null;
    }

    @Getter
    private static class DiscoveredRestActuatorEndpoint extends AbstractDiscoveredEndpoint<Operation> implements RestActuatorControllerEndpoint {
        private final String rootPath;

        DiscoveredRestActuatorEndpoint(final EndpointDiscoverer<?, ?> discoverer, final Object endpointBean,
                                       final EndpointId id, final String rootPath, final Access defaultAccess) {
            super(discoverer, endpointBean, id, defaultAccess, List.of());
            this.rootPath = rootPath;
        }
    }
}

