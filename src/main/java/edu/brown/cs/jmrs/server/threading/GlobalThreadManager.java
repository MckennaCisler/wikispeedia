package edu.brown.cs.jmrs.server.threading;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Manages all server action besides sending messages to clients.
 *
 * @author shastin1
 *
 */
public class GlobalThreadManager {

  private static ExecutorService threadManager = Executors
      .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  /**
   * Submits given task to the ExecutorService
   *
   * @param task
   *          The task to be submitted to the ExecutorService
   * @return The Future produced by the ExecutorService
   */
  public static Future<?> submit(Callable<?> task) {
    return threadManager.submit(task);
  }

  /**
   * Submits given task to the ExecutorService
   *
   * @param task
   *          The task to be submitted to the ExecutorService
   * @return The Future produced by the ExecutorService
   */
  public static Future<?> submit(Runnable task) {
    return threadManager.submit(task);
  }
}
