package com.farast.utuclient.main.java.util;

import com.farast.utuapi.util.operations.Operation;
import com.farast.utuapi.util.operations.OperationListener;

/**
 * Created by cendr_000 on 28.07.2016.
 */
public class OperationListenerLogger implements OperationListener {
    private StatusLogger logger;

    public OperationListenerLogger(StatusLogger logger) {
        this.logger = logger;
    }

    @Override
    public void started(Operation operation) {
        logger.logInfo(StringUtil.capitalize(operation.getName()) + "...");
    }

    @Override
    public void ended(Operation operation) {
        logger.logInfo(StringUtil.capitalize(operation.getName()) + "... Done");
    }

    public void logInfo(String info, Operation operation) {
        logger.logInfo(operation.getName() + " :" + info);
    }

    public void logException(Exception exception, String explanation, String solution, boolean fatal, Operation operation) {
        String operationName = operation.getName();
        logger.logException(exception, explanation);
        logger.logInfo(StringUtil.capitalize(operationName) + "... Errored: " + explanation);
        logger.showExceptionDialog(explanation, "Errored while performing: " + operationName + "\n" + solution, exception, fatal);
    }
}
