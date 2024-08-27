package org.cron;

import org.cron.job.configuration.JobConfiguration;
import org.cron.job.definition.IJobDefinition;

import java.util.concurrent.atomic.AtomicInteger;

public class ExampleJob implements IJobDefinition {
    private final JobConfiguration jobConfiguration;
    private static final AtomicInteger executionCounter = new AtomicInteger(0); // Shared counter

    public ExampleJob(JobConfiguration jobConfiguration) {
        this.jobConfiguration = jobConfiguration;
    }

    @Override
    public JobConfiguration getJobConfiguration() {
        return jobConfiguration;
    }

    @Override
    public void execute() {
        System.out.println("ExampleJob : " + jobConfiguration.jobId() + ", started at time : " + System.currentTimeMillis());
        try {
            Thread.sleep(300); // Simulate job execution
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executionCounter.incrementAndGet(); // Increment the counter
        System.out.println("ExampleJob : " + jobConfiguration.jobId() + ", finished at time : " + System.currentTimeMillis());
    }

    public static int getExecutionCount() {
        return executionCounter.get();
    }

    public static void resetExecutionCount() {
        executionCounter.set(0);
    }
}
