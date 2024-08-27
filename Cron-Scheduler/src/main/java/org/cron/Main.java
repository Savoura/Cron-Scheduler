package org.cron;

import org.cron.job.configuration.JobConfiguration;
import org.cron.job.configuration.JobConfigurationBuilder;
import org.cron.job.definition.IJobDefinition;
import org.cron.job.scheduler.CronScheduler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Creating an instance of CronScheduler
        CronScheduler scheduler = new CronScheduler();

        // Creating a new job configuration
        JobConfiguration jobConfiguration = new JobConfigurationBuilder()
                .setJobId("job1")
                .setFrequency(1L, TimeUnit.SECONDS)
                .setTimeOut(500L, TimeUnit.MILLISECONDS)
                .build();

        // Creating a new job with the defined configuration
        IJobDefinition job = new ExampleJob(jobConfiguration);

        // Add the created job to the scheduler
        scheduler.scheduleJob(job);

        // Start the scheduler to begin executing the scheduled jobs
        scheduler.start();

        // Sleep for 5 seconds to simulate job executions in that interval
        Thread.sleep(5000);

        // Stop the scheduler to quit the scheduled jobs execution
        scheduler.stop();
    }
}
