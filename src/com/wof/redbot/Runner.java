package com.wof.redbot;

public class Runner
{
    public static void main(String[]args)
    {
        if(args.length != 0)
        {
            RedBot rb = new RedBot();
            rb.run(args[0],args[1],args[2],args[3],args[4], args[5]);
        }
        if(args.length == 0)
        {
            RedBot rb = new RedBot();
            rb.run();
        }
    }
}
