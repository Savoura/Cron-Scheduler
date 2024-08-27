package org.cron.job.scheduler;

import org.cron.job.definition.IJobDefinition;

import java.util.concurrent.Future;

public final class CronScheduler {
    private final ScheduleExecutor executor = new ScheduleExecutor();

    public Future<Boolean> scheduleJob(IJobDefinition job) {
        return executor.scheduleJob(job);
    }

    public void start() {
        executor.start();
    }

    public void stop() {
        executor.stop();
    }

    public Future<Boolean> removeJob(String jobId) {
        return executor.removeJob(jobId);
    }
}
