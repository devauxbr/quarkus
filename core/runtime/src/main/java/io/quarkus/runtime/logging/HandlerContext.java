package io.quarkus.runtime.logging;

import java.util.List;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;

/**
 * A container that provides the required context for extensions to build new logging {@link Handler} instances
 */
public class HandlerContext {
    private List<LogCleanupFilterElement> filterElements;
    private ErrorManager errorManager;

    public HandlerContext(List<LogCleanupFilterElement> filterElements, ErrorManager errorManager) {
        this.filterElements = filterElements;
        this.errorManager = errorManager;
    }

    public List<LogCleanupFilterElement> getFilterElements() {
        return filterElements;
    }

    public ErrorManager getErrorManager() {
        return errorManager;
    }
}
