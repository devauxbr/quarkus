package io.quarkus.deployment.logging;

import org.jboss.builder.item.SimpleBuildItem;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.logging.HandlerContext;

/**
 * A build item for the log {@linkplain HandlerContext} runtime value created for the deployment.
 */
public final class HandlerContextBuildItem extends SimpleBuildItem {
    private final RuntimeValue<HandlerContext> handlersContext;

    public HandlerContextBuildItem(RuntimeValue<HandlerContext> handlersContext) {
        this.handlersContext = handlersContext;
    }

    public RuntimeValue<HandlerContext> getHandlersContext() {
        return handlersContext;
    }
}
