package com.alerts.decorators;

import com.alerts.IAlert;

/**
 * Marks an alert as one that should be re-checked at a regular interval until
 * resolved.
 *
 * <p>This decorator augments the {@code condition} string with a
 * {@code "[REPEAT:every=Xs]"} marker so downstream consumers (logging,
 * notification routing) can spot and schedule the re-check. It does not
 * itself implement the scheduling — that's the routing layer's job.
 *
 * @author 6439058
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    private final long repeatIntervalSeconds;

    /**
     * @param wrapped                 the alert to decorate
     * @param repeatIntervalSeconds   how often (in seconds) the alert should
     *                                be re-checked
     */
    public RepeatedAlertDecorator(IAlert wrapped, long repeatIntervalSeconds) {
        super(wrapped);
        this.repeatIntervalSeconds = repeatIntervalSeconds;
    }

    /** @return interval between re-checks, in seconds */
    public long getRepeatIntervalSeconds() {
        return repeatIntervalSeconds;
    }

    @Override
    public String getCondition() {
        return wrapped.getCondition() + " [REPEAT:every=" + repeatIntervalSeconds + "s]";
    }
}
