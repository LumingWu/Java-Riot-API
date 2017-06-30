package test;

import main.java.riot.api.request.RequestPool;
import main.java.riot.api.utility.ApiUtility;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class JUnit {

    /**
     * This test is to show how instant is not that precise, but it is enough for timestamp because millisecond difference
     * is detectable. the timeout between 2 threads may vary machine to machine, so don't think about fail test too much.
     * @throws InterruptedException
     */
    @Test
    public void instant_functionality_test() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(new Runnable(){
            @Override
            public void run() {
                RequestPool.queueRequest(RequestPool.Regions.br1, RequestPool.Methods.summonerByName);
            }
        });
        executor.awaitTermination(4, TimeUnit.MILLISECONDS);
        executor.submit(new Runnable(){
            @Override
            public void run() {
                RequestPool.queueRequest(RequestPool.Regions.br1, RequestPool.Methods.summonerByName);
            }
        });
        executor.awaitTermination(1, TimeUnit.SECONDS);
        LinkedList<Instant> queue = RequestPool.getApplication_queues().get(RequestPool.Regions.br1.ordinal()).get(0);
        Instant instant_start = queue.getFirst();
        Instant instant_end = queue.getLast();
        assertTrue("\nQueue size: " + queue.size()
                + "\nInstant start: " + instant_start
                + "\nInstant end: " + instant_end, instant_start.isBefore(instant_end));

    }

    /**
     * Following from the previous test, try to hit the second request limit
     * The thread start and finish order should be synced and the last queue request should be denied.
     */
    @Test
    public void second_rate_limit_test() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool((int)RequestPool.getSeconds() - 2);
        LinkedList<Long> thread_start_order = new LinkedList<Long>();
        LinkedList<Long> thread_finish_order = new LinkedList<Long>();
        boolean[] limit_test = {true};
        for(int i = 0; i < RequestPool.getSeconds() - 2; i++){
            executor.submit(new Runnable(){
                @Override
                public void run() {
                    thread_start_order.addLast(Thread.currentThread().getId());
                    limit_test[0] = RequestPool.queueRequest(RequestPool.Regions.br1, RequestPool.Methods.summonerByName);
                    thread_finish_order.addLast(Thread.currentThread().getId());
                }
            });
        }
        executor.awaitTermination(1, TimeUnit.SECONDS);
        boolean order_test = true;
        for(int i = 0; i < thread_start_order.size(); i++){
            if(thread_start_order.get(i) != thread_finish_order.get(i)){
                order_test = false;
            }
        }
        assertTrue("\nOrder: " + order_test
                + "\nLimit: " + limit_test[0]
                + "\n" + thread_start_order.toString()
                + "\n" + thread_finish_order.toString(),order_test && !limit_test[0]);
    }



}
