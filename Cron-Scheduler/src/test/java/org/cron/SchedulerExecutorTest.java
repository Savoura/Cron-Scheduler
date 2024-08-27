package org.cron;

import org.cron.job.configuration.JobConfiguration;
import org.cron.job.configuration.JobConfigurationBuilder;
import org.cron.job.definition.IJobDefinition;
import org.cron.job.scheduler.CronScheduler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


public class SchedulerExecutorTest {
    public static IJobDefinition job1, job2, job3, timedOutJob;

    @BeforeAll
    public static void setUp() {
        JobConfiguration job1Config = new JobConfigurationBuilder()
                .setJobId("job1")
                .setFrequency(1L, TimeUnit.SECONDS)
                .setTimeOut(1L, TimeUnit.SECONDS)
                .build();

        JobConfiguration job2Config = new JobConfigurationBuilder().setJobId("job2")
                .setFrequency(2L, TimeUnit.SECONDS)
                .setTimeOut(2L, TimeUnit.SECONDS)
                .build();

        JobConfiguration job3Config = new JobConfigurationBuilder().setJobId("job3")
                .setFrequency(1L, TimeUnit.SECONDS)
                .setTimeOut(1L, TimeUnit.SECONDS)
                .build();

        JobConfiguration timedOutJobConfig = new JobConfigurationBuilder().setJobId("timedOutJob")
                .setFrequency(1L, TimeUnit.SECONDS)
                .setTimeOut(100L, TimeUnit.MILLISECONDS)
                .build();

        job1 = new ExampleJob(job1Config);
        job2 = new ExampleJob(job2Config);
        job3 = new ExampleJob(job3Config);
        timedOutJob = new ExampleJob(timedOutJobConfig);
    }

    @BeforeEach
    public void resetExecutionCounter() {
        ExampleJob.resetExecutionCount(); // Reset the counter before each test
    }

    @Test
    public void testScheduleJob() throws Exception {
        CronScheduler cronScheduler = new CronScheduler();

        // Try scheduling a job
        Future<Boolean> result = cronScheduler.scheduleJob(job1);

        // Check that the job was scheduled successfully
        assertTrue(result.get(), "The job should be scheduled successfully.");

        // Try scheduling the same job again to see if it throws an exception
        result = cronScheduler.scheduleJob(job1);

        // Check that the job was not scheduled again
        assertFalse(result.get(), "Scheduling the same job twice should return false.");
    }

    @Test
    public void testStartScheduler() {
        CronScheduler cronScheduler = new CronScheduler();

        // Check if the scheduler can be started for the first time without throwing an exception
        assertDoesNotThrow(cronScheduler::start);

        // Check if the scheduler throws an exception when started a second time
        assertThrows(IllegalStateException.class, cronScheduler::start, "Starting the scheduler twice should throw an exception.");
    }

    @Test
    public void testRemoveJob() throws Exception {
        CronScheduler cronScheduler = new CronScheduler();

        // Try scheduling a job
        cronScheduler.scheduleJob(job1);

        // Try removing the job
        Future<Boolean> result = cronScheduler.removeJob(job1.getJobConfiguration().jobId());

        // Check that the job was removed successfully
        assertTrue(result.get(), "The job should be removed successfully.");

        // Try removing the same job again
        result = cronScheduler.removeJob(job1.getJobConfiguration().jobId());

        // Check that the job was not removed again
        assertFalse(result.get(), "Removing the same job twice should return false.");
    }

    @Test
    public void testStopScheduler() {
        CronScheduler cronScheduler = new CronScheduler();

        //  Check if the scheduler throws an exception when stopped without starting
        assertThrows(IllegalStateException.class, cronScheduler::stop, "Stopping the scheduler when not running should throw an exception.");

        cronScheduler.start();

        // Check if the scheduler can be stopped without throwing an exception
        assertDoesNotThrow(cronScheduler::stop);
    }

    @Test
    public void testJobExecution() throws Exception {
        CronScheduler cronScheduler = new CronScheduler();

        cronScheduler.scheduleJob(job1);
        cronScheduler.start();

        // Wait for the longJob to execute at least once (wait for the timeout at least)
        Thread.sleep(500);

        Future<Boolean> removeResult = cronScheduler.removeJob(job1.getJobConfiguration().jobId());
        // Check if the longJob was executed by attempting to remove it
        assertTrue(removeResult.get(), "Job should have been executed and re-scheduled.");

        cronScheduler.stop();
    }

    @Test
    public void testJobTimeoutExecution() throws Exception {
        CronScheduler cronScheduler = new CronScheduler();

        cronScheduler.scheduleJob(timedOutJob);
        cronScheduler.start();

        // Wait for the timedOutJob to execute at least once (wait for the timeout at least)
        Thread.sleep(500);

        Future<Boolean> removeResult = cronScheduler.removeJob(job1.getJobConfiguration().jobId());

        // Check if the longJob timed out and removed
        assertFalse(removeResult.get(), "Job should have been timed out and removed already.");

        cronScheduler.stop();
    }

