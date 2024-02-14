package my.first.messenger.activities.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import my.first.messenger.activities.utils.PreferencesManager;

public class TimerService extends Service {
    private PreferencesManager preferencesManager;
    public TimerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       // throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        threader();
        Log.d("SERVICE","service");

        return super.onStartCommand(intent, flags, startId);


    }
    private void threader(){
        new Thread(){
            public void run(){
                try {
                    sleep(3000);

                } catch (InterruptedException e) {
                    return;
                }
            }
        }.start();
    }

}