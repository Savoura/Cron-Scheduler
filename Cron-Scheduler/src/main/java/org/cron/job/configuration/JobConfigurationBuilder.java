package org.cron.job.configuration;

import java.util.concurrent.TimeUnit;

public class JobConfigurationBuilder {
    private String jobId;
    private Long frequencyUnitCount;
    private TimeUnit frequencyMeasurementUnit;
    private Long timeOutUnitCount;
    private TimeUnit timeOutMeasurementUnit;

    public JobConfigurationBuilder setJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    public JobConfigurationBuilder setFrequency(Long frequencyUnitCount, TimeUnit frequencyMeasurementUnit) {
        this.frequencyUnitCount = frequencyUnitCount;
        this.frequencyMeasurementUnit = frequencyMeasurementUnit;
        return this;
    }

    public JobConfigurationBuilder setTimeOut(Long timeOutUnitCount, TimeUnit timeOutMeasurementUnit) {
        this.timeOutUnitCount = timeOutUnitCount;
        this.timeOutMeasurementUnit = timeOutMeasurementUnit;
        return this;
    }

    public JobConfiguration build() {
        if (jobId == null) {
            throw new IllegalStateException("Missing required parameter jobId");
        }
        if (frequencyUnitCount == null) {
            throw new IllegalStateException("Missing required parameter frequencyUnitCount");
        }
        if (frequencyMeasurementUnit == null) {
            throw new IllegalStateException("Missing required parameter frequencyMeasurementUnit");
        }
        if (timeOutUnitCount == null) {
            throw new IllegalStateException("Missing required parameter timeOutUnitCount");
        }
        if (timeOutMeasurementUnit == null) {
            throw new IllegalStateException("Missing required parameter timeOutMeasurementUnit");
        }

        return new JobConfiguration(jobId, frequencyUnitCount, frequencyMeasurementUnit, timeOutUnitCount, timeOutMeasurementUnit);
    }

}
