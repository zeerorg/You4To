package com.example.rishabh.you4to;

import android.support.v4.app.Fragment;

/**
 * Created by rishabh on 12/23/16.
 */

public class BaseFragment extends Fragment {

    private static boolean running = false;

    public static boolean isRunning(){
        return running;
    }

    public static void setRunning(){
        running = true;
    }
}
