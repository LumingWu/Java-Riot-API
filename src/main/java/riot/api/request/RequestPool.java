package main.java.riot.api.request;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

public class RequestPool {

    /* Instances that keep the singleton of the requester and the record of the requests */
    private static RequestPool instance;
    private static ArrayList<ArrayList<LinkedList<Instant>>> application_queues;
    private static ArrayList<ArrayList<ArrayList<LinkedList<Instant>>>> method_queues;

    /* Rate limit constants subject to change */
    private static final long APP_RATE_PER_SECONDS = 10;
    private static final long METHOD_RATE_PER_SECONDS = 10;
    private static final long SECONDS = 10;
    private static final long APP_RATE_PER_MINUTES = 500;
    private static final long METHOD_RATE_PER_MINUTES = 500;
    private static final long MINUTES = 10;

    /* Calculation constants */
    private static final long NANOSECOND_PER_SECOND = 1_000_000_000;
    private static final long SECOND_PER_MINUTE = 60;

    /* static initializer to setup objects that need high level structure */
    static {
        instance = new RequestPool();
        application_queues = new ArrayList<ArrayList<LinkedList<Instant>>>(Regions.values().length);
        for(int i = 0; i < Regions.values().length; i++){
            ArrayList<LinkedList<Instant>> queues = new ArrayList<LinkedList<Instant>>(2);
            queues.add(new LinkedList<Instant>());
            queues.add(new LinkedList<Instant>());
            application_queues.add(queues);
        }
        method_queues = new ArrayList<ArrayList<ArrayList<LinkedList<Instant>>>>(Regions.values().length);
        for(int i = 0; i < Regions.values().length; i++){
            ArrayList<ArrayList<LinkedList<Instant>>> method_lists = new ArrayList<ArrayList<LinkedList<Instant>>>(Methods.values().length);
            for(int j = 0; j < Methods.values().length; j++){
                ArrayList<LinkedList<Instant>> queues = new ArrayList<LinkedList<Instant>>(2);
                queues.add(new LinkedList<Instant>());
                queues.add(new LinkedList<Instant>());
                method_lists.add(queues);
            }
            method_queues.add(method_lists);
        }
    }

    public RequestPool(){

    }

    /**
     * This method checks the queue requests to see if a request can be made to the Riot server.
     * It is synchronized because it is the key to make sure no two function will get a pass to request when only one function request.
     * @param region - the region
     * @param method - the method
     * @return true if a request can be made, false otherwise.
     */
    public static synchronized boolean queueRequest(Regions region, Methods method){
        LinkedList<Instant> app_second_queue = application_queues.get(region.ordinal()).get(0);
        LinkedList<Instant> app_minute_queue = application_queues.get(region.ordinal()).get(1);
        LinkedList<Instant> method_second_queue = method_queues.get(region.ordinal()).get(method.ordinal()).get(0);
        LinkedList<Instant> method_minute_queue = method_queues.get(region.ordinal()).get(method.ordinal()).get(1);
        Instant timestamp = Instant.now();
        long timestamp_in_nano = timestamp.getEpochSecond() * NANOSECOND_PER_SECOND + timestamp.getNano();
        /* If a queue is adding an item that will hit the limit, check if the time difference is going to be too small */
        long app_difference_in_seconds;
        long app_difference_in_minutes;
        long method_difference_in_seconds;
        long method_difference_in_minutes;
        if((app_second_queue.size() == APP_RATE_PER_SECONDS - 1
                && (app_difference_in_seconds = (timestamp_in_nano - app_second_queue.getFirst().getEpochSecond() * NANOSECOND_PER_SECOND - app_second_queue.getFirst().getNano())
                    / NANOSECOND_PER_SECOND) <= SECONDS)
                || (app_minute_queue.size() == APP_RATE_PER_MINUTES - 1
                && (app_difference_in_minutes = (timestamp_in_nano - app_minute_queue.getFirst().getEpochSecond() * NANOSECOND_PER_SECOND - app_minute_queue.getFirst().getNano())
                    / NANOSECOND_PER_SECOND / SECOND_PER_MINUTE) <= MINUTES)
                || (method_second_queue.size() == METHOD_RATE_PER_SECONDS - 1
                && (method_difference_in_seconds = (timestamp_in_nano - method_second_queue.getFirst().getEpochSecond() * NANOSECOND_PER_SECOND - method_second_queue.getFirst().getNano())
                    / NANOSECOND_PER_SECOND) <= SECONDS)
                || (method_minute_queue.size() == METHOD_RATE_PER_MINUTES - 1
                && (method_difference_in_minutes = (timestamp_in_nano - method_minute_queue.getFirst().getEpochSecond() * NANOSECOND_PER_SECOND - method_minute_queue.getFirst().getNano())
                    / NANOSECOND_PER_SECOND / SECOND_PER_MINUTE) <= MINUTES)){
            return false;
        }
        app_second_queue.addLast(timestamp);
        app_minute_queue.addLast(timestamp);
        method_second_queue.addLast(timestamp);
        method_minute_queue.addLast(timestamp);
        /* Pop first item of queues if it is no longer useful for comparison */
        if(app_second_queue.size() == APP_RATE_PER_SECONDS){
            app_second_queue.removeFirst();
        }
        if(app_minute_queue.size() == APP_RATE_PER_MINUTES){
            app_minute_queue.removeFirst();
        }
        if(method_second_queue.size() == METHOD_RATE_PER_SECONDS){
            method_second_queue.removeFirst();
        }
        if(method_minute_queue.size() == METHOD_RATE_PER_MINUTES){
            method_minute_queue.removeFirst();
        }
        return true;
    }

