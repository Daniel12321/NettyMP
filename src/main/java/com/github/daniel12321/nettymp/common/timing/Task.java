package com.github.daniel12321.nettymp.common.timing;

public class Task implements Comparable<Task> {

    private final long runTime;
    private Runnable task;

    public Task(long delay, Runnable task) {
        this.runTime = System.currentTimeMillis() + delay;
        this.task = task;
    }

    public long getRunTime() {
        return this.runTime;
    }

    public void run() {
        if (this.task == null)
            return;

        this.task.run();
        this.task = null;
    }

    @Override
    public int compareTo(Task o) {
        return (int) (this.runTime - o.runTime);
    }
}
