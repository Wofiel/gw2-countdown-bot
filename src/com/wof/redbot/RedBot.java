/*
 * Basically, this is horribly mish-mash as I stumbled towards mostly functioning.
 * 
 * I also apologise for my variable names. I use bad names when I'm banging my head against the wall.
 */

package com.wof.redbot;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.watij.webspec.dsl.Tag;
import org.watij.webspec.dsl.WebSpec;

public class RedBot
{
    private static String USERNAME = "";
    private static String PASSWORD = "";
    private static long futureTime = 1335553200;
    
    /*
     * I don't even know if these work properly. I think the ph3 still grabs 5-hash strings...
     * I really didn't bother to test it.
     */
    private static Pattern ph5 = Pattern.compile("^#{5}[^#].*");
    private static Pattern ph3 = Pattern.compile("^#{3}[^#].*");
    
    private long longFutureDate;
    private int numhashesInt;
    private Timer timer = new Timer();
    private int runNo = 0;
    private WebSpec spec = new WebSpec().ie();
    
    public void run(String subreddit, String username, String password, String numhashes, String frequency, String futureDate)
    {
        //Have only really tested this with my own subreddit. Probably works though.
        System.out.println("Starting spec...");
        spec.hide();
        spec.open("http://www.reddit.com/r/"+subreddit+"/about/edit/");
        System.out.print("Loading page... ");
        spec.pauseUntilReady();
        System.out.println("Done!");
        
        //LOGGIN' IN YOOOO
        if(spec.findWithId("login_login").exists())
        {
            //I don't even know what happens if you put in the wrong details...
            login(spec,username,password);
        }
        /*
         * I don't know why the login's pauseUntilReady doesn't wait long enough...
         * Or maybe it does. I haven't tried removing this since I added it and it stopped being mad at me.
         */
        while(!spec.ready())
        {
            spec.pause(500);
        }
        
        //WE DON'T NEED NO SANITATION
        longFutureDate = Long.parseLong(futureDate);
        numhashesInt = Integer.parseInt(numhashes);
        
        createTimer(5);
    }
    
    public void run()
    {     
        System.out.println("Starting spec...");
        spec.hide();
        spec.open("http://www.reddit.com/r/woftest/about/edit/");
        System.out.print("Loading page... ");
        spec.pauseUntilReady();
        System.out.println("Done!");
        
        //LOGGIN' IN YOOOO
        if(spec.findWithId("login_login").exists())
        {
            login(spec,USERNAME,PASSWORD);
        }
        while(!spec.ready())
        {
            spec.pause(500);
        }
        
        //createTimer(5);
        numhashesInt = 5;
        longFutureDate = futureTime;
        updateDescription(spec);
    }
    
    private void createTimer(int minuteIntervals)
    {
        minuteIntervals = minuteIntervals*1000*60;
        
        TimerTask desc = new TimerTask(){public void run()
        {
            updateDescription(spec);
        }
        };
        
        
        Date loldate = new Date();
        
        /*
         * Use this to sync to the 5 minute mark.
         * scheduleAtFixedRate with the specific date start seemed 
         * to repeatedly execute with no regard for the interval
         */
        while(((((loldate.getTime()/1000)/60) % 60) %5) != 0)
        {
            //System.out.println((((loldate.getTime()/1000)/60) % 60) %15);
            System.out.println((((loldate.getTime()/1000)/60)% 60) + " mins");
            try
            {
                //Sleep for 5 seconds
                Thread.sleep(5000);
                loldate = new Date();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        timer.scheduleAtFixedRate(desc, 0, minuteIntervals);
        
    }

    private void login(WebSpec spec, String un, String pw)
    {
        System.out.print("Logging in as "+un+"... ");
        Tag login = spec.findWithId("user_login");
        login.set.value(un);
        login = spec.findWithId("passwd_login");
        login.set.value(pw);
        spec.findWithId("login_login").call("submit()");
        
        while(spec.findWithId("login_login").exists())
        {
            spec.pause(500);
        }
        spec.pauseUntilReady();
        System.out.println("Success!");
    }
    
    private TimerTask updateDescription(WebSpec spec)
    {
        runNo++;
        System.out.print(runNo + " - ");
        
        Tag tarea = spec.find("textarea").with("name", "description");
        String moo = tarea.get("value");
        
        Date theDate = new Date();
        long curTime = theDate.getTime()/1000;
        long timeleft = longFutureDate-curTime;
        
        int daysleft = Math.round(((timeleft/60)/60)/24);
        int hoursleft = Math.round(((timeleft/60)/60) % 24);
        int minutesleft = 1+Math.round((timeleft/60) % 60);
        
        /* 
         * Because it's like, 2 seconds behind after it ends up retrieving
         * added 1 to minutes because 2:00 is prettier than 1:59
         * Then had to fix this part on the fringe cases.
         */
        if(minutesleft == 60)
        {
            minutesleft = 0;
            hoursleft++;
        }
        
        //Create countdown string to be added
        String datelol = daysleft+" days "+hoursleft+" hours "+minutesleft+" minutes";
        
        /*
         * Split entire textarea on newlines. Need to do this anyway because 
         * the Tag set method which doesn't like un-escaped newlines 
         * and replaceAll was annoying me.
         */
        
        String tokenized[] = moo.split("\\r?\\n");
        
        Matcher m = null;
        Pattern p = null;
        
        /*
         * Was going to add proper functionality here to specify which heading you wanted to edit
         * (determined by number of # in reddit markup)
         * Got lazy.
         */
        
        if(numhashesInt==5)
        {
            p = ph5;
        }
        if(numhashesInt==3)
        {
            //p = ph3;
        }
        
        //Our final string to replace the textarea
        String lol=null;
        
        /*
         * Combination paragraph creator and string replacer.
         * This is really ugly.
         */
        
        for(int i=0;i<tokenized.length;i++)
        {
            m = p.matcher(tokenized[i]);
            if (m.matches())
            {
                tokenized[i] = "#####"+datelol;
            }
            if(i==0)
            {
                lol = tokenized[i]+"\\n";
            }
            else if(i==tokenized.length-1)
                lol += tokenized[i];
            else
                lol += tokenized[i]+"\\n";
            
        }
        System.out.println(datelol);
        
        //Fuck this method.
        tarea.set("value",lol);
        
        //This one too.
        spec.find("button").with.type("button").with.name("edit").click();
        
        //System.out.println("Form submitted.");
        spec.pauseUntilReady();
        
        return null;
    }
}
