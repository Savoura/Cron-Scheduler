package org.cron.job.scheduler;

public record FutureJobInstance(String jobId, long nextExecutionTime) {
}
