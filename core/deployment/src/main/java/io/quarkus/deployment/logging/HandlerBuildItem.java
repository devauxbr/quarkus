package io.quarkus.deployment.logging;

import java.util.Optional;
import java.util.logging.Handler;

import org.jboss.builder.item.MultiBuildItem;

import io.quarkus.runtime.RuntimeValue;

/**
 * A build item for a log {@linkplain Handler} runtime value created for the deployment.
 */
public final class HandlerBuildItem extends MultiBuildItem {
    private final RuntimeValue<Optional<Handler>> handlerRuntimeValue;

    public HandlerBuildItem(RuntimeValue<Optional<Handler>> handlerRuntimeValue) {
        this.handlerRuntimeValue = handlerRuntimeValue;
    }

    public RuntimeValue<Optional<Handler>> getHandlerRuntimeValue() {
        return handlerRuntimeValue;
    }
}
