package io.quarkus.amazon.common.base;


public class RuntimeConfigurationError extends  RuntimeException {
    public RuntimeConfigurationError(String message) {
        super(message);
    }
}
