package com.hashem.island;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.Intent;
import android.app.Notification;
import android.os.Bundle;

public class IslandNotificationListener extends NotificationListenerService {
    @Override public void onNotificationPosted(StatusBarNotification sbn){
        if(sbn==null||sbn.getNotification()==null)return;
        Notification n=sbn.getNotification();
        if((n.flags&Notification.FLAG_ONGOING_EVENT)!=0)return;
        Bundle b=n.extras;
        CharSequence title=b==null?null:b.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence body=b==null?null:b.getCharSequence(Notification.EXTRA_BIG_TEXT);
        if(body==null&&b!=null)body=b.getCharSequence(Notification.EXTRA_TEXT);
        Intent i=new Intent(this,IslandOverlayService.class).setAction("SHOW_EVENT");
        i.putExtra("title",title==null?sbn.getPackageName():title.toString());
        i.putExtra("text",body==null?"إشعار جديد":body.toString());
        try{if(android.os.Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);}catch(Exception ignored){}
    }
}