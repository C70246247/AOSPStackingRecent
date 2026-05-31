package com.buildsession.aospstackingrecent;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 模块的设置界面，用于调整多任务卡片间距和堆叠紧密度。
 */
public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "com.buildsession.aospstackingrecent_preferences";
    private static final String KEY_SPACING = "card_spacing_factor";
    private static final String KEY_TIGHTNESS = "card_tightness_factor";
    private static final float DEFAULT_SPACING = 4.7f;
    private static final float DEFAULT_TIGHTNESS = 0.72f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Use ScrollView as root to prevent content being cut off
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dpToPx(24);
        layout.setPadding(padding, padding, padding, padding);
        
        // Add some top margin to avoid overlap with ActionBar/StatusBar if any
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);
        
        scrollView.addView(layout);

        TextView titleView = new TextView(this);
        titleView.setText("AOSPStackingRecent");
        titleView.setTextSize(28);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setPadding(0, dpToPx(16), 0, dpToPx(8));
        titleView.setGravity(android.view.Gravity.CENTER);
        layout.addView(titleView);

        TextView authorView = new TextView(this);
        authorView.setText("Module by BuildSession");
        authorView.setTextSize(16);
        authorView.setAlpha(0.7f);
        authorView.setPadding(0, 0, 0, dpToPx(32));
        authorView.setGravity(android.view.Gravity.CENTER);
        layout.addView(authorView);

        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(1)));
        divider.setBackgroundColor(0xFFCCCCCC);
        layout.addView(divider);

        final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Ensure keys exist so file is created
        if (!prefs.contains(KEY_SPACING)) {
            prefs.edit().putFloat(KEY_SPACING, DEFAULT_SPACING).commit();
        }
        if (!prefs.contains(KEY_TIGHTNESS)) {
            prefs.edit().putFloat(KEY_TIGHTNESS, DEFAULT_TIGHTNESS).commit();
        }
        fixPermissions();

        TextView statusView = new TextView(this);
        statusView.setText("前往LSP管理器激活模块即可使用");
        statusView.setGravity(android.view.Gravity.CENTER);
        statusView.setPadding(0, dpToPx(48), 0, dpToPx(24));
        layout.addView(statusView);

        TextView githubView = new TextView(this);
        githubView.setText("查看项目 GitHub 页面");
        githubView.setTextColor(0xFF007AFF); // iOS style blue
        githubView.setGravity(android.view.Gravity.CENTER);
        githubView.setPadding(0, dpToPx(16), 0, dpToPx(16));
        githubView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse("https://github.com/C70246247/AOSPStackingRecent"));
                startActivity(intent);
            }
        });
        layout.addView(githubView);

        setContentView(scrollView);
    }

    private void fixPermissions() {
        try {
            java.io.File sharedPrefsDir = new java.io.File(getApplicationInfo().dataDir, "shared_prefs");
            if (sharedPrefsDir.exists()) {
                sharedPrefsDir.setExecutable(true, false);
                sharedPrefsDir.setReadable(true, false);
                java.io.File prefsFile = new java.io.File(sharedPrefsDir, PREFS_NAME + ".xml");
                if (prefsFile.exists()) {
                    prefsFile.setReadable(true, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    interface OnValueChangeListener {
        void onValueChanged(float value);
    }
}
