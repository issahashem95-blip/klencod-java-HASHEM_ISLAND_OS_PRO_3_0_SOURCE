package com.hashem.island;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.net.Uri;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    private LinearLayout root;
    private TextView status;
    private final int bg = 0xFF080A0F;
    private final int panel = 0xFF121722;
    private final int white = 0xFFF4F6FA;
    private final String[] themes = {"Hyper Gold", "Ocean Blue", "Violet", "Emerald", "Rose", "Pure White"};
    private final int[] accents = {0xFFFFC857,0xFF43C7FF,0xFFB69CFF,0xFF42D6A4,0xFFFF718D,0xFFF1F4F8};

    private int dp(float v){ return (int)(v*getResources().getDisplayMetrics().density+.5f); }
    private GradientDrawable shape(int color, int radius, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius));
        if (strokeColor != 0) d.setStroke(dp(1), strokeColor);
        return d;
    }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size);
        t.setTextColor(color); t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private TextView button(String s, Runnable action) {
        TextView b = text(s, 14, white, true);
        b.setGravity(Gravity.CENTER); b.setPadding(dp(14),dp(15),dp(14),dp(15));
        b.setBackground(shape(panel,18,0xFF263247)); b.setOnClickListener(v -> action.run());
        return b;
    }
    private void gap(int h){ View v=new View(this); root.addView(v,new LinearLayout.LayoutParams(1,dp(h))); }
    private int accent(){ return getPreferences(0).getInt("accent", accents[0]); }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        if (Build.VERSION.SDK_INT >= 23) getWindow().getDecorView().setSystemUiVisibility(0);
        render();
    }
    @Override protected void onResume(){ super.onResume(); refreshStatus(); }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this); root.setOrientation(1);
        root.setPadding(dp(20),dp(22),dp(20),dp(28)); root.setBackgroundColor(bg);
        scroll.addView(root);

        TextView brand = text("HASHEM",27,white,true);
        root.addView(brand);
        TextView sub = text("ISLAND OS  •  VERSION 3.0",11,0xFF8D9AAF,true);
        root.addView(sub); gap(22);

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(1); hero.setPadding(dp(18),dp(18),dp(18),dp(18));
        hero.setBackground(shape(0xFF111824,24,0xFF29364B));
        TextView live = text("●  LIVE ISLAND PREVIEW",10,accent(),true);
        hero.addView(live);
        FrameLayout preview = new FrameLayout(this);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1,dp(116)); pp.topMargin=dp(12);
        preview.setBackground(shape(0xFF080A0F,20,0xFF202A39));
        TextView pill = text("●",13,0xFF58D68D,true);
        pill.setGravity(Gravity.CENTER);
        pill.setBackground(shape(0xFF000000,30,0xFF252A32));
        FrameLayout.LayoutParams pl = new FrameLayout.LayoutParams(dp(205),dp(48),Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        pl.topMargin=dp(16); preview.addView(pill,pl);
        TextView previewTitle = text("HASHEM ISLAND",12,white,true);
        previewTitle.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams pt = new FrameLayout.LayoutParams(-1,dp(30),Gravity.BOTTOM);
        pt.bottomMargin=dp(12); preview.addView(previewTitle,pt);
        hero.addView(preview);
        TextView caption = text("تصميم عائم مستوحى من تجربة HyperOS، مع إشعارات وتوسّع سريع.",12,0xFF9BA8BA,false);
        caption.setPadding(0,dp(12),0,0); hero.addView(caption);
        root.addView(hero); gap(18);

        status = text("",12,0xFF9BA8BA,false);
        status.setPadding(dp(14),dp(13),dp(14),dp(13));
        status.setBackground(shape(0xFF101722,14,0xFF263247)); root.addView(status);
        gap(12);
        root.addView(button("١. السماح بالظهور فوق التطبيقات",()->overlayPermission()));
        gap(9);
        root.addView(button("٢. تفعيل الوصول إلى الإشعارات",()->startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))));
        gap(9);
        root.addView(button("▶  تشغيل الجزيرة الآن",()->startIsland()));
        gap(9);
        root.addView(button("■  إيقاف الجزيرة",()->stopService(new Intent(this,IslandOverlayService.class))));
        gap(9);
        root.addView(button("🎨  الثيمات والألوان",()->themes()));
        gap(9);
        root.addView(button("⚙  تخصيص الجزيرة",()->settings()));
        gap(9);
        root.addView(button("حول HASHEM ISLAND",()->new AlertDialog.Builder(this)
            .setTitle("HASHEM ISLAND OS PRO 3.0")
            .setMessage("تطبيق جزيرة عائمة مستقل لأجهزة Android. يحتاج إلى إذن الظهور فوق التطبيقات وإذن الإشعارات لعرض التنبيهات. لا يدّعي أنه جزء من نظام HyperOS ولا يغيّر ملفات النظام.")
            .setPositiveButton("حسناً",null).show()));
        setContentView(scroll);
        refreshStatus();
    }

    private void refreshStatus(){
        if(status==null)return;
        boolean overlay = Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this);
        status.setText("حالة الأذونات\\nالظهور فوق التطبيقات: "+(overlay?"مسموح ✓":"غير مفعّل")+
            "\\nالإشعارات: "+(isNotificationEnabled()?"مسموح ✓":"يحتاج تفعيل"));
    }
    private boolean isNotificationEnabled(){
        String enabled=Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners");
        return enabled!=null && enabled.contains(getPackageName());
    }
    private void overlayPermission(){
        if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));
        } else refreshStatus();
    }
    private void startIsland(){
        if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){ overlayPermission(); return; }
        Intent i=new Intent(this,IslandOverlayService.class);
        try { if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i); }
        catch(Exception e){ Toast.makeText(this,"تعذر التشغيل: "+e.getMessage(),Toast.LENGTH_LONG).show(); }
    }
    private void themes(){
        int selected=getPreferences(0).getInt("theme",0);
        new AlertDialog.Builder(this).setTitle("اختر مظهر الجزيرة")
            .setSingleChoiceItems(themes,selected,(d,w)->{
                getPreferences(0).edit().putInt("theme",w).putInt("accent",accents[w]).apply();
                d.dismiss(); sendRefresh(); Toast.makeText(this,"تم تطبيق "+themes[w],Toast.LENGTH_SHORT).show();
            }).setNegativeButton("إغلاق",null).show();
    }
    private void settings(){
        LinearLayout p=new LinearLayout(this); p.setOrientation(1); p.setPadding(dp(20),dp(8),dp(20),dp(8));
        TextView info=text("عدّل شكل الجزيرة وسلوكها",13,0xFF9BA8BA,false); p.addView(info);
        SeekBar width=new SeekBar(this); width.setMax(180); width.setProgress(getPreferences(0).getInt("width",220)-160);
        p.addView(text("العرض",13,accent(),true)); p.addView(width);
        SeekBar alpha=new SeekBar(this); alpha.setMax(100); alpha.setProgress(getPreferences(0).getInt("alpha",100));
        p.addView(text("عتامة الخلفية",13,accent(),true)); p.addView(alpha);
        SeekBar duration=new SeekBar(this); duration.setMax(14); duration.setProgress(getPreferences(0).getInt("duration",4)-1);
        p.addView(text("مدة ظهور الإشعار (ثوانٍ)",13,accent(),true)); p.addView(duration);
        CheckBox show=new CheckBox(this); show.setText("إظهار نص الإشعار"); show.setTextColor(white);
        show.setChecked(getPreferences(0).getBoolean("showText",true)); p.addView(show);
        new AlertDialog.Builder(this).setTitle("إعدادات الجزيرة").setView(p)
            .setPositiveButton("حفظ وتطبيق",(d,w)->{
                getPreferences(0).edit().putInt("width",160+width.getProgress())
                    .putInt("alpha",alpha.getProgress()).putInt("duration",duration.getProgress()+1)
                    .putBoolean("showText",show.isChecked()).apply(); sendRefresh();
            }).setNegativeButton("إلغاء",null).show();
    }
    private void sendRefresh(){
        try { Intent i=new Intent(this,IslandOverlayService.class).setAction("REFRESH_STYLE");
            if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i);
        } catch(Exception ignored){}
    }
}