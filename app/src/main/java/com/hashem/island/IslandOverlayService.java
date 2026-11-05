package com.hashem.island;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public class IslandOverlayService extends Service {
    private WindowManager wm;
    private View island;
    private WindowManager.LayoutParams params;
    private LinearLayout container, row, expandedPanel;
    private TextView appTitle, detail, time;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable hideTask;
    private int accent=0xFFFFC857, islandWidth=220, bgAlpha=100, displaySeconds=4;
    private boolean showText=true, expanded=false;
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    @Override public void onCreate(){
        super.onCreate(); loadPrefs(); createChannel();
        Notification n=new Notification.Builder(this,"hashem_island")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("HASHEM ISLAND تعمل")
            .setContentText("اضغط هنا للعودة إلى التطبيق")
            .setOngoing(true).build();
        startForeground(71,n);
        if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){stopSelf();return;}
        attachIsland();
    }
    private void createChannel(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel c=new NotificationChannel("hashem_island","HASHEM Island",NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
    }
    private void loadPrefs(){
        android.content.SharedPreferences p=getSharedPreferences("MainActivity",MODE_PRIVATE);
        accent=p.getInt("accent",0xFFFFC857); islandWidth=p.getInt("width",220);
        bgAlpha=p.getInt("alpha",100); displaySeconds=p.getInt("duration",4);
        showText=p.getBoolean("showText",true);
    }
    private GradientDrawable background(boolean open){
        GradientDrawable d=new GradientDrawable();
        int a=Math.max(170,Math.min(255,bgAlpha*255/100));
        d.setColor(Color.argb(a,5,7,11)); d.setCornerRadius(dp(open?28:40));
        d.setStroke(dp(1),Color.argb(190,Color.red(accent),Color.green(accent),Color.blue(accent)));
        return d;
    }
    private TextView makeText(String s,int size,int color,boolean bold){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }
    private void attachIsland(){
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        container=new LinearLayout(this); container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(13),dp(8),dp(13),dp(8)); container.setBackground(background(false));
        row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
        TextView dot=makeText("●",10,0xFF50D890,true);
        row.addView(dot,new LinearLayout.LayoutParams(dp(18),dp(24)));
        LinearLayout labels=new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL);
        appTitle=makeText("HASHEM ISLAND",11,Color.WHITE,true);
        detail=makeText("جاهزة • اضغط للتوسيع",10,0xFFB8C0CC,false);
        labels.addView(appTitle); labels.addView(detail);
        row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
        time=makeText("⋯",17,accent,true); row.addView(time);
        container.addView(row);
        expandedPanel=new LinearLayout(this); expandedPanel.setOrientation(LinearLayout.VERTICAL);
        expandedPanel.setPadding(dp(4),dp(12),dp(4),dp(4));
        TextView hint=makeText("مركز التنبيهات المصغّر",12,Color.WHITE,true);
        expandedPanel.addView(hint);
        TextView desc=makeText("ستظهر هنا تفاصيل الإشعارات الواردة.",10,0xFF9CA8B8,false);
        desc.setPadding(0,dp(5),0,dp(4)); expandedPanel.addView(desc);
        expandedPanel.setVisibility(View.GONE); container.addView(expandedPanel);
        params=new WindowManager.LayoutParams(dp(islandWidth),dp(52),
            Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL; params.y=dp(26);
        container.setOnClickListener(v->toggleExpanded());
        container.setOnTouchListener(new View.OnTouchListener(){
            float downX,downY; int startX,startY; boolean moved;
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getRawX();downY=e.getRawY();startX=params.x;startY=params.y;moved=false;return true;}
                if(e.getAction()==MotionEvent.ACTION_MOVE){
                    float dx=e.getRawX()-downX,dy=e.getRawY()-downY;
                    if(Math.abs(dx)>dp(5)||Math.abs(dy)>dp(5))moved=true;
                    if(moved){params.x=startX+(int)dx;params.y=Math.max(0,startY+(int)dy);try{wm.updateViewLayout(container,params);}catch(Exception ignored){}}
                    return true;
                }
                if(e.getAction()==MotionEvent.ACTION_UP){if(!moved)toggleExpanded();return true;}
                return true;
            }
        });
        island=container;
        try{wm.addView(island,params); island.setAlpha(0f); island.animate().alpha(1f).setDuration(260).start();}
        catch(Exception e){stopSelf();}
    }
    private void toggleExpanded(){
        if(container==null)return;
        expanded=!expanded; expandedPanel.setVisibility(expanded?View.VISIBLE:View.GONE);
        params.width=dp(expanded?Math.max(islandWidth,270):islandWidth);
        params.height=dp(expanded?112:52);
        container.setBackground(background(expanded));
        try{wm.updateViewLayout(container,params);}catch(Exception ignored){}
        container.animate().scaleX(expanded?1.025f:1f).scaleY(expanded?1.025f:1f).setDuration(180).start();
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null&&"STOP".equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
        if(intent!=null&&"REFRESH_STYLE".equals(intent.getAction())){loadPrefs();applyStyle();return START_NOT_STICKY;}
        if(intent!=null&&"SHOW_EVENT".equals(intent.getAction())){
            showEvent(intent.getStringExtra("title"),intent.getStringExtra("text"));
        }
        return START_STICKY;
    }
    private void applyStyle(){
        if(container==null)return; container.setBackground(background(expanded));
        params.width=dp(expanded?Math.max(islandWidth,270):islandWidth);
        try{wm.updateViewLayout(container,params);}catch(Exception ignored){}
        if(time!=null)time.setTextColor(accent);
    }
    private void showEvent(String title,String text){
        if(container==null)return; loadPrefs(); applyStyle();
        appTitle.setText(title==null||title.trim().isEmpty()?"إشعار جديد":title);
        detail.setText(showText&&text!=null&&!text.trim().isEmpty()?text:"وصلك إشعار جديد");
        if(hideTask!=null)handler.removeCallbacks(hideTask);
        if(!expanded){params.width=dp(islandWidth);params.height=dp(58);}
        container.setVisibility(View.VISIBLE); container.setScaleX(.88f);container.setScaleY(.88f);
        container.animate().scaleX(1f).scaleY(1f).setDuration(220).start();
        try{wm.updateViewLayout(container,params);}catch(Exception ignored){}
        hideTask=()->{
            if(container!=null&&!expanded){
                appTitle.setText("HASHEM ISLAND");detail.setText("جاهزة • اضغط للتوسيع");
            }
        };
        handler.postDelayed(hideTask,displaySeconds*1000L);
    }
    @Override public void onDestroy(){
        if(hideTask!=null)handler.removeCallbacks(hideTask);
        if(wm!=null&&island!=null)try{wm.removeView(island);}catch(Exception ignored){}
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent){return null;}
}