package com.alerts.decorators;

import com.alerts.IAlert;

/**
 * Base class for the <b>Decorator</b> design pattern applied to alerts.
 *
 * <p>Holds a wrapped {@link IAlert} and forwards all interface methods to it
 * by default. Concrete decorators override only the methods whose behaviour
 * they want to extend or modify. Decorators implement {@link IAlert}
 * themselves, so they can be stacked: a {@code PriorityAlertDecorator} can
 * wrap a {@code RepeatedAlertDecorator} can wrap a plain {@code Alert}.
 *
 * @author 6439058
 */
public abstract class AlertDecorator implements IAlert {

    /** The alert this decorator augments. Never null. */
    protected final IAlert wrapped;

    protected AlertDecorator(IAlert wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("Decorated alert must not be null");
        }
        this.wrapped = wrapped;
    }

    @Override
    public String getPatientId() {
        return wrapped.getPatientId();
    }

    @Override
    public String getCondition() {
        return wrapped.getCondition();
    }

    @Override
    public long getTimestamp() {
        return wrapped.getTimestamp();
    }

    @Override
    public String getPriority() {
        return wrapped.getPriority();
    }
}