    @Test
    public void testStartAndStopScheduler() {
        CronScheduler cronScheduler = new CronScheduler();

        // Check if the scheduler can be started for the first time without throwing an exception
        assertDoesNotThrow(cronScheduler::start);

        // Check if the scheduler throws an exception when started a second time
        assertThrows(IllegalStateException.class, cronScheduler::start, "Starting the scheduler twice should throw an exception.");

        // Check if the scheduler can be stopped without throwing an exception
        assertDoesNotThrow(cronScheduler::stop);

        // Check if the scheduler throws an exception when stopped without starting
        assertThrows(IllegalStateException.class, cronScheduler::stop, "Stopping the scheduler when not running should throw an exception.");
    }

    @Test
    public void testJobExecutionWithOneJob() throws Exception {
        CronScheduler cronScheduler = new CronScheduler();

        cronScheduler.scheduleJob(job1);
        cronScheduler.start();
        Thread.sleep(3500); // Wait for jobs to execute
        cronScheduler.stop();

        // Check that jobs were executed
        assertEquals(3, ExampleJob.getExecutionCount(), "Job did not execute the expected number of times.");
    }

    @Test
    public void testStartAndStopSchedulerMultipleTimes() {
        CronScheduler cronScheduler = new CronScheduler();

        // Check if the scheduler can be started for the first time without throwing an exception
        assertDoesNotThrow(cronScheduler::start);

        // Check if the scheduler throws an exception when started a second time
        assertThrows(IllegalStateException.class, cronScheduler::start, "Starting the scheduler twice should throw an exception.");

        // Check if the scheduler can be stopped without throwing an exception
        assertDoesNotThrow(cronScheduler::stop);

        // Check if the scheduler throws an exception when stopped without starting
        assertThrows(IllegalStateException.class, cronScheduler::stop, "Stopping the scheduler when not running should throw an exception.");

        // Check if the scheduler can be started again after stopping
        assertDoesNotThrow(cronScheduler::start);

        // Check if the scheduler can be stopped again after starting
        assertDoesNotThrow(cronScheduler::stop);
    }

    @Test
    public void testJobExecutionWithMultipleJobs() throws Exception {
        CronScheduler cronScheduler = new CronScheduler();

        cronScheduler.scheduleJob(job1);
        cronScheduler.scheduleJob(job2);

        cronScheduler.start();
        Thread.sleep(5500); // Wait for jobs to execute
        cronScheduler.stop();

        // Check that jobs were executed
        assertEquals(7, ExampleJob.getExecutionCount(), "Jobs did not execute the expected number of times.");
    }

    @Test
    public void testJobExecutionWithMultipleJobsAndTimeout() throws Exception {
        CronScheduler cronScheduler = new CronScheduler();

        cronScheduler.scheduleJob(job1);
        cronScheduler.scheduleJob(job2);
        cronScheduler.scheduleJob(timedOutJob);

        cronScheduler.start();
        Thread.sleep(5500); // Wait for jobs to execute

        // Check that jobs were executed, Job1 = 5 times, Job2 = 2 times, timedOutJob = 1 time (then forcefully removed for timing out)
        assertEquals(8, ExampleJob.getExecutionCount(), "Jobs did not execute the expected number of times.");

        // Check that timedOutJob was removed
        Future<Boolean> removeResult = cronScheduler.removeJob(timedOutJob.getJobConfiguration().jobId());
        assertFalse(removeResult.get(), "Job should have been removed already.");

        cronScheduler.stop();
    }

    @Test
    public void testMultipleJobExecutionWithRemoval() throws InterruptedException, ExecutionException {
        CronScheduler cronScheduler = new CronScheduler();

        cronScheduler.scheduleJob(job1);
        cronScheduler.scheduleJob(job2);
        cronScheduler.start();

        Thread.sleep(3000);

        // Remove Job2 after it has executed once in the 3000 ms
        Future<Boolean> removeResult = cronScheduler.removeJob(job2.getJobConfiguration().jobId());
        assertTrue(removeResult.get(), "Job should have been executed and re-scheduled.");

        // Wait for Job1 to execute 3 more times
        Thread.sleep(3500);

        cronScheduler.stop();

        // Check that jobs were executed 7 times Job1 = 6 times Job2 = 1 time;
        assertEquals(7, ExampleJob.getExecutionCount(), "Jobs did not execute the expected number of times.");

    }

    @Test
    public void testJobExecutionConcurrency() throws InterruptedException, ExecutionException {
        CronScheduler cronScheduler = new CronScheduler();

        // Job 1 and Job 3 have the same frequency, so they should both start after 1-second and sleep for 300ms in their execution
        cronScheduler.scheduleJob(job1);
        cronScheduler.scheduleJob(job3);
        cronScheduler.start();

        // Waiting for 1450 ms to check if both jobs executed at the same time or waited each other to finish
        Thread.sleep(1450);

        // the execution count should be 2 because both jobs should have executed at the same time
        assertEquals(2, ExampleJob.getExecutionCount(), "Jobs did not execute the expected number of times.");

    }
}
