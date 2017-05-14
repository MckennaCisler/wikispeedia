package edu.brown.cs.jmrs.server.threading;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClosableReadWriteLock extends ReentrantReadWriteLock
    implements AutoCloseable {

  private class LockTracker {
    private boolean write;

    public LockTracker(boolean write) {
      this.write = write;
    }

    public boolean isWrite() {
      return write;
    }
  }

  private ThreadLocal<Deque<LockTracker>> lockQueue;

  public ClosableReadWriteLock() {
    lockQueue = new ThreadLocal<Deque<LockTracker>>() {
      @Override
      protected Deque<LockTracker> initialValue() {
        return new ArrayDeque<>();
      }
    };
  }

  @Override
  public void close() {
    LockTracker lock = lockQueue.get().pollLast();

    if (lock == null) {
      throw new IndexOutOfBoundsException();
    } else if (lock.isWrite() == false) {
      readLock().unlock();
    } else {
      try {
        writeLock().unlock();
      } catch (IllegalMonitorStateException e) {
        e.printStackTrace();
      }
    }
  }

  public ClosableReadWriteLock lockRead() {
    readLock().lock();
    lockQueue.get().add(new LockTracker(false));
    return this;
  }

  public ClosableReadWriteLock lockWrite() {
    writeLock().lock();
    lockQueue.get().add(new LockTracker(true));
    return this;
  }
}
