package com.alerts.decorators;

import com.alerts.IAlert;

/**
 * Tags an alert with an explicit priority level (HIGH, MEDIUM, LOW).
 *
 * <p>This decorator overrides {@link #getPriority()} to return the configured
 * level rather than the wrapped alert's default, and prepends a marker to
 * the condition string for easy log scanning.
 *
 * @author 6439058
 */
public class PriorityAlertDecorator extends AlertDecorator {

    private final String priority;

    /**
     * @param wrapped  the alert to decorate
     * @param priority {@code "HIGH"}, {@code "MEDIUM"}, or {@code "LOW"}
     */
    public PriorityAlertDecorator(IAlert wrapped, String priority) {
        super(wrapped);
        if (priority == null
                || !(priority.equals("HIGH") || priority.equals("MEDIUM") || priority.equals("LOW"))) {
            throw new IllegalArgumentException("Priority must be HIGH, MEDIUM, or LOW; got: " + priority);
        }
        this.priority = priority;
    }

    @Override
    public String getCondition() {
        return "[" + priority + "] " + wrapped.getCondition();
    }

    @Override
    public String getPriority() {
        return priority;
    }
}
