package com.cisco.services;

import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kkaiwen on 4/22/2016.
 */

@Component
public class ScheduleJob {

    public void reportCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        System.out.println("The time is now " + dateFormat.format(new Date()));
    }


}
