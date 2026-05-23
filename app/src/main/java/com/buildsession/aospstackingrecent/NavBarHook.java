package com.buildsession.aospstackingrecent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.animation.ObjectAnimator;

import java.lang.reflect.Constructor;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * 负责在 Launcher 设置中添加导航条颜色自定义选项，并尝试修改其显示逻辑。
 */
public class NavBarHook {
    private static final String KEY_NAV_HANDLE_DARK_BG_COLOR = "pref_nav_handle_dark_bg_custom_color";
    private static final String KEY_NAV_HANDLE_LIGHT_BG_COLOR = "pref_nav_handle_light_bg_custom_color";
    private static final String KEY_BUILD_SETTINGS_CATEGORY = "build_settings_category";
    private static final String LAUNCHER_PREFS_NAME = "com.android.launcher3.prefs";

    private final ClassLoader classLoader;

    public NavBarHook(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void initHooks() {
        initSettingsHooks();
        initHandleHooks();
    }

    private void initSettingsHooks() {
        try {
            Class<?> settingsFragmentClass = XposedHelpers.findClassIfExists("com.android.launcher3.settings.SettingsActivity$LauncherSettingsFragment", classLoader);
            if (settingsFragmentClass == null) {
                settingsFragmentClass = XposedHelpers.findClassIfExists("com.google.android.apps.nexuslauncher.settings.MySettingsFragment", classLoader);
            }

            final Class<?> prefClass = XposedHelpers.findClassIfExists("androidx.preference.Preference", classLoader);
            final Class<?> catClass = XposedHelpers.findClassIfExists("androidx.preference.PreferenceCategory", classLoader);
            final Class<?> vhClass = XposedHelpers.findClassIfExists("androidx.preference.PreferenceViewHolder", classLoader);

            if (settingsFragmentClass != null && prefClass != null && catClass != null) {
                XposedBridge.hookAllMethods(settingsFragmentClass, "onCreatePreferences", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object fragment = param.thisObject;
                        Object screen = XposedHelpers.callMethod(fragment, "getPreferenceScreen");
                        Context context = (Context) XposedHelpers.callMethod(fragment, "getContext");

                        if (screen == null || context == null) return;

                        // Check if category already exists to avoid duplication
                        Object existingCategory = XposedHelpers.callMethod(screen, "findPreference", KEY_BUILD_SETTINGS_CATEGORY);
                        if (existingCategory != null) return;

                        Object category = createPreferenceInstance(catClass, context);
                        if (category == null) return;

                        XposedHelpers.callMethod(category, "setKey", KEY_BUILD_SETTINGS_CATEGORY);
                        XposedHelpers.callMethod(category, "setTitle", "Build_ 的模块设置");
                        XposedHelpers.callMethod(screen, "addPreference", category);

                        SharedPreferences sp = context.getSharedPreferences(LAUNCHER_PREFS_NAME, Context.MODE_PRIVATE);

                        // Dark Background / Light Handle
                        Object darkBgColorPref = createPreferenceInstance(prefClass, context);
                        if (darkBgColorPref != null) {
                            XposedHelpers.callMethod(darkBgColorPref, "setKey", KEY_NAV_HANDLE_DARK_BG_COLOR);
                            XposedHelpers.callMethod(darkBgColorPref, "setTitle", "深色背景下小白条颜色");
                            setPreferenceSelectable(darkBgColorPref, false);
                            XposedHelpers.callMethod(category, "addPreference", darkBgColorPref);
                        }

                        // Light Background / Dark Handle
                        Object lightBgColorPref = createPreferenceInstance(prefClass, context);
                        if (lightBgColorPref != null) {
                            XposedHelpers.callMethod(lightBgColorPref, "setKey", KEY_NAV_HANDLE_LIGHT_BG_COLOR);
                            XposedHelpers.callMethod(lightBgColorPref, "setTitle", "浅色背景下小白条颜色");
                            setPreferenceSelectable(lightBgColorPref, false);
                            XposedHelpers.callMethod(category, "addPreference", lightBgColorPref);
                        }
                    }
                });

                if (vhClass != null) {
                    XposedHelpers.findAndHookMethod(prefClass, "onBindViewHolder", vhClass, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Object preference = param.thisObject;
                            String key = (String) XposedHelpers.callMethod(preference, "getKey");
                            if (KEY_NAV_HANDLE_DARK_BG_COLOR.equals(key) || KEY_NAV_HANDLE_LIGHT_BG_COLOR.equals(key)) {
                                setupInlinePreferenceUI(preference, param.args[0], key);
                            }
                        }
                    });
                }
            }
        } catch (Throwable t) {
            XposedBridge.log("AOSPStackingRecent: Error hooking settings UI: " + t.getMessage());
        }
    }

    private void setupInlinePreferenceUI(Object preference, Object holder, String key) {
        try {
            View itemView = (View) XposedHelpers.getObjectField(holder, "itemView");
            if (!(itemView instanceof ViewGroup)) return;
            ViewGroup vg = (ViewGroup) itemView;

            if (vg.findViewWithTag("custom_color_ui") != null) return;

            Context context = itemView.getContext();
            SharedPreferences sp = context.getSharedPreferences(LAUNCHER_PREFS_NAME, Context.MODE_PRIVATE);

            // Create a container for our inline UI
            LinearLayout container = new LinearLayout(context);
            container.setTag("custom_color_ui");
            container.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams containerLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            int marginSide = Math.round(16 * context.getResources().getDisplayMetrics().density);
            int marginBottom = Math.round(8 * context.getResources().getDisplayMetrics().density);
            container.setLayoutParams(containerLp);
            container.setPadding(marginSide, 0, marginSide, marginBottom);
            container.setGravity(Gravity.CENTER_VERTICAL);

            final EditText editText = new EditText(context);
            editText.setHint("#AARRGGBB");
            editText.setText(sp.getString(key, ""));
            editText.setTextSize(14);
            editText.setSingleLine(true);
            // Try to set a background that looks like an input field if possible
            editText.setPadding(Math.round(8 * context.getResources().getDisplayMetrics().density), 
                              Math.round(8 * context.getResources().getDisplayMetrics().density), 
                              Math.round(8 * context.getResources().getDisplayMetrics().density), 
                              Math.round(8 * context.getResources().getDisplayMetrics().density));
            
            LinearLayout.LayoutParams etLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            etLp.rightMargin = Math.round(8 * context.getResources().getDisplayMetrics().density);
            editText.setLayoutParams(etLp);

            Button btnApply = new Button(context);
            btnApply.setText("Apply");
            btnApply.setTextSize(12);
            btnApply.setAllCaps(false);
            btnApply.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                    Math.round(60 * context.getResources().getDisplayMetrics().density),
                    Math.round(36 * context.getResources().getDisplayMetrics().density));
            btnApply.setLayoutParams(btnLp);
            
            btnApply.setOnClickListener(v -> {
                String val = editText.getText().toString().trim();
                if (val.isEmpty()) {
                    sp.edit().remove(key).apply();
                } else {
                    try {
                        Color.parseColor(val);
                        sp.edit().putString(key, val).apply();
                        // Clear focus to hide keyboard
                        editText.clearFocus();
                    } catch (Exception e) {
                        // Invalid color - maybe show red text?
                        editText.setTextColor(Color.RED);
                        return;
                    }
                }
                editText.setTextColor(editText.getHintTextColors()); // Reset color
            });

            Button btnReset = new Button(context);
            btnReset.setText("重置");
            btnReset.setTextSize(12);
            btnReset.setAllCaps(false);
            btnReset.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams resetLp = new LinearLayout.LayoutParams(
                    Math.round(60 * context.getResources().getDisplayMetrics().density),
                    Math.round(36 * context.getResources().getDisplayMetrics().density));
            resetLp.leftMargin = Math.round(4 * context.getResources().getDisplayMetrics().density);
            btnReset.setLayoutParams(resetLp);
            
            btnReset.setOnClickListener(v -> {
                editText.setText("");
                sp.edit().remove(key).apply();
                editText.setTextColor(editText.getHintTextColors());
            });

            container.addView(editText);
            container.addView(btnApply);
            container.addView(btnReset);

            // Change itemView to vertical if it's a LinearLayout to stack our UI below the title
            if (vg instanceof LinearLayout) {
                ((LinearLayout) vg).setOrientation(LinearLayout.VERTICAL);
            }
            vg.addView(container);

            // Hide the summary if it's confusing
            XposedHelpers.callMethod(preference, "setSummary", "");
        } catch (Throwable t) {
            XposedBridge.log("AOSPStackingRecent: Error setting up inline UI: " + t.getMessage());
        }
    }

    private void initHandleHooks() {
        try {
            Class<?> stashedHandleViewClass = XposedHelpers.findClassIfExists("com.android.launcher3.taskbar.StashedHandleView", classLoader);
            if (stashedHandleViewClass != null) {
                XposedBridge.hookAllMethods(stashedHandleViewClass, "updateHandleColor", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) param.thisObject;
                        Context context = view.getContext();
                        SharedPreferences sp = context.getSharedPreferences(LAUNCHER_PREFS_NAME, Context.MODE_PRIVATE);

                        boolean isRegionDark = (boolean) param.args[0];
                        String key = isRegionDark ? KEY_NAV_HANDLE_DARK_BG_COLOR : KEY_NAV_HANDLE_LIGHT_BG_COLOR;
                        String customColorHex = sp.getString(key, "");

                        if (customColorHex != null && !customColorHex.isEmpty()) {
                            try {
                                int color = Color.parseColor(customColorHex);
                                boolean animate = (boolean) param.args[1];

                                if (animate) {
                                    ObjectAnimator mColorChangeAnim = (ObjectAnimator) XposedHelpers.getObjectField(view, "mColorChangeAnim");
                                    if (mColorChangeAnim != null) {
                                        mColorChangeAnim.cancel();
                                    }

                                    Class<?> animUtilsClass = XposedHelpers.findClassIfExists("com.android.launcher3.LauncherAnimUtils", classLoader);
                                    if (animUtilsClass != null) {
                                        Object viewBackgroundColorProp = XposedHelpers.getStaticObjectField(animUtilsClass, "VIEW_BACKGROUND_COLOR");
                                        mColorChangeAnim = ObjectAnimator.ofArgb(view, (android.util.Property) viewBackgroundColorProp, color);
                                        XposedHelpers.setObjectField(view, "mColorChangeAnim", mColorChangeAnim);
                                        mColorChangeAnim.setDuration(120);
                                        mColorChangeAnim.start();
                                    } else {
                                        view.setBackgroundColor(color);
                                    }
                                } else {
                                    view.setBackgroundColor(color);
                                }
                                param.setResult(null);
                            } catch (Exception e) {
                                // Invalid color
                            }
                        }
                    }
                });
            }
        } catch (Throwable t) {
            XposedBridge.log("AOSPStackingRecent: Error hooking handle color: " + t.getMessage());
        }
    }

    private void setPreferenceSelectable(Object pref, boolean selectable) {
        try {
            XposedHelpers.callMethod(pref, "setSelectable", selectable);
        } catch (Throwable t) {
            try {
                // Try finding the method explicitly with boolean primitive
                java.lang.reflect.Method m = XposedHelpers.findMethodBestMatch(pref.getClass(), "setSelectable", boolean.class);
                m.invoke(pref, selectable);
            } catch (Throwable t2) {
                // If all fails, ignore or log
                XposedBridge.log("AOSPStackingRecent: Failed to setSelectable: " + t2.getMessage());
            }
        }
    }

    private Object createPreferenceInstance(Class<?> prefClass, Context context) {
        try {
            return XposedHelpers.newInstance(prefClass, context);
        } catch (Throwable t1) {
            try {
                return XposedHelpers.newInstance(prefClass, context, null);
            } catch (Throwable t2) {
                try {
                    Constructor<?> cons = XposedHelpers.findConstructorBestMatch(prefClass, Context.class);
                    return cons.newInstance(context);
                } catch (Throwable t3) {
                    return null;
                }
            }
        }
    }
}
