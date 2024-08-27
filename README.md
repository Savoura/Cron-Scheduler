## Documentation
### Overview

The `CronScheduler` is a robust tool for scheduling and managing jobs, built on top of the `ScheduleExecutor` class. It offers a user-friendly interface to schedule, start, stop, and remove jobs. The `ScheduleExecutor` is the backbone of the system, responsible for managing the core functionalities such as maintaining a `Priority Queue` for job scheduling, executing jobs with defined timeouts, and handling concurrency. Additionally, it provides comprehensive logging of job scheduling and execution details, leveraging Java's built-in `Logger` for effective monitoring and debugging.

### System Design

![CronScheduler](https://github.com/user-attachments/assets/461c5d10-ed94-4ef7-a60a-c6ae5f178029)

### Technical Decisions 

1. Interface for Job Definition (`IJobDefinition`):
    The `IJobDefinition` interface establishes a standardized structure for job implementations. By enforcing a consistent contract, it ensures that all jobs conform to the required properties and methods, facilitating flexible and modular job integration.

2. Thread Management with Executors:
    - CachedThreadPool: Employed for managing concurrent job execution, the cached thread pool adapts dynamically to varying workloads, optimizing resource utilization.
    - SingleThreadExecutor: Used for job scheduling and removal operations, this executor ensures thread safety and maintains a consistent state across scheduling tasks.

3. Separation of System Logic and Job Definitions:
    The architecture separates system-related business logic from job definitions. This separation mitigates the risk of bugs in job definitions affecting the overall system stability, leading to a more robust and maintainable scheduling system.

### Trade-offs

- Single-Threaded Job Management:
    The use of a single-threaded executor for job management operations ensures thread safety but may become a bottleneck if the scheduler is scaled to handle a very large number of jobs.

- Offline vs Online Execution Time Calculation:
    In a system where job settings are stored in a database or another external system and can be updated dynamically, the decision to calculate execution times offline (with no changes to settings) minimizes CPU usage. However, if settings can be modified frequently, regular updates (pulls) may be needed to ensure the scheduler reflects the latest configuration.

- Priority Queue for Scheduling:
    Using a `PriorityQueue` to manage jobs based on their next execution times optimizes job scheduling by ensuring that the job closest to its next execution time is always processed first. This approach reduces unnecessary checks and enhances scheduling efficiency, although it may require additional complexity in maintaining the queue.


- Milliseconds granularity:
    To prevent jobs with the same frequency from competing for the same resources (e.g., all hourly jobs triggering simultaneously), the system assigns a starting time based on the job's registration timestamp. Assuming uniform registration times in an online system, this approach helps distribute job instances more evenly and promotes balanced resource utilization.

- Manual Timeout Handling:
    While manual timeout handling introduces additional complexity, it provides greater flexibility and control over job execution. This approach allows for precise timeout management but requires careful implementation to avoid introducing potential issues or bugs.

### Building & Running

##### Prerequisites
- Java Development Kit (JDK): Ensure that JDK is installed on your system.
- Apache Maven: Maven is needed for building and managing the project's dependencies.


##### Building
To build the project using Maven:
1.  Navigate to the project's root directory (where the pom.xml file is located).
2.  Run the following command:

``` mvn clean install ```

This command will 
- Clean up any previous builds
- Compile the source code
- Run the tests
- Package the application into a JAR file, which will be placed in the target directory.

##### Running
After building the project, you can run the compiled application using the following Maven command:

``` mvn exec:java -Dexec.mainClass="org.cron.Main" ```

This will execute the Main class, launching the application.

### Snippets

##### Defining a new Job
```java
public class ExampleJob implements IJobDefinition {
    private final JobConfiguration jobConfiguration;

    // Constructor to initialize the job with its configuration
    public ExampleJob(JobConfiguration jobConfiguration) {
        this.jobConfiguration = jobConfiguration;
    }

    // Return the job's configuration
    @Override
    public JobConfiguration getJobConfiguration() {
        return jobConfiguration;
    }

    // Execute the job
    @Override
    public void execute() {
        // Log the start time of the job
        System.out.println("ExampleJob: " + jobConfiguration.jobId() + ", started at: " + System.currentTimeMillis());
        try {
            // Simulate job execution with sleep
            Thread.sleep(300);
        } catch (InterruptedException e) {
            // Handle interruption
            Thread.currentThread().interrupt();
        }
        // Log the end time of the job
        System.out.println("ExampleJob: " + jobConfiguration.jobId() + ", finished at: " + System.currentTimeMillis());
    }
}

```
##### Scheduling a Job
``` java
public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Create an instance of CronScheduler
        CronScheduler scheduler = new CronScheduler();

        // Define a new job configuration with ID, frequency, and timeout
        JobConfiguration jobConfiguration = new JobConfigurationBuilder()
                .setJobId("job1") // Unique identifier for the job
                .setFrequency(1L, TimeUnit.SECONDS) // Frequency of job execution
                .setTimeOut(500L, TimeUnit.MILLISECONDS) // Timeout for job execution
                .build();

        // Create a new job with the defined configuration
        IJobDefinition job = new ExampleJob(jobConfiguration);

        // Schedule the job to be executed according to its configuration
        scheduler.scheduleJob(job);

        // Start the scheduler to begin executing scheduled jobs
        scheduler.start();

        // Sleep for 5 seconds to allow jobs to execute
        Thread.sleep(5000);

        // Stop the scheduler to halt job execution
        scheduler.stop();
    }
}
```
### Future Improvements

##### Architecture
- Persistence of Jobs Configuration: Implement a mechanism to persist job configurations using local or cloud-based storage solutions. This will ensure that job configurations are not lost upon system reboot, enabling the system to restore its state and resume job scheduling seamlessly.
- Separation into Server-Client Architecture: Divide the system into two distinct components:
  1. Server: Handles job execution and scheduling.
  2. Client: Provides an interface (e.g., CLI command) to submit and manage jobs on the server. This separation will enhance scalability and flexibility.
- Dynamic Loading of Jobs: Support dynamic or side-loading of jobs using the Java Reflection API. This will allow new job implementations to be loaded and scheduled without restarting the system, providing greater flexibility and extensibility.

##### Job Management
- Prioritization: Introduce a prioritization mechanism to manage job execution order based on configurable prioritization algorithms. This will ensure that critical jobs are executed promptly in high-demand scenarios.
- Failure Management: Implement configurable failure management strategies for jobs. Allow settings such as retry limits and retry intervals to handle job failures. For example, a job marked as stateless can have a maximum number of retries specified before giving up.
- Permissions: Enhance security by limiting permissions for specific jobs. This ensures that jobs only have access to the resources they need, reducing the risk of unauthorized access or modifications.

##### Monitoring & Observability
- Monitoring & Metrics: Integrate advanced monitoring and metrics collection to track system and job-level performance, reliability, and resource usage. This will help in assessing the health and efficiency of the system.
- Debugging Logs: Enrich the system with more detailed logging to facilitate debugging and issue resolution. Comprehensive logs will aid in identifying and troubleshooting problems more effectively.
- Cloud-Based Logging: Enable logging to a cloud provider to ensure that logs are accessible regardless of the host's status. This will improve the ability to review and analyze logs from different locations and ensure continuity in log access.