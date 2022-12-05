package com.trill.ecommerce.util.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.trill.ecommerce.R;


public class SystemBarUtils {

    private SystemBarUtils() {
    }

    public static void setStatusBarColor(Window window, int color, boolean darkIcons) {

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(darkIcons){
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }else{
                window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        window.setStatusBarColor(color);
        final View container = window.getDecorView().findViewById(R.id.container);
        if(container != null){
            container.setFitsSystemWindows(true);
            container.requestApplyInsets();
        }
    }

    public static void setStatusBarTranslucentDark(Window window){
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0)
        );
        final View container = window.getDecorView().findViewById(R.id.container);
        if(container != null){
            container.setFitsSystemWindows(false);
            container.setPadding(0, 0, 0, 0);
        }
    }

    public static void setStatusBarTranslucent(Window window){
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        final View container = window.getDecorView().findViewById(R.id.container);
        if(container != null){
            container.setFitsSystemWindows(false);
            container.setPadding(0, 0, 0, 0);
        }
    }

    public static void setStatusBarIconsDark(Window window){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|window.getDecorView().getSystemUiVisibility());
        }
    }

    public static void setStatusBarIconsLight(Window window){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility()-View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void setNavTranslucent(Window window){

    }

    public static void setNavColor(Window window, int color, boolean darkIcons){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            if(darkIcons){
                window.getDecorView().findViewById(android.R.id.content).setSystemUiVisibility(window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }else{
                window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
            //this call doesn't need SDK O, but it will sometimes set the navbar to white and make the navigation bar icons hard to see. Just leave the navigation bar as system default for such cases
            window.setNavigationBarColor(color);
        }else if(!darkIcons){
            window.setNavigationBarColor(color);
        }
    }

    //this one determines whether you need dark icons on its own
    public static void setNavColor(Window window, int color){
        setNavColor(window, color, !isColorDark(color));
    }

    public static boolean isColorDark(int color){
        double darkness = 1-(0.299* Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.45){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) view = new View(activity);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)  view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showSoftKeyboard(Context context, View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }


}
