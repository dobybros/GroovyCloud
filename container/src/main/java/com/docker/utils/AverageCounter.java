package com.docker.utils;

import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;

import java.util.Queue;

/**
 * Calculate the average of the recent 10 numbers.
 *
 * Created by aplombchen on 21/5/17.
 */
public class AverageCounter {
    private int max = 10;
    private Integer average;
    private final long PERIOD = 10000;
    private Queue<Integer> queue = Queues.synchronizedQueue(EvictingQueue.create(10));
    private TimerTaskEx calculateTask = new TimerTaskEx("com.docker.utils.AverageCounter") {
        @Override
        public void execute() {
            Integer[] values = new Integer[10];
            queue.toArray(values);

            int count = 0;
            int total = 0;
            for(Integer value : values) {
                if(value != null) {
                    total++;
                    count += value;
                }
            }
            if(total > 0)
                average = count / total;
        }
    };
    public AverageCounter() {
        this(10);
    }

    public AverageCounter(int max) {
        this.max = max;
        queue = Queues.synchronizedQueue(EvictingQueue.create(this.max));
        TimerEx.schedule(calculateTask, PERIOD, PERIOD);
    }
    public void add(int num) {
        if(average == null)
            average = num;
        queue.add(num);
    }

    public Integer getAverage() {
        return average;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(AverageCounter.class.getSimpleName() + ": ");
        builder.append("average: " + average);
        return builder.toString();
    }
}
