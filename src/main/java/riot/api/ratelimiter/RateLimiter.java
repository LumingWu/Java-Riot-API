package main.java.riot.api.ratelimiter;


import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class RateLimiter{
    
    /**
     * maxAmount: the maximum number of tokens the bucket can hold
     * refillTime: the amount of time between refills
     * refillAmount: the number of tokens that are added to the bucket during a refill
     * value: the current number of tokens in the bucket
     * lastUpdate: the last time the bucket was updated
     */
    
    private int max_token;
    private int token;
    private int refill_time;
    private int refill_amount;
    private Instant last_update;
    
    public RateLimiter(int maxstoken, int refilltime, int refillamount){
        max_token = maxstoken;
        token = max_token;
        refill_time = refilltime;
        refill_amount = refillamount;
        last_update = Instant.now(Clock.systemUTC());
    }

    public RateLimiter(int maxtoken){
        this(maxtoken, 1, 1);
    }
    
    public RateLimiter(int maxstoken, int refilltime){
        this(maxstoken, refilltime, 1);
    }
    
    public synchronized boolean requestRate(){
        Instant now = Instant.now(Clock.systemUTC());
        int refill_count = (int) Math.floor(Duration.between(last_update, now).getSeconds() / refill_time);
        token = Math.min(max_token, token + refill_count * refill_amount);
        last_update = last_update.plusSeconds(refill_count * refill_time);
        if(token > 0){
            token -= 1;
            return true;
        }
        return false;
    }

    public synchronized void incrementToken(){
        if(token < max_token){
            token += 1;
        }
    }

    public int getToken(){
        return token;
    }

    public Instant getLastUpdate(){
        return last_update;
    }


}
