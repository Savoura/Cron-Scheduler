package org.cron.job.definition;

import org.cron.job.configuration.JobConfiguration;

public interface IJobDefinition {

    JobConfiguration getJobConfiguration();

    void execute();

}
