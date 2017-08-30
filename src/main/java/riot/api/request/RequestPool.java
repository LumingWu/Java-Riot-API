package main.java.riot.api.request;

import main.java.riot.api.ratelimiter.RateLimiter;

import java.util.ArrayList;

public class RequestPool {

    /* Instances that keep the singleton of the requester and the record of the requests */
    private ArrayList<ArrayList<RateLimiter>> application_queues;
    private ArrayList<ArrayList<ArrayList<RateLimiter>>> method_queues;

    /* Rate limit constants subject to change */
    private static final int APP_RATE_PER_SECONDS = 10;
    private static final int METHOD_RATE_PER_SECONDS = 10;
    private static final int SECONDS = 10;
    private static final int APP_RATE_PER_MINUTES = 500;
    private static final int METHOD_RATE_PER_MINUTES = 500;
    private static final int MINUTES = 10;

    /* Calculation constants */
    private static final int NANOSECOND_PER_SECOND = 1_000_000_000;
    private static final int SECOND_PER_MINUTE = 60;

    protected RequestPool(){
        application_queues = new ArrayList<ArrayList<RateLimiter>>(Regions.values().length);
        for(int i = 0; i < Regions.values().length; i++){
            ArrayList<RateLimiter> queues = new ArrayList<RateLimiter>(2);
            queues.add(new RateLimiter(APP_RATE_PER_SECONDS, APP_RATE_PER_SECONDS, SECONDS));
            queues.add(new RateLimiter(APP_RATE_PER_MINUTES, APP_RATE_PER_MINUTES, MINUTES * SECOND_PER_MINUTE));
            application_queues.add(queues);
        }
        method_queues = new ArrayList<ArrayList<ArrayList<RateLimiter>>>(Regions.values().length);
        for(int i = 0; i < Regions.values().length; i++){
            ArrayList<ArrayList<RateLimiter>> method_lists = new ArrayList<ArrayList<RateLimiter>>(Methods.values().length);
            for(int j = 0; j < Methods.values().length; j++){
                ArrayList<RateLimiter> queues = new ArrayList<RateLimiter>(2);
                queues.add(new RateLimiter(METHOD_RATE_PER_SECONDS, METHOD_RATE_PER_SECONDS, SECONDS));
                queues.add(new RateLimiter(METHOD_RATE_PER_MINUTES, METHOD_RATE_PER_MINUTES, MINUTES * SECOND_PER_MINUTE));
                method_lists.add(queues);
            }
            method_queues.add(method_lists);
        }
    }

    /**
     * This method checks the queue requests to see if a request can be made to the Riot server.
     * It is synchronized because it is the key to make sure no two function will get a pass to request when only one function request.
     * @param region - the region
     * @param method - the method
     * @return true if a request can be made, false otherwise.
     */
    public synchronized boolean queueRequest(Regions region, Methods method){
        RateLimiter app_second_queue = application_queues.get(region.ordinal()).get(0);
        RateLimiter app_minute_queue = application_queues.get(region.ordinal()).get(1);
        RateLimiter method_second_queue = method_queues.get(region.ordinal()).get(method.ordinal()).get(0);
        RateLimiter method_minute_queue = method_queues.get(region.ordinal()).get(method.ordinal()).get(1);
        if(app_second_queue.requestRate()){
            if(app_minute_queue.requestRate()){
                if (method_second_queue.requestRate()){
                    if (method_minute_queue.requestRate()){
                        return true;
                    }
                    method_second_queue.incrementToken();
                }
                app_minute_queue.incrementToken();
            }
            app_second_queue.incrementToken();
        }
        return false;
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
