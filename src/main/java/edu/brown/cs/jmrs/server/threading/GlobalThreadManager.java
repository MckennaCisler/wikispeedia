package edu.brown.cs.jmrs.server.threading;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GlobalThreadManager {

  private static ExecutorService threadManager = Executors
      .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  public static Future<?> submit(Callable<?> task) {
    return threadManager.submit(task);
  }

  public static Future<?> submit(Runnable task) {
    return threadManager.submit(task);
  }
}
