package com.github.daniel12321.nettymp.common.timing;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class TaskScheduler extends Thread {

    private static TaskScheduler instance;
    public static TaskScheduler getInstance() {
        return instance == null ? instance = new TaskScheduler() : instance;
    }

    private final Queue<Task> tasks;
    private boolean running;

    private TaskScheduler() {
        this.tasks = new PriorityBlockingQueue<>();
        this.running = true;

        this.start();
    }

    public void end(boolean force) {
        this.running = false;

        if (force)
            this.tasks.clear();

        synchronized (this) {
            notify();
        }
    }

    public void addTask(long delay, Runnable task) {
        if (!this.running)
            return;

        this.tasks.offer(new Task(delay, task));

        synchronized (this) {
            notify();
        }
    }

    @Override
    public void run() {
        while (this.running) {
            while (this.tasks.isEmpty() && this.running)
                waitForUnlock();

            if (this.tasks.isEmpty())
                continue;

            long current = System.currentTimeMillis();
            long millis = this.tasks.peek().getRunTime() - current - 2;
            if (millis > 2)
                waitFor(millis);

            current = System.currentTimeMillis();
            while (!this.tasks.isEmpty() && this.tasks.peek().getRunTime() <= current)
                this.tasks.poll().run();
        }
    }

    private void waitForUnlock() {
        try { synchronized (this) { wait(); } }
        catch (InterruptedException ignored) {}
    }

    private void waitFor(long millis) {
        try { synchronized (this) { wait(millis); } }
        catch (InterruptedException ignored) {}
    }
}
