package org.cron;

import org.cron.job.configuration.JobConfiguration;
import org.cron.job.configuration.JobConfigurationBuilder;
import org.cron.job.definition.IJobDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

public class JobTest {

    private static IJobDefinition job;

    @BeforeAll
    public static void setUp() {
        JobConfiguration job1Config = new JobConfigurationBuilder()
                .setJobId("job1")
                .setFrequency(1L, TimeUnit.SECONDS)
                .setTimeOut(1L, TimeUnit.SECONDS)
                .build();

        job = new ExampleJob(job1Config);
    }

    @BeforeEach
    public void resetExecutionCounter() {
        ExampleJob.resetExecutionCount(); // Reset the counter before each test
    }

    @Test
    public void testJobCreation() {
        assertNotNull(job);
        assertEquals("job1", job.getJobConfiguration().jobId());
        assertEquals(1, job.getJobConfiguration().frequencyUnitCount());
        assertEquals(1, job.getJobConfiguration().timeOutUnitCount());
        assertEquals(TimeUnit.SECONDS, job.getJobConfiguration().frequencyMeasurementUnit());
        assertEquals(TimeUnit.SECONDS, job.getJobConfiguration().timeOutMeasurementUnit());
    }

    @Test
    public void testFrequencyInMillis() {
        assertEquals(1000, job.getJobConfiguration().getFrequencyInMillis());
    }

    @Test
    public void testTimeOutInMillis() {
        assertEquals(1000, job.getJobConfiguration().getTimeoutInMillis());
    }

    @Test
    public void testExecuteTime() {
        long startTime = System.currentTimeMillis();


        // Simulating a job for timeout
        assertTimeout(ofMillis(job.getJobConfiguration().getTimeoutInMillis()), () -> job.execute());


        long endTime = System.currentTimeMillis();
        // Finding the time taken by the job
        long executionTime = endTime - startTime;

        // Check that execution should complete within the allowed time
        assertTrue(executionTime < job.getJobConfiguration().getTimeoutInMillis(), "Execution should complete within the allowed time");
    }

    @Test
    public void testExecute() {
        Thread thread = new Thread(job::execute);
        thread.start();
        try {
            // Monitor the job execution with a timeout
            thread.join(job.getJobConfiguration().getTimeoutInMillis());
            assertFalse(thread.isAlive(), "Job should have completed within the allowed time");
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    public void testExecutionCount() {
        job.execute();
        assertEquals(1, ExampleJob.getExecutionCount(), "Execution count should be 1");
    }

    @Test
    public void testNextExecutionTime() {
        long nextExecutionTime = job.getJobConfiguration().calculateNextExecutionTimeInMillis();
        long currentTime = System.currentTimeMillis();
        assertTrue(nextExecutionTime > currentTime, "Next execution time should be greater than the current time");
    }
}