    /**
     * This function simply prints the items in all the queues.
     */
    public static void printQueues(){
        System.out.println("       Application Queue:");
        for(int i = 0; i < application_queues.size(); i++){
            System.out.println("              " + RequestPool.Regions.values()[i].toString());
            System.out.println("                     Second : " + application_queues.get(i).get(0).toString());
            System.out.println("                     Minute : " + application_queues.get(i).get(1).toString());
        }
        System.out.println("       Method Queue:");
        for(int i = 0; i < method_queues.size(); i++){
            for(int j = 0; j < method_queues.get(i).size(); j++){
                System.out.println("              " + RequestPool.Regions.values()[i].toString() + ", " + RequestPool.Methods.values()[j].toString());
                System.out.println("                     Second : " + method_queues.get(i).get(j).get(0).toString());
                System.out.println("                     Minute : " + method_queues.get(i).get(j).get(1).toString());
            }
        }
    }

    /**
     * Accessor methods for JUnit Tests
     */

    public static long getAppRatePerSeconds(){
        return APP_RATE_PER_SECONDS;
    }

    public static long getAppRatePerMinutes(){
        return APP_RATE_PER_MINUTES;
    }

    public static long getMethodRatePerSeconds(){
        return METHOD_RATE_PER_SECONDS;
    }

    public static long getMethodRatePerMinutes(){
        return METHOD_RATE_PER_MINUTES;
    }

    public static long getSeconds(){
        return SECONDS;
    }

    public static long getMinutes(){
        return MINUTES;
    }

    public static ArrayList<ArrayList<LinkedList<Instant>>> getApplication_queues(){
        return application_queues;
    }

    public static ArrayList<ArrayList<ArrayList<LinkedList<Instant>>>> getMethod_queues(){
        return method_queues;
    }

    /* Enum that helps maintaining the size of queues and index */
    public static enum Regions{
        ru, kr, br1, oc1, jp1, na1, eun1, euw1, tr1, la1, la2
    }
    // Add method names here
    public static enum Methods{
        summonerByName, summonerById, summonerByAccountId,
        championMasteriesBySummonerId, championMasteryBySummonerIdChampionId, championMasteryScoresBySummonerId,
        matchById, matchesByAccountId, matchesByAccountIdFilter, matchTimelinesByMatchId,
        champions, championById,
        challengerLeagueByQueue, leagueBySummonerId, masterLeagueByQueue, positionBySummonerId,
        masteriesBySummonerId,
        runesBySummonerId
    }

}
