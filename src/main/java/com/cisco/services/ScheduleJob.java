package com.cisco.services;

import org.springframework.stereotype.Component;

/**
 * Created by kkaiwen on 4/22/2016.
 */

@Component
public class ScheduleJob {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        System.out.println("The time is now " + dateFormat.format(new Date()));
    }
}
