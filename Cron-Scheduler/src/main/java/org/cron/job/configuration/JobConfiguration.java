package org.cron.job.configuration;

import java.util.concurrent.TimeUnit;

public record JobConfiguration(String jobId, long frequencyUnitCount, TimeUnit frequencyMeasurementUnit,
                               long timeOutUnitCount, TimeUnit timeOutMeasurementUnit) {

    public long getFrequencyInMillis() {
        return frequencyMeasurementUnit.toMillis(frequencyUnitCount);
    }

    public long getTimeoutInMillis() {
        return timeOutMeasurementUnit.toMillis(timeOutUnitCount);
    }


    // Calculates the next execution time based on the current time and the job's frequency.
    public long calculateNextExecutionTimeInMillis() {
        return System.currentTimeMillis() + getFrequencyInMillis();
    }
}