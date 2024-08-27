package org.cron.job.scheduler;

import org.cron.job.definition.IJobDefinition;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class ScheduleExecutor {
    // Map to store the jobs
    private final Map<String, IJobDefinition> jobs = new ConcurrentHashMap<>();
    // Executor to manage executing the jobs
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile Boolean isRunning = false;
    // Priority Queue to store the jobs based on the next execution time
    private final PriorityQueue<FutureJobInstance> jobQueue = new PriorityQueue<>(Comparator.comparingLong(FutureJobInstance::nextExecutionTime));
    // Mutex to synchronize the job scheduling and execution
    private final Object mutex = new Object();
    // Executor to manage job management operations
    private final ExecutorService jobManagerExecutor = Executors.newSingleThreadExecutor();
    // Logger to log the errors and info
    private final Logger logger = Logger.getLogger(ScheduleExecutor.class.getName());

    public Future<Boolean> scheduleJob(IJobDefinition job) {
        return jobManagerExecutor.submit(() -> {
            String jobId = job.getJobConfiguration().jobId();
            // Check if the job is already scheduled
            if (jobs.containsKey(jobId)) {
                return false;
            }
            // Add the job to the job map and queue
            jobs.put(jobId, job);
            jobQueue.offer(new FutureJobInstance(jobId, job.getJobConfiguration().calculateNextExecutionTimeInMillis()));
            logger.log(Level.INFO, String.format("Job: %s has been scheduled.", jobId));
            // Notify the scheduler to re-check the queue
            synchronized (mutex) {
                mutex.notify();
            }
            return true;
        });
    }

    synchronized public void start() {
        // Check if the scheduler is already running
        if (isRunning) {
            throw new IllegalStateException("Scheduler has already been started.");
        } else {
            logger.log(Level.INFO, "Starting the scheduler.");
            isRunning = true;
            new Thread(this::runScheduler).start();
        }
    }

    synchronized public void stop() {
        // Check if the scheduler is already stopped
        if (!isRunning) {
            throw new IllegalStateException("Scheduler is not running.");
        } else {
            logger.log(Level.INFO, "Stopping the scheduler.");
            isRunning = false;
            // Notify any waiting thread to allow it to exit the loop in runScheduler
            synchronized (mutex) {
                mutex.notify();
            }
        }
        // Shutdown the executors outside the synchronized block
        executor.shutdownNow();
        jobManagerExecutor.shutdownNow();
    }

    private void runScheduler() {
        while (isRunning) {
            if (!jobQueue.isEmpty()) {
                FutureJobInstance scheduledJob = jobQueue.peek();
                long currentSystemTime = System.currentTimeMillis();
                long nextExecutionTimeForJob = scheduledJob.nextExecutionTime();
                long remainingTimeForJob = nextExecutionTimeForJob - currentSystemTime;
                // Check if the job is ready to be executed
                if (nextExecutionTimeForJob <= currentSystemTime) {
                    // Log the time difference between the scheduled time and the current time
                    logger.log(Level.INFO, String.format("Job: %s is ready to be executed. Time difference: %d", scheduledJob.jobId(), remainingTimeForJob));
                    // Execute the job
                    executor.submit(() -> executeJob(jobs.get(scheduledJob.jobId())));
                    // Remove the job from the queue
                    jobQueue.poll();
                    // Re-schedule the job with the next execution time
                    jobQueue.offer(new FutureJobInstance(scheduledJob.jobId(), jobs.get(scheduledJob.jobId()).getJobConfiguration().calculateNextExecutionTimeInMillis()));
                } else {
                    try {
                        // Wait for the next job to be executed
                        synchronized (mutex) {
                            mutex.wait(remainingTimeForJob);
                        }
                    } catch (InterruptedException e) {
                        logger.log(Level.SEVERE, String.format("Scheduler error found! \nError found : %s", e.getMessage()), e);
                    }
                }
            } else {
                try {
                    // No jobs to execute, wait for a new job to be scheduled
                    logger.log(Level.INFO, "No jobs to execute. Waiting for a new job to be scheduled.");
                    synchronized (mutex) {
                        mutex.wait();
                    }
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Scheduler error found! \nError found :", e);
                }
            }
        }
    }

    private void executeJob(IJobDefinition job) {
        String jobId = job.getJobConfiguration().jobId();
        Thread jobThread = new Thread(job::execute);
        jobThread.start();
        try {
            // Monitor the job execution with a timeout
            jobThread.join(job.getJobConfiguration().getTimeoutInMillis());

            if (jobThread.isAlive()) {
                // Set the interrupted flag to true
                jobThread.interrupt();
                logger.log(Level.SEVERE, String.format("Job forcefully removed due to timeout. Job ID: %s", jobId));
                // Remove the job from the scheduler
                removeJob(jobId);
                throw new TimeoutException(String.format("Job execution timeout. Job ID: %s", jobId));
            } else {
                logger.log(Level.INFO, String.format("Job: %s has been executed successfully.", jobId));
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, String.format("Job execution interrupted. Job ID: %s", jobId), e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public Future<Boolean> removeJob(String jobId) {
        return jobManagerExecutor.submit(() -> {
            // Check if the job is not scheduled
            if (!jobs.containsKey(jobId)) {
                return false;
            }
            // Remove the job from the map and queue
            jobs.remove(jobId);
            jobQueue.removeIf(entry -> entry.jobId().equals(jobId));
            logger.log(Level.INFO, String.format("Job: %s has been removed.", jobId));
            // Notify the scheduler to re-check the queue
            synchronized (mutex) {
                mutex.notify();
            }
            return true;
        });
    }
}
