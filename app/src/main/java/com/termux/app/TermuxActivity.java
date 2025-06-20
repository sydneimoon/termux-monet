package com.termux.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.EditText;
//import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.termux.R;
import com.termux.app.api.file.FileReceiverActivity;
import com.termux.app.style.TermuxBackgroundManager;
import com.termux.app.terminal.TermuxActivityRootView;
import com.termux.app.terminal.TermuxTerminalSessionActivityClient;
import com.termux.app.terminal.io.TermuxTerminalExtraKeys;
import com.termux.shared.activities.ReportActivity;
import com.termux.shared.activity.ActivityUtils;
import com.termux.shared.activity.media.AppCompatActivityUtils;
import com.termux.shared.data.IntentUtils;
import com.termux.shared.android.PermissionUtils;
import com.termux.shared.data.DataUtils;
import com.termux.shared.termux.TermuxConstants;
import com.termux.shared.termux.TermuxConstants.TERMUX_APP.TERMUX_ACTIVITY;
import com.termux.app.activities.HelpActivity;
import com.termux.app.activities.SettingsActivity;
import com.termux.shared.termux.crash.TermuxCrashUtils;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;
import com.termux.app.terminal.TermuxSessionsListViewController;
import com.termux.app.terminal.io.TerminalToolbarViewPager;
import com.termux.app.terminal.TermuxTerminalViewClient;
import com.termux.shared.termux.extrakeys.ExtraKeysView;
import com.termux.shared.termux.interact.TextInputDialogUtils;
import com.termux.shared.logger.Logger;
import com.termux.shared.termux.TermuxUtils;
import com.termux.shared.termux.settings.properties.TermuxAppSharedProperties;
import com.termux.shared.termux.theme.TermuxThemeUtils;
import com.termux.shared.theme.NightMode;
import com.termux.shared.view.ViewUtils;
import com.termux.shared.view.KeyboardUtils;
import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;
import com.termux.view.TerminalView;
import com.termux.view.TerminalViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import java.util.Arrays;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.termux.app.CustomDialogFragment;

import java.util.List;
import android.util.Log;
//import android.view.View;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsAnimationCompat.Callback;
import androidx.core.view.WindowInsetsAnimationCompat.BoundsCompat;

/**
 * A terminal emulator activity.
 * <p/>
 * See
 * <ul>
 * <li>http://www.mongrel-phones.com.au/default/how_to_make_a_local_service_and_bind_to_it_in_android</li>
 * <li>https://code.google.com/p/android/issues/detail?id=6426</li>
 * </ul>
 * about memory leaks.
 */
public final class TermuxActivity extends BaseTermuxActivity implements ServiceConnection {

    /**
     * The connection to the {@link TermuxService}. Requested in {@link #onCreate(Bundle)} with a call to
     * {@link #bindService(Intent, ServiceConnection, int)}, and obtained and stored in
     * {@link #onServiceConnected(ComponentName, IBinder)}.
     */
    TermuxService mTermuxService;

    /**
     * The {@link TerminalView} shown in  {@link TermuxActivity} that displays the terminal.
     */
    TerminalView mTerminalView;

    /**
     *  The {@link TerminalViewClient} interface implementation to allow for communication between
     *  {@link TerminalView} and {@link TermuxActivity}.
     */
    TermuxTerminalViewClient mTermuxTerminalViewClient;

    /**
     *  The {@link TerminalSessionClient} interface implementation to allow for communication between
     *  {@link TerminalSession} and {@link TermuxActivity}.
     */
    TermuxTerminalSessionActivityClient mTermuxTerminalSessionActivityClient;

    /**
     * Termux app shared preferences manager.
     */
    private TermuxAppSharedPreferences mPreferences;

    /**
     * Termux app SharedProperties loaded from termux.properties
     */
    private TermuxAppSharedProperties mProperties;

    /**
     * The root view of the {@link TermuxActivity}.
     */
    TermuxActivityRootView mTermuxActivityRootView;

    /**
     * The space at the bottom of {@link @mTermuxActivityRootView} of the {@link TermuxActivity}.
     */
    View mTermuxActivityBottomSpaceView;

    /**
     * The terminal extra keys view.
     */
    ExtraKeysView mExtraKeysView;
    ExtraKeysView mExtraKeysView2;

    /**
     * The client for the {@link #mExtraKeysView}.
     */
    TermuxTerminalExtraKeys mTermuxTerminalExtraKeys;
    TermuxTerminalExtraKeys mTermuxTerminalExtraKeys2;

    /**
     * The termux sessions list controller.
     */
    TermuxSessionsListViewController mTermuxSessionListViewController;

    /**
     * The termux background manager for updating background.
     */
    TermuxBackgroundManager mTermuxBackgroundManager;

    /**
     * The {@link TermuxActivity} broadcast receiver for various things like terminal style configuration changes.
     */
    private final BroadcastReceiver mTermuxActivityBroadcastReceiver = new TermuxActivityBroadcastReceiver();

    /**
     * The last toast shown, used cancel current toast before showing new in {@link #showToast(String, boolean)}.
     */
    Toast mLastToast;

    /**
     * If between onResume() and onStop(). Note that only one session is in the foreground of the terminal view at the
     * time, so if the session causing a change is not in the foreground it should probably be treated as background.
     */
    private boolean mIsVisible;

    /**
     * If onResume() was called after onCreate().
     */
    private boolean mIsOnResumeAfterOnCreate = false;

    /**
     * If activity was restarted like due to call to {@link #recreate()} after receiving
     * {@link TERMUX_ACTIVITY#ACTION_RELOAD_STYLE}, system dark night mode was changed or activity
     * was killed by android.
     */
    private boolean mIsActivityRecreated = false;

    /**
     * The {@link TermuxActivity} is in an invalid state and must not be run.
     */
    private boolean mIsInvalidState;

    public boolean isToolbarHidden = false;

    private int mNavBarHeight;

    private float mTerminalToolbarDefaultHeight;

    private static final int CONTEXT_MENU_SELECT_URL_ID = 0;

    private static final int CONTEXT_MENU_SHARE_TRANSCRIPT_ID = 1;

    private static final int CONTEXT_MENU_SHARE_SELECTED_TEXT = 10;

    private static final int CONTEXT_MENU_AUTOFILL_USERNAME = 14;

    private static final int CONTEXT_MENU_AUTOFILL_PASSWORD = 2;

    private static final int CONTEXT_MENU_RESET_TERMINAL_ID = 3;

    private static final int CONTEXT_MENU_KILL_PROCESS_ID = 4;

    private static final int CONTEXT_MENU_STYLING_ID = 5;

    private static final int CONTEXT_SUBMENU_FONT_AND_COLOR_ID = 11;

    private static final int CONTEXT_SUBMENU_SET_BACKROUND_IMAGE_ID = 12;

    private static final int CONTEXT_SUBMENU_REMOVE_BACKGROUND_IMAGE_ID = 13;

    private static final int CONTEXT_MENU_TOGGLE_KEEP_SCREEN_ON = 6;

    private static final int CONTEXT_MENU_HELP_ID = 7;

    private static final int CONTEXT_MENU_SETTINGS_ID = 8;

    private static final int CONTEXT_MENU_REPORT_ID = 9;

    private static final String ARG_TERMINAL_TOOLBAR_TEXT_INPUT = "terminal_toolbar_text_input";

    private static final String ARG_ACTIVITY_RECREATED = "activity_recreated";

    private static final String LOG_TAG = "TermuxActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.logDebug(LOG_TAG, "onCreate");
        mIsOnResumeAfterOnCreate = true;
        if (savedInstanceState != null)
            mIsActivityRecreated = savedInstanceState.getBoolean(ARG_ACTIVITY_RECREATED, false);
        // Delete ReportInfo serialized object files from cache older than 14 days
        ReportActivity.deleteReportInfoFilesOlderThanXDays(this, 14, false);
        // Load Termux app SharedProperties from disk
        mProperties = TermuxAppSharedProperties.getProperties();
        reloadProperties();
        setActivityTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termux);
        // Load termux shared preferences
        // This will also fail if TermuxConstants.TERMUX_PACKAGE_NAME does not equal applicationId
        mPreferences = TermuxAppSharedPreferences.build(this, true);
        if (mPreferences == null) {
            // An AlertDialog should have shown to kill the app, so we don't continue running activity code
            mIsInvalidState = true;
            return;
        }
        setMargins();
        mTermuxActivityRootView = findViewById(R.id.activity_termux_root_view);
        mTermuxActivityRootView.setActivity(this);
        mTermuxActivityBottomSpaceView = findViewById(R.id.activity_termux_bottom_space_view);
        //mTermuxActivityRootView.setOnApplyWindowInsetsListener(new TermuxActivityRootView.WindowInsetsListener());
        /*View content = findViewById(android.R.id.content);
        content.setOnApplyWindowInsetsListener((v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets, v);
            mNavBarHeight = insetsCompat.getInsets(Type.systemBars()).bottom;
            return insetsCompat.toWindowInsets();
        });*/
        if (mProperties.isUsingFullScreen()) {
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
                insetsController.hide(WindowInsets.Type.navigationBars());
            }
        }
        // Must be done every time activity is created in order to registerForActivityResult,
        // Even if the logic of launching is based on user input.
        setBackgroundManager();
        setTermuxTerminalViewAndClients();
        setTerminalToolbarView(savedInstanceState);
        setSettingsButtonView();
        setNewSessionButtonView();
        setToggleKeyboardView();
        registerForContextMenu(mTerminalView);
        FileReceiverActivity.updateFileReceiverActivityComponentsState(this);
        try {
            // Start the {@link TermuxService} and make it run regardless of who is bound to it
            Intent serviceIntent = new Intent(this, TermuxService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            };

            // Attempt to bind to the service, this will call the {@link #onServiceConnected(ComponentName, IBinder)}
            // callback if it succeeds.
            if (!bindService(serviceIntent, this, 0))
                throw new RuntimeException("bindService() failed");
        } catch (Exception e) {
            Logger.logStackTraceWithMessage(LOG_TAG, "TermuxActivity failed to start TermuxService", e);
            Logger.showToast(this, getString(e.getMessage() != null && e.getMessage().contains("app is in background") ? R.string.error_termux_service_start_failed_bg : R.string.error_termux_service_start_failed_general), true);
            mIsInvalidState = true;
            return;
        }
        // Send the {@link TermuxConstants#BROADCAST_TERMUX_OPENED} broadcast to notify apps that Termux
        // app has been opened.
        TermuxUtils.sendTermuxOpenedBroadcast(this);
        verifyRWPermission();
        verifyAndroid11ManageFiles();
        configureDrawerLayout();

    /*
    View rootView = findViewById(R.id.terminal_toolbar_view_pager);
    //View rootView = findViewById(R.id.terminal_toolbar_container);
    //View toolbarGroup = findViewById(R.id.terminal_toolbar_group);
    
    // Paso 1: escuchar visibilidad del teclado
    ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
        boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
        int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
        Log.d("IME", "Visible: " + imeVisible + ", Height: " + imeHeight);
        return insets;
    });

    // Paso 2: animar sincronizado con el teclado
    ViewCompat.setWindowInsetsAnimationCallback(
        rootView,
        new WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {

            float startBottom;
            float endBottom;

            @Override
            public void onPrepare(@NonNull WindowInsetsAnimationCompat animation) {
                startBottom = rootView.getBottom();
                //startBottom = toolbarGroup.getBottom();
            }

            @NonNull
            @Override
            public WindowInsetsAnimationCompat.BoundsCompat onStart(
                    @NonNull WindowInsetsAnimationCompat animation,
                    @NonNull WindowInsetsAnimationCompat.BoundsCompat bounds) {
                endBottom = rootView.getBottom();
                //endBottom = toolbarGroup.getBottom();
                return bounds;
            }

            @NonNull
            @Override
            public WindowInsetsCompat onProgress(
                    @NonNull WindowInsetsCompat insets,
                    @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {

                for (WindowInsetsAnimationCompat anim : runningAnimations) {
                    if ((anim.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
                        float progress = anim.getInterpolatedFraction();
                        float offset = (startBottom - endBottom) * (1 - progress);
                        rootView.setTranslationY(offset);
                        //toolbarGroup.setTranslationY(offset); // Se mueven los 3 views juntos
                        break;
                    }
                }
                return insets;
            }

            @Override
            public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                rootView.setTranslationY(0); // Restablecer cuando termine la animación
            }
        }
    );
    */
View blurView = findViewById(R.id.extrakeys_backgroundblur);
View backgroundView = findViewById(R.id.extrakeys_background);
View pagerView = findViewById(R.id.terminal_toolbar_view_pager);

// Usamos uno de ellos como "ancla" para escuchar los insets
ViewCompat.setOnApplyWindowInsetsListener(pagerView, (v, insets) -> {
    boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
    int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
    Log.d("IME", "Visible: " + imeVisible + ", Height: " + imeHeight);
    return insets;
});

// Animación sincronizada para los 3 views
ViewCompat.setWindowInsetsAnimationCallback(
    pagerView,
    new WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP) {

        float startBottom;
        float endBottom;

        @Override
        public void onPrepare(@NonNull WindowInsetsAnimationCompat animation) {
            startBottom = pagerView.getBottom();
        }

        @NonNull
        @Override
        public WindowInsetsAnimationCompat.BoundsCompat onStart(
                @NonNull WindowInsetsAnimationCompat animation,
                @NonNull WindowInsetsAnimationCompat.BoundsCompat bounds) {
            endBottom = pagerView.getBottom();
            return bounds;
        }

        @NonNull
        @Override
        public WindowInsetsCompat onProgress(
                @NonNull WindowInsetsCompat insets,
                @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {

            for (WindowInsetsAnimationCompat anim : runningAnimations) {
                if ((anim.getTypeMask() & WindowInsetsCompat.Type.ime()) != 0) {
                    float progress = anim.getInterpolatedFraction();
                    float offset = (startBottom - endBottom) * (1 - progress);

                    blurView.setTranslationY(offset);
                    backgroundView.setTranslationY(offset);
                    pagerView.setTranslationY(offset);
                    break;
                }
            }
            return insets;
        }

        @Override
        public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
            blurView.setTranslationY(0);
            backgroundView.setTranslationY(0);
            pagerView.setTranslationY(0);
        }
    }
);








        


    }

    private void configureDrawerLayout() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(getColor(R.color.drawer_scrim_color));
        LinearLayout headerLayout = findViewById(R.id.drawer_header);
        headerLayout.setOnClickListener(v -> {
            getDrawer().closeDrawers();
            CustomDialogFragment dialog = new CustomDialogFragment();
            dialog.show(getSupportFragmentManager(), "customDialog");
        });
        headerLayout.setOnLongClickListener(view -> {
            String url = getString(R.string.developer_github);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)); 
            startActivity(intent);
            return true;
        });
        MaterialButton changeBackgroundButton = findViewById(R.id.change_background_button);
        changeBackgroundButton.setTypeface(null, Typeface.NORMAL);
        changeBackgroundButton.setOnClickListener(view -> {
            getDrawer().closeDrawers();
            mTermuxBackgroundManager.setBackgroundImage();
        });

        /*applyGradientToTextView(findViewById(R.id.drawer_header_name), new int[]{
            getColor(R.color.drawer_degrade_name_01),
            getColor(R.color.drawer_degrade_name_02),
            getColor(R.color.drawer_degrade_name_03)
        });*/
        /*ImageView verifyView = findViewById(R.id.drawer_header_verify);
        setAnimatedVector(verifyView, R.drawable.ic_shield_check_animated);*/
        ImageView sessionView = findViewById(R.id.new_session_button);
        setAnimatedVector(sessionView, R.drawable.ic_new_session_animated);
    }

    private void configureStatusBarBackgroundBlur() {
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        View existingBlur = decorView.findViewById(R.id.status_bar_blur);
        boolean isBlurEnabled = mPreferences.isStatusBarBlurEnabled();

        if (isBlurEnabled) {
            if (existingBlur == null) {
                LayoutInflater inflater = LayoutInflater.from(this);
                final View blurOverlay = inflater.inflate(R.layout.blur_statusbar_overlay, decorView, false);
                blurOverlay.setId(R.id.status_bar_blur);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        getStatusBarHeight()
                );
                layoutParams.gravity = Gravity.TOP;
                blurOverlay.setLayoutParams(layoutParams);
                decorView.addView(blurOverlay, 0);

                // Detectar cambios de altura si el status bar varía
                View rootView = decorView.getRootView();
                rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                int newHeight = getStatusBarHeight();
                                ViewGroup.LayoutParams lp = blurOverlay.getLayoutParams();
                                if (lp.height != newHeight) {
                                    lp.height = newHeight;
                                    blurOverlay.setLayoutParams(lp);
                                }
                            }
                        });
            } else {
                existingBlur.setVisibility(View.VISIBLE);
            }
        } else {
            if (existingBlur != null) {
                decorView.removeView(existingBlur);
            }
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        } else {
            // Fallback: 24dp en píxeles
            result = (int) (24 * getResources().getDisplayMetrics().density);
        }
        return result;
    }

    private void applyGradientToTextView(final TextView textView, final int[] colors) {
        textView.post(new Runnable() {
            @Override
            public void run() {
                float width = textView.getPaint().measureText(textView.getText().toString());
                Shader shader = new LinearGradient(
                    0, 0, width, 0, colors, null, Shader.TileMode.CLAMP
                );
                textView.getPaint().setShader(shader);
                textView.invalidate();
            }
        });
    }

    private void setAnimatedVector(ImageView imageView, int resId) {
        imageView.setImageResource(resId);
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof AnimatedVectorDrawable) {
            ((AnimatedVectorDrawable) drawable).start();
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        VibrationEffect vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE); // 50ms
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (vibrationEffect != null) {
                vibrator.vibrate(vibrationEffect);
            }
        } else {
            vibrator.vibrate(50);
        }
    }

    private void verifyRWPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE };
            ActivityCompat.requestPermissions(this, permissions, 1738);
        }
    }

    private void verifyAndroid11ManageFiles() {
        if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            i.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(i);
        }
    }

    private Map<String, String> readColorsFromPropertiesFile(String filePath) {
        Properties properties = new Properties();
        Map<String, String> colors = new HashMap<>();

        String defaultColor = String.format("#%08X", getColor(R.color.general_default_extrakeys_background));

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
            properties.load(reader);
            colors.put("extra-keys-background", getColorOrDefault(properties, "extra-keys-background", getAccentColor()));
            colors.put("sessions-background", getColorOrDefault(properties, "sessions-background", getAccentColor()));
            colors.put("status-bar-background", getColorOrDefault(properties, "status-bar-background", defaultColor));
        } catch (IOException e) {
            String defaultAccent = getAccentColor();
            colors.put("extra-keys-background", defaultAccent);
            colors.put("sessions-background", defaultAccent);
            colors.put("status-bar-background", defaultColor);
        }
        return colors;
    }

    private String getColorOrDefault(Properties props, String key, String defaultColor) {
        String value = props.getProperty(key);
        return (value != null ? value.trim() : defaultColor);
    }

    private String getAccentColor() {
        int colorRes = getResources().getColor(R.color.background_accent);
        return String.format("#%06X", (0xFFFFFF & colorRes));
    }

    private void applyDynamicUIConfigurations() {
        String filePath = "/data/data/com.termux/files/home/.termux/termux.properties";
        Map<String, String> colors = readColorsFromPropertiesFile(filePath);
        int extraKeysColor = Color.parseColor(colors.get("extra-keys-background"));
        int sessionsColor = Color.parseColor(colors.get("sessions-background"));
        int statusBarColor = Color.parseColor(colors.get("status-bar-background"));
        configureViewVisibility(R.id.terminal_monetbackground, mPreferences.isMonetBackgroundEnabled());
        configureBackgroundBlur(
            R.id.sessions_backgroundblur,
            R.id.sessions_background,
            sessionsColor,
            mPreferences.isSessionsBlurEnabled(),
            0.5f
        );
        configureExtraKeysBackground(extraKeysColor);
        getWindow().setStatusBarColor(statusBarColor);
        configureStatusBarBackgroundBlur();
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.logDebug(LOG_TAG, "onStart");
        if (mIsInvalidState) return;
        mIsVisible = true;
        if (mTermuxTerminalSessionActivityClient != null)
            mTermuxTerminalSessionActivityClient.onStart();
        if (mTermuxTerminalViewClient != null)
            mTermuxTerminalViewClient.onStart();
        if (mPreferences.isTerminalMarginAdjustmentEnabled())
            addTermuxActivityRootViewGlobalLayoutListener();
        applyDynamicUIConfigurations();
        registerTermuxActivityBroadcastReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.logVerbose(LOG_TAG, "onResume");
        if (mIsInvalidState) return;
        if (mTermuxTerminalSessionActivityClient != null)
            mTermuxTerminalSessionActivityClient.onResume();
        if (mTermuxTerminalViewClient != null)
            mTermuxTerminalViewClient.onResume();
        applyDynamicUIConfigurations();
        // Check if a crash happened on last run of the app or if a plugin crashed and show a
        // notification with the crash details if it did
        TermuxCrashUtils.notifyAppCrashFromCrashLogFile(this, LOG_TAG);
        mIsOnResumeAfterOnCreate = false;
    }

    private void configureViewVisibility(int viewId, boolean isVisible) {
        View view = findViewById(viewId);
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void configureBackgroundBlur(int blurViewId, int backgroundViewId, int sessionsColor, boolean isBlurEnabled, float alphaIfBlurred) {
        View blurView = findViewById(blurViewId);
        View backgroundView = findViewById(backgroundViewId);
        setBlurAndVisibility(blurView, backgroundView, isBlurEnabled, alphaIfBlurred);
        applyRoundedBackground(backgroundView, sessionsColor, 24f);
    }

    private void configureExtraKeysBackground(int extraKeysColor) {
        View extraKeysBackground = findViewById(R.id.extrakeys_background);
        View extraKeysBackgroundBlur = findViewById(R.id.extrakeys_backgroundblur);
        boolean isToolbarToggled = mPreferences.toogleShowTerminalToolbar();
        if (!isToolbarToggled) {
            extraKeysBackgroundBlur.setVisibility(View.GONE);
            extraKeysBackground.setVisibility(View.GONE);
            return;
        }
        boolean isBlurEnabled = mPreferences.isExtraKeysBlurEnabled();
        setBlurAndVisibility(extraKeysBackgroundBlur, extraKeysBackground, isBlurEnabled, 0.80f);
        applyRoundedBackground(extraKeysBackground, extraKeysColor, 24f);
    }

    private void applyRoundedBackground(View view, int color, float cornerRadius) {
        GradientDrawable roundedBackground = new GradientDrawable();
        roundedBackground.setColor(color);
        roundedBackground.setCornerRadius(cornerRadius);
        view.setBackground(roundedBackground);
    }

    private void setBlurAndVisibility(View blurView, View backgroundView, boolean isBlurEnabled, float blurredAlpha) {
        blurView.setVisibility(isBlurEnabled ? View.VISIBLE : View.GONE);
        backgroundView.setAlpha(isBlurEnabled ? blurredAlpha : 1.0f);
        backgroundView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.logDebug(LOG_TAG, "onStop");
        if (mIsInvalidState) return;
        mIsVisible = false;
        if (mTermuxTerminalSessionActivityClient != null)
            mTermuxTerminalSessionActivityClient.onStop();
        if (mTermuxTerminalViewClient != null)
            mTermuxTerminalViewClient.onStop();
        removeTermuxActivityRootViewGlobalLayoutListener();
        unregisterTermuxActivityBroadcastReceiver();
        getDrawer().closeDrawers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.logDebug(LOG_TAG, "onDestroy");
        if (mIsInvalidState) return;
        if (mTermuxService != null) {
            // Do not leave service and session clients with references to activity.
            mTermuxService.unsetTermuxTerminalSessionClient();
            mTermuxService = null;
        }
        try {
            unbindService(this);
        } catch (Exception e) {
            // ignore.
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        Logger.logVerbose(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(savedInstanceState);
        saveTerminalToolbarTextInput(savedInstanceState);
        savedInstanceState.putBoolean(ARG_ACTIVITY_RECREATED, true);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Logger.logVerbose(LOG_TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        mTermuxTerminalSessionActivityClient.onConfigurationChanged(newConfig);
    }

    /**
     * Part of the {@link ServiceConnection} interface. The service is bound with
     * {@link #bindService(Intent, ServiceConnection, int)} in {@link #onCreate(Bundle)} which will cause a call to this
     * callback method.
     */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        Logger.logDebug(LOG_TAG, "onServiceConnected");
        mTermuxService = ((TermuxService.LocalBinder) service).service;
        setTermuxSessionsListView();
        final Intent intent = getIntent();
        setIntent(null);
        if (mTermuxService.isTermuxSessionsEmpty()) {
            if (mIsVisible) {
                TermuxInstaller.setupBootstrapIfNeeded(TermuxActivity.this, () -> {
                    // Activity might have been destroyed.
                    if (mTermuxService == null)
                        return;
                    try {
                        boolean launchFailsafe = false;
                        if (intent != null && intent.getExtras() != null) {
                            launchFailsafe = intent.getExtras().getBoolean(TERMUX_ACTIVITY.EXTRA_FAILSAFE_SESSION, false);
                        }
                        mTermuxTerminalSessionActivityClient.addNewSession(launchFailsafe, null);
                    } catch (WindowManager.BadTokenException e) {
                        // Activity finished - ignore.
                    }
                });
            } else {
                // The service connected while not in foreground - just bail out.
                finishActivityIfNotFinishing();
            }
        } else {
            // If termux was started from launcher "New session" shortcut and activity is recreated,
            // then the original intent will be re-delivered, resulting in a new session being re-added
            // each time.
            if (!mIsActivityRecreated && intent != null && Intent.ACTION_RUN.equals(intent.getAction())) {
                // Android 7.1 app shortcut from res/xml/shortcuts.xml.
                boolean isFailSafe = intent.getBooleanExtra(TERMUX_ACTIVITY.EXTRA_FAILSAFE_SESSION, false);
                mTermuxTerminalSessionActivityClient.addNewSession(isFailSafe, null);
            } else {
                mTermuxTerminalSessionActivityClient.setCurrentSession(mTermuxTerminalSessionActivityClient.getCurrentStoredSessionOrLast());
            }
        }
        // Update the {@link TerminalSession} and {@link TerminalEmulator} clients.
        mTermuxService.setTermuxTerminalSessionClient(mTermuxTerminalSessionActivityClient);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Logger.logDebug(LOG_TAG, "onServiceDisconnected");
        // Respect being stopped from the {@link TermuxService} notification action.
        finishActivityIfNotFinishing();
    }

    private void reloadProperties() {
        mProperties.loadTermuxPropertiesFromDisk();
        if (mTermuxTerminalViewClient != null)
            mTermuxTerminalViewClient.onReloadProperties();
    }

    private void setActivityTheme() {
        // Update NightMode.APP_NIGHT_MODE
        TermuxThemeUtils.setAppNightMode(mProperties.getNightMode());
        // Set activity night mode. If NightMode.SYSTEM is set, then android will automatically
        // trigger recreation of activity when uiMode/dark mode configuration is changed so that
        // day or night theme takes affect.
        AppCompatActivityUtils.setNightMode(this, NightMode.getAppNightMode().getName(), true);
    }
    
    private void setMargins() {
        RelativeLayout relativeLayout = findViewById(R.id.activity_termux_root_relative_layout);
        int marginHorizontal = mProperties.getTerminalMarginHorizontal();
        int marginVertical = mProperties.getTerminalMarginVertical();
        ViewUtils.setLayoutMarginsInDp(relativeLayout, marginHorizontal, marginVertical, marginHorizontal, marginVertical);
    }

    public void addTermuxActivityRootViewGlobalLayoutListener() {
        getTermuxActivityRootView().getViewTreeObserver().addOnGlobalLayoutListener(getTermuxActivityRootView());
    }

    public void removeTermuxActivityRootViewGlobalLayoutListener() {
        if (getTermuxActivityRootView() != null)
            getTermuxActivityRootView().getViewTreeObserver().removeOnGlobalLayoutListener(getTermuxActivityRootView());
    }

    private void setTermuxTerminalViewAndClients() {
        // Set termux terminal view and session clients
        mTermuxTerminalSessionActivityClient = new TermuxTerminalSessionActivityClient(this);
        mTermuxTerminalViewClient = new TermuxTerminalViewClient(this, mTermuxTerminalSessionActivityClient);
        // Set termux terminal view
        mTerminalView = findViewById(R.id.terminal_view);
        mTerminalView.setTerminalViewClient(mTermuxTerminalViewClient);
        if (mTermuxTerminalViewClient != null)
            mTermuxTerminalViewClient.onCreate();
        if (mTermuxTerminalSessionActivityClient != null)
            mTermuxTerminalSessionActivityClient.onCreate();
    }

    private void setTermuxSessionsListView() {
        ListView termuxSessionsListView = findViewById(R.id.terminal_sessions_list);
        mTermuxSessionListViewController = new TermuxSessionsListViewController(this, mTermuxService.getTermuxSessions());
        termuxSessionsListView.setAdapter(mTermuxSessionListViewController);
        termuxSessionsListView.setOnItemClickListener(mTermuxSessionListViewController);
        termuxSessionsListView.setOnItemLongClickListener(mTermuxSessionListViewController);
    }

    private void setTerminalToolbarView(Bundle savedInstanceState) {
        mTermuxTerminalExtraKeys = new TermuxTerminalExtraKeys(this, mTerminalView, mTermuxTerminalViewClient, mTermuxTerminalSessionActivityClient, 0);
        mTermuxTerminalExtraKeys2 = new TermuxTerminalExtraKeys(this, mTerminalView, mTermuxTerminalViewClient, mTermuxTerminalSessionActivityClient, 1);
        final ViewPager terminalToolbarViewPager = getTerminalToolbarViewPager();
        if (mPreferences.shouldShowTerminalToolbar())
            terminalToolbarViewPager.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams layoutParams = terminalToolbarViewPager.getLayoutParams();
        mTerminalToolbarDefaultHeight = layoutParams.height;
        setTerminalToolbarHeight();
        String savedTextInput = null;
        if (savedInstanceState != null)
            savedTextInput = savedInstanceState.getString(ARG_TERMINAL_TOOLBAR_TEXT_INPUT);
        terminalToolbarViewPager.setAdapter(new TerminalToolbarViewPager.PageAdapter(this, savedTextInput));
        terminalToolbarViewPager.addOnPageChangeListener(new TerminalToolbarViewPager.OnPageChangeListener(this, terminalToolbarViewPager));
    }

    public void setTerminalToolbarHeight() {
        final ViewPager terminalToolbarViewPager = getTerminalToolbarViewPager();
        View extraKeysBackgroundBlur = findViewById(R.id.extrakeys_backgroundblur);
        View extraKeysBackground = findViewById(R.id.extrakeys_background);
        if (terminalToolbarViewPager == null)
            return;
        ViewGroup.LayoutParams layoutParams = terminalToolbarViewPager.getLayoutParams();

        int i = terminalToolbarViewPager.getCurrentItem();
        int matrix = 0;
        if (i == 0) {
            if (mTermuxTerminalExtraKeys.getExtraKeysInfo() != null)
            matrix = mTermuxTerminalExtraKeys.getExtraKeysInfo().getMatrix().length;
        } else {
            if (mTermuxTerminalExtraKeys2.getExtraKeysInfo() != null)
                matrix = mTermuxTerminalExtraKeys2.getExtraKeysInfo().getMatrix().length;
        }

        layoutParams.height = Math.round(mTerminalToolbarDefaultHeight * matrix * mProperties.getTerminalToolbarHeightScaleFactor());
        terminalToolbarViewPager.setLayoutParams(layoutParams);
        extraKeysBackground.setLayoutParams(layoutParams);
        extraKeysBackgroundBlur.setLayoutParams(layoutParams);
    }

    public void toggleTerminalToolbar() {
        ViewPager terminalToolbarViewPager = getTerminalToolbarViewPager();
        if (terminalToolbarViewPager == null) return;

        boolean showNow = mPreferences.toogleShowTerminalToolbar();
        Logger.showToast(this, showNow ? getString(R.string.msg_enabling_terminal_toolbar) : getString(R.string.msg_disabling_terminal_toolbar), true);

        updateViewVisibility(terminalToolbarViewPager, showNow);
        updateViewVisibility(R.id.extrakeys_backgroundblur, showNow);
        updateViewVisibility(R.id.extrakeys_background, showNow);

        isToolbarHidden = !showNow;

        if (showNow && isTerminalToolbarTextInputViewSelected()) {
            findViewById(R.id.terminal_toolbar_text_input).requestFocus();
        }
    }

    private void updateViewVisibility(int viewId, boolean isVisible) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setVisibility(isVisible ? View.VISIBLE : View.GONE);

            if (viewId == R.id.extrakeys_backgroundblur && isVisible) {
                boolean isBlurEnabled = mPreferences.isExtraKeysBlurEnabled();
                view.setVisibility(isBlurEnabled ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void updateViewVisibility(View view, boolean isVisible) {
        if (view != null) {
            view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    private void saveTerminalToolbarTextInput(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            return;
        final EditText textInputView = findViewById(R.id.terminal_toolbar_text_input);
        if (textInputView != null) {
            String textInput = textInputView.getText().toString();
            if (!textInput.isEmpty())
                savedInstanceState.putString(ARG_TERMINAL_TOOLBAR_TEXT_INPUT, textInput);
        }
    }

    private void setSettingsButtonView() {
        View settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            vibrate();
            ActivityUtils.startActivity(this, new Intent(this, SettingsActivity.class));
        });
    }

    private void setNewSessionButtonView() {
        View newSessionButton = findViewById(R.id.new_session_button);
        newSessionButton.setOnClickListener(v -> {
            vibrate();
            mTermuxTerminalSessionActivityClient.addNewSession(false, null);
        });
        newSessionButton.setOnLongClickListener(v -> {
            TextInputDialogUtils.textInput(TermuxActivity.this, R.string.title_create_named_session, null, R.string.action_create_named_session_confirm, text -> mTermuxTerminalSessionActivityClient.addNewSession(false, text), R.string.action_new_session_failsafe, text -> mTermuxTerminalSessionActivityClient.addNewSession(true, text), -1, null, null);
            return true;
        });
    }

    private void setToggleKeyboardView() {
        findViewById(R.id.toggle_keyboard_button).setOnClickListener(v -> {
            vibrate();
            mTermuxTerminalViewClient.onToggleSoftKeyboardRequest();
            getDrawer().closeDrawers();
        });
        findViewById(R.id.toggle_keyboard_button).setOnLongClickListener(v -> {
            toggleTerminalToolbar();
            return true;
        });
    }

    private void setBackgroundManager() {
        this.mTermuxBackgroundManager = new TermuxBackgroundManager(TermuxActivity.this);
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onBackPressed() {
        if (getDrawer().isDrawerOpen(Gravity.RIGHT)) {
            getDrawer().closeDrawers();
        //} else if (!getDrawer().isDrawerOpen(Gravity.RIGHT)) {
            //getDrawer().openDrawer(Gravity.RIGHT);
        } else {
            finishActivityIfNotFinishing();
        }
    }

    public void finishActivityIfNotFinishing() {
        // prevent duplicate calls to finish() if called from multiple places
        if (!TermuxActivity.this.isFinishing()) {
            if (mPreferences.isRemoveTaskOnActivityFinishEnabled())
                finishAndRemoveTask();
            else
                finish();
        }
    }

    /**
     * Show a toast and dismiss the last one if still visible.
     */
    public void showToast(String text, boolean longDuration) {
        if (text == null || text.isEmpty())
            return;
        if (mLastToast != null)
            mLastToast.cancel();
        mLastToast = Toast.makeText(TermuxActivity.this, text, longDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        mLastToast.setGravity(Gravity.TOP, 0, 0);
        mLastToast.show();
    }

    public void showContextPopupWindow(View anchorView) {
        TerminalSession session = getCurrentSession();
        if (session == null) return;

        boolean autoFillEnabled = mTerminalView.isAutoFillEnabled();
        String selectedText = mTerminalView.getStoredSelectedText();
        boolean hasSelectedText = selectedText != null && !selectedText.isEmpty();

        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.custom_context_menu, null);

        //popupView.setBackgroundResource(R.drawable.popup_background);
        int[] gradientColors = new int[] {
            getColor(R.color.general_dialog_border_01),
            getColor(R.color.general_dialog_border_02),
            getColor(R.color.general_dialog_border_03)
        };
        GradientDrawable border = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            gradientColors
        );
        border.setCornerRadius(48f);
        GradientDrawable background = new GradientDrawable();
        background.setColor(getColor(R.color.general_dialog_background));
        background.setCornerRadius(44f);
        Drawable[] layers = new Drawable[] { border, background };
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        layerDrawable.setLayerInset(1, 6, 6, 6, 6);
        popupView.setBackground(layerDrawable);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(16f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        MaterialButton selectUrlBtn = popupView.findViewById(R.id.select_url);
        selectUrlBtn.setTypeface(null, Typeface.NORMAL);
        selectUrlBtn.setOnClickListener(v -> {
            mTermuxTerminalViewClient.showUrlSelection();
            popupWindow.dismiss();
        });

        MaterialButton shareTranscriptBtn = popupView.findViewById(R.id.share_transcript);
        shareTranscriptBtn.setTypeface(null, Typeface.NORMAL);
        shareTranscriptBtn.setOnClickListener(v -> {
            mTermuxTerminalViewClient.shareSessionTranscript();
            popupWindow.dismiss();
        });

        MaterialButton shareSelected = popupView.findViewById(R.id.share_selected_text);
        shareSelected.setTypeface(null, Typeface.NORMAL);
        if (hasSelectedText) {
            shareSelected.setVisibility(View.VISIBLE);
            shareSelected.setOnClickListener(v -> {
                mTermuxTerminalViewClient.shareSelectedText();
                popupWindow.dismiss();
            });
        }

        MaterialButton autofillUsername = popupView.findViewById(R.id.autofill_username);
        MaterialButton autofillPassword = popupView.findViewById(R.id.autofill_password);
        autofillUsername.setTypeface(null, Typeface.NORMAL);
        autofillPassword.setTypeface(null, Typeface.NORMAL);
        if (autoFillEnabled) {
            autofillUsername.setVisibility(View.VISIBLE);
            autofillUsername.setOnClickListener(v -> {
                mTerminalView.requestAutoFillUsername();
                popupWindow.dismiss();
            });

            autofillPassword.setVisibility(View.VISIBLE);
            autofillPassword.setOnClickListener(v -> {
                mTerminalView.requestAutoFillPassword();
                popupWindow.dismiss();
            });
        }

        MaterialButton resetBtn = popupView.findViewById(R.id.reset_terminal);
        resetBtn.setTypeface(null, Typeface.NORMAL);
        resetBtn.setOnClickListener(v -> {
            onResetTerminalSession(session);
            popupWindow.dismiss();
        });

        MaterialButton killProcessBtn = popupView.findViewById(R.id.kill_process);
        killProcessBtn.setTypeface(null, Typeface.NORMAL);
        killProcessBtn.setText(getString(R.string.action_kill_process, session.getPid()));
        killProcessBtn.setEnabled(session.isRunning());
        killProcessBtn.setOnClickListener(v -> {
            showKillSessionDialog(session);
            popupWindow.dismiss();
        });

        MaterialButton fontColorBtn = popupView.findViewById(R.id.font_color);
        fontColorBtn.setTypeface(null, Typeface.NORMAL);
        fontColorBtn.setOnClickListener(v -> {
            showFontAndColorDialog();
            popupWindow.dismiss();
        });

        MaterialButton setBgImageBtn = popupView.findViewById(R.id.set_background_image);
        setBgImageBtn.setTypeface(null, Typeface.NORMAL);
        setBgImageBtn.setOnClickListener(v -> {
            mTermuxBackgroundManager.setBackgroundImage();
            popupWindow.dismiss();
        });

        MaterialButton removeBgImageBtn = popupView.findViewById(R.id.remove_background_image);
        removeBgImageBtn.setTypeface(null, Typeface.NORMAL);
        removeBgImageBtn.setOnClickListener(v -> {
            mTermuxBackgroundManager.removeBackgroundImage(true);
            popupWindow.dismiss();
        });

        SwitchMaterial keepScreenSwitch = popupView.findViewById(R.id.keep_screen_on);
        keepScreenSwitch.setTypeface(keepScreenSwitch.getTypeface(), Typeface.NORMAL);
        keepScreenSwitch.setTextColor(getColor(R.color.general_dialog_text_03));
        keepScreenSwitch.setChecked(mPreferences.shouldKeepScreenOn());
        keepScreenSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleKeepScreenOn());
        int[] thumbColors = new int[] {
            getColor(R.color.context_menu_switch_thumb_on),
            getColor(R.color.context_menu_switch_thumb_off)
        };
        int[] trackColors = new int[] {
            getColor(R.color.context_menu_switch_track_on),
            getColor(R.color.context_menu_switch_track_off)
        };
        int[][] states = new int[][] {
            new int[] { android.R.attr.state_checked },
            new int[] { -android.R.attr.state_checked }
        };
        ColorStateList thumbColorStateList = new ColorStateList(states, thumbColors);
        keepScreenSwitch.setThumbTintList(thumbColorStateList);
        ColorStateList trackColorStateList = new ColorStateList(states, trackColors);
        keepScreenSwitch.setTrackTintList(trackColorStateList);

        MaterialButton helpBtn = popupView.findViewById(R.id.help);
        helpBtn.setTypeface(null, Typeface.NORMAL);
        helpBtn.setOnClickListener(v -> {
            ActivityUtils.startActivity(this, new Intent(this, HelpActivity.class));
            popupWindow.dismiss();
        });

        MaterialButton settingsBtn = popupView.findViewById(R.id.settings);
        settingsBtn.setTypeface(null, Typeface.NORMAL);
        settingsBtn.setOnClickListener(v -> {
            ActivityUtils.startActivity(this, new Intent(this, SettingsActivity.class));
            popupWindow.dismiss();
        });

        MaterialButton reportBtn = popupView.findViewById(R.id.report);
        reportBtn.setTypeface(null, Typeface.NORMAL);
        reportBtn.setOnClickListener(v -> {
            mTermuxTerminalViewClient.reportIssueFromTranscript();
            popupWindow.dismiss();
        });

        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        showContextPopupWindow(findViewById(android.R.id.content));
    }

    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        TerminalSession currentSession = getCurrentSession();
        if (currentSession == null) return;

        boolean autoFillEnabled = mTerminalView.isAutoFillEnabled();

        menu.add(Menu.NONE, CONTEXT_MENU_SELECT_URL_ID, Menu.NONE, R.string.action_select_url);
        menu.add(Menu.NONE, CONTEXT_MENU_SHARE_TRANSCRIPT_ID, Menu.NONE, R.string.action_share_transcript);
        if (!DataUtils.isNullOrEmpty(mTerminalView.getStoredSelectedText()))
            menu.add(Menu.NONE, CONTEXT_MENU_SHARE_SELECTED_TEXT, Menu.NONE, R.string.action_share_selected_text);
        if (autoFillEnabled)
            menu.add(Menu.NONE, CONTEXT_MENU_AUTOFILL_USERNAME, Menu.NONE, R.string.action_autofill_username);
        if (autoFillEnabled)
            menu.add(Menu.NONE, CONTEXT_MENU_AUTOFILL_PASSWORD, Menu.NONE, R.string.action_autofill_password);
        menu.add(Menu.NONE, CONTEXT_MENU_RESET_TERMINAL_ID, Menu.NONE, R.string.action_reset_terminal);
        menu.add(Menu.NONE, CONTEXT_MENU_KILL_PROCESS_ID, Menu.NONE, getResources().getString(R.string.action_kill_process, getCurrentSession().getPid())).setEnabled(currentSession.isRunning());
        SubMenu subMenu = menu.addSubMenu(Menu.NONE, CONTEXT_MENU_STYLING_ID, Menu.NONE, R.string.action_style_terminal);
        subMenu.clearHeader();
        subMenu.add(SubMenu.NONE, CONTEXT_SUBMENU_FONT_AND_COLOR_ID, SubMenu.NONE, R.string.action_font_and_color);
        subMenu.add(SubMenu.NONE, CONTEXT_SUBMENU_SET_BACKROUND_IMAGE_ID, SubMenu.NONE, R.string.action_set_background_image);
        subMenu.add(SubMenu.NONE, CONTEXT_SUBMENU_REMOVE_BACKGROUND_IMAGE_ID, SubMenu.NONE, R.string.action_remove_background_image);
        menu.add(Menu.NONE, CONTEXT_MENU_TOGGLE_KEEP_SCREEN_ON, Menu.NONE, R.string.action_toggle_keep_screen_on).setCheckable(true).setChecked(mPreferences.shouldKeepScreenOn());
        menu.add(Menu.NONE, CONTEXT_MENU_HELP_ID, Menu.NONE, R.string.action_open_help);
        menu.add(Menu.NONE, CONTEXT_MENU_SETTINGS_ID, Menu.NONE, R.string.action_open_settings);
        menu.add(Menu.NONE, CONTEXT_MENU_REPORT_ID, Menu.NONE, R.string.action_report_issue);
    }*/

    /**
     * Hook system menu to show context menu instead.
     */
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mTerminalView.showContextMenu();
        return false;
    }*/

    /*@Override
    public boolean onContextItemSelected(MenuItem item) {
        TerminalSession session = getCurrentSession();
        switch(item.getItemId()) {
            case CONTEXT_MENU_SELECT_URL_ID:
                mTermuxTerminalViewClient.showUrlSelection();
                return true;
            case CONTEXT_MENU_SHARE_TRANSCRIPT_ID:
                mTermuxTerminalViewClient.shareSessionTranscript();
                return true;
            case CONTEXT_MENU_SHARE_SELECTED_TEXT:
                mTermuxTerminalViewClient.shareSelectedText();
                return true;
            case CONTEXT_MENU_AUTOFILL_USERNAME:
                mTerminalView.requestAutoFillUsername();
                return true;
            case CONTEXT_MENU_AUTOFILL_PASSWORD:
                mTerminalView.requestAutoFillPassword();
                return true;
            case CONTEXT_MENU_RESET_TERMINAL_ID:
                onResetTerminalSession(session);
                return true;
            case CONTEXT_MENU_KILL_PROCESS_ID:
                showKillSessionDialog(session);
                return true;
            case CONTEXT_SUBMENU_FONT_AND_COLOR_ID:
                showFontAndColorDialog();
                return true;
            case CONTEXT_SUBMENU_SET_BACKROUND_IMAGE_ID:
                mTermuxBackgroundManager.setBackgroundImage();
                return true;
            case CONTEXT_SUBMENU_REMOVE_BACKGROUND_IMAGE_ID:
                mTermuxBackgroundManager.removeBackgroundImage(true);
                return true;
            case CONTEXT_MENU_TOGGLE_KEEP_SCREEN_ON:
                toggleKeepScreenOn();
                return true;
            case CONTEXT_MENU_HELP_ID:
                ActivityUtils.startActivity(this, new Intent(this, HelpActivity.class));
                return true;
            case CONTEXT_MENU_SETTINGS_ID:
                ActivityUtils.startActivity(this, new Intent(this, SettingsActivity.class));
                return true;
            case CONTEXT_MENU_REPORT_ID:
                mTermuxTerminalViewClient.reportIssueFromTranscript();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }*/

    /*@Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        // onContextMenuClosed() is triggered twice if back button is pressed to dismiss instead of tap for some reason
        mTerminalView.onContextMenuClosed(menu);
    }*/

    private void showKillSessionDialog(TerminalSession session) {
        if (session == null)
            return;
        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setMessage(R.string.title_confirm_kill_process);
        b.setPositiveButton(android.R.string.ok, (dialog, id) -> {
            dialog.dismiss();
            session.finishIfRunning();
        });
        b.setNegativeButton(android.R.string.cancel, null);
        b.show();
    }

    private void onResetTerminalSession(TerminalSession session) {
        if (session != null) {
            session.reset();
            showToast(getResources().getString(R.string.msg_terminal_reset), true);
            if (mTermuxTerminalSessionActivityClient != null)
                mTermuxTerminalSessionActivityClient.onResetTerminalSession();
        }
    }

    private void showFontAndColorDialog() {
        Intent stylingIntent = new Intent();
        stylingIntent.setClassName(TermuxConstants.TERMUX_STYLING_PACKAGE_NAME, TermuxConstants.TERMUX_STYLING_APP.TERMUX_STYLING_ACTIVITY_NAME);
        try {
            startActivity(stylingIntent);
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            // The startActivity() call is not documented to throw IllegalArgumentException.
            // However, crash reporting shows that it sometimes does, so catch it here.
            new AlertDialog.Builder(this).setMessage(getString(R.string.error_styling_not_installed)).setPositiveButton(R.string.action_styling_install, (dialog, which) -> ActivityUtils.startActivity(this, new Intent(Intent.ACTION_VIEW, Uri.parse(TermuxConstants.TERMUX_STYLING_FDROID_PACKAGE_URL)))).setNegativeButton(android.R.string.cancel, null).show();
        }
    }

    private void toggleKeepScreenOn() {
        if (mTerminalView.getKeepScreenOn()) {
            mTerminalView.setKeepScreenOn(false);
            mPreferences.setKeepScreenOn(false);
        } else {
            mTerminalView.setKeepScreenOn(true);
            mPreferences.setKeepScreenOn(true);
        }
    }

    /**
     * For processes to access primary external storage (/sdcard, /storage/emulated/0, ~/storage/shared),
     * termux needs to be granted legacy WRITE_EXTERNAL_STORAGE or MANAGE_EXTERNAL_STORAGE permissions
     * if targeting targetSdkVersion 30 (android 11) and running on sdk 30 (android 11) and higher.
     */
    public void requestStoragePermission(boolean isPermissionCallback) {
        new Thread() {
            @Override
            public void run() {
                // Do not ask for permission again
                int requestCode = isPermissionCallback ? -1 : PermissionUtils.REQUEST_GRANT_STORAGE_PERMISSION;
                // If permission is granted, then also setup storage symlinks.
                if(PermissionUtils.checkAndRequestLegacyOrManageExternalStoragePermission(
                    TermuxActivity.this, requestCode, true, !isPermissionCallback)) {
                    if (isPermissionCallback)
                        Logger.logInfoAndShowToast(TermuxActivity.this, LOG_TAG, getString(com.termux.shared.R.string.msg_storage_permission_granted_on_request));
                    TermuxInstaller.setupStorageSymlinks(TermuxActivity.this);
                } else {
                    if (isPermissionCallback)
                        Logger.logInfoAndShowToast(TermuxActivity.this, LOG_TAG, getString(com.termux.shared.R.string.msg_storage_permission_not_granted_on_request));
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.logVerbose(LOG_TAG, "onActivityResult: requestCode: " + requestCode + ", resultCode: " + resultCode + ", data: " + IntentUtils.getIntentString(data));
        if (requestCode == PermissionUtils.REQUEST_GRANT_STORAGE_PERMISSION) {
            requestStoragePermission(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Logger.logVerbose(LOG_TAG, "onRequestPermissionsResult: requestCode: " + requestCode + ", permissions: " + Arrays.toString(permissions) + ", grantResults: " + Arrays.toString(grantResults));
        if (requestCode == PermissionUtils.REQUEST_GRANT_STORAGE_PERMISSION) {
            requestStoragePermission(true);
        }
    }

    public int getNavBarHeight() {
        return mNavBarHeight;
    }

    public TermuxActivityRootView getTermuxActivityRootView() {
        return mTermuxActivityRootView;
    }

    public View getTermuxActivityBottomSpaceView() {
        return mTermuxActivityBottomSpaceView;
    }

    public ExtraKeysView getExtraKeysView(int i) {
        if (i==0)
            return mExtraKeysView;
        else
            return mExtraKeysView2;
    }
    public ExtraKeysView getExtraKeysView() {
        int i = getTerminalToolbarViewPager().getCurrentItem();
        return getExtraKeysView(i);
    }

    public TermuxTerminalExtraKeys getTermuxTerminalExtraKeys(int i) {
        if (i==0)
            return mTermuxTerminalExtraKeys;
        else
            return mTermuxTerminalExtraKeys2;
    }

    public void setExtraKeysView(ExtraKeysView extraKeysView, int i) {
        if (i==0)
            mExtraKeysView = extraKeysView;
        else
            mExtraKeysView2 = extraKeysView;
    }

    public DrawerLayout getDrawer() {
        return (DrawerLayout) findViewById(R.id.drawer_layout);
    }

    public ViewPager getTerminalToolbarViewPager() {
        return (ViewPager) findViewById(R.id.terminal_toolbar_view_pager);
    }

    public float getTerminalToolbarDefaultHeight() {
        return mTerminalToolbarDefaultHeight;
    }

    public boolean isTerminalViewSelected() {
        return getTerminalToolbarViewPager().getCurrentItem() == 0;
    }

    public boolean isTerminalToolbarTextInputViewSelected() {
        return getTerminalToolbarViewPager().getCurrentItem() == 1;
    }

    public void termuxSessionListNotifyUpdated() {
        mTermuxSessionListViewController.notifyDataSetChanged();
    }

    public boolean isVisible() {
        return mIsVisible;
    }

    public boolean isOnResumeAfterOnCreate() {
        return mIsOnResumeAfterOnCreate;
    }

    public boolean isActivityRecreated() {
        return mIsActivityRecreated;
    }

    public TermuxService getTermuxService() {
        return mTermuxService;
    }

    public TerminalView getTerminalView() {
        return mTerminalView;
    }

    public TermuxTerminalViewClient getTermuxTerminalViewClient() {
        return mTermuxTerminalViewClient;
    }

    public TermuxTerminalSessionActivityClient getTermuxTerminalSessionClient() {
        return mTermuxTerminalSessionActivityClient;
    }

    @Nullable
    public TerminalSession getCurrentSession() {
        if (mTerminalView != null)
            return mTerminalView.getCurrentSession();
        else
            return null;
    }

    public TermuxAppSharedPreferences getPreferences() {
        return mPreferences;
    }

    public TermuxAppSharedProperties getProperties() {
        return mProperties;
    }

    public TermuxBackgroundManager getmTermuxBackgroundManager() {
        return mTermuxBackgroundManager;
    }

    public static void updateTermuxActivityStyling(Context context, boolean recreateActivity) {
        // Make sure that terminal styling is always applied.
        Intent stylingIntent = new Intent(TERMUX_ACTIVITY.ACTION_RELOAD_STYLE);
        stylingIntent.putExtra(TERMUX_ACTIVITY.EXTRA_RECREATE_ACTIVITY, recreateActivity);
        context.sendBroadcast(stylingIntent);
    }

    private void registerTermuxActivityBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TERMUX_ACTIVITY.ACTION_NOTIFY_APP_CRASH);
        intentFilter.addAction(TERMUX_ACTIVITY.ACTION_RELOAD_STYLE);
        intentFilter.addAction(TERMUX_ACTIVITY.ACTION_REQUEST_PERMISSIONS);

        if (Build.VERSION.SDK_INT >= 28 ) {
            registerReceiver(mTermuxActivityBroadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mTermuxActivityBroadcastReceiver, intentFilter);
        }
    }

    private void unregisterTermuxActivityBroadcastReceiver() {
        unregisterReceiver(mTermuxActivityBroadcastReceiver);
    }

    private void fixTermuxActivityBroadcastReceiverIntent(Intent intent) {
        if (intent == null)
            return;
        String extraReloadStyle = intent.getStringExtra(TERMUX_ACTIVITY.EXTRA_RELOAD_STYLE);
        if ("storage".equals(extraReloadStyle)) {
            intent.removeExtra(TERMUX_ACTIVITY.EXTRA_RELOAD_STYLE);
            intent.setAction(TERMUX_ACTIVITY.ACTION_REQUEST_PERMISSIONS);
        } else if ("apps-cache".equals(extraReloadStyle)) {
            TermuxInstaller.setupAppListCache(TermuxActivity.this);
        }
    }

    class TermuxActivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null)
                return;
            if (mIsVisible) {
                fixTermuxActivityBroadcastReceiverIntent(intent);
                switch(intent.getAction()) {
                    case TERMUX_ACTIVITY.ACTION_NOTIFY_APP_CRASH:
                        Logger.logDebug(LOG_TAG, "Received intent to notify app crash");
                        TermuxCrashUtils.notifyAppCrashFromCrashLogFile(context, LOG_TAG);
                        return;
                    case TERMUX_ACTIVITY.ACTION_RELOAD_STYLE:
                        Logger.logDebug(LOG_TAG, "Received intent to reload styling");
                        reloadActivityStyling(intent.getBooleanExtra(TERMUX_ACTIVITY.EXTRA_RECREATE_ACTIVITY, true));
                        return;
                    case TERMUX_ACTIVITY.ACTION_REQUEST_PERMISSIONS:
                        Logger.logDebug(LOG_TAG, "Received intent to request storage permissions");
                        requestStoragePermission(false);
                        return;
                    default:
                }
            }
        }
    }

    private void reloadActivityStyling(boolean recreateActivity) {
        if (mProperties != null) {
            reloadProperties();
            if (mExtraKeysView != null) {
                mExtraKeysView.setButtonTextAllCaps(mProperties.shouldExtraKeysTextBeAllCaps());
               mExtraKeysView.reload(mTermuxTerminalExtraKeys.getExtraKeysInfo(), mTerminalToolbarDefaultHeight);
            }
            if (mExtraKeysView2 != null) {
                mExtraKeysView2.setButtonTextAllCaps(mProperties.shouldExtraKeysTextBeAllCaps());
               mExtraKeysView2.reload(mTermuxTerminalExtraKeys2.getExtraKeysInfo(), mTerminalToolbarDefaultHeight);
            }
            // Update NightMode.APP_NIGHT_MODE
            TermuxThemeUtils.setAppNightMode(mProperties.getNightMode());
        }
        setMargins();
        setTerminalToolbarHeight();
        FileReceiverActivity.updateFileReceiverActivityComponentsState(this);
        if (mTermuxTerminalSessionActivityClient != null)
            mTermuxTerminalSessionActivityClient.onReloadActivityStyling();
        if (mTermuxTerminalViewClient != null)
            mTermuxTerminalViewClient.onReloadActivityStyling();
        // To change the activity and drawer theme, activity needs to be recreated.
        // It will destroy the activity, including all stored variables and views, and onCreate()
        // will be called again. Extra keys input text, terminal sessions and transcripts will be preserved.
        if (recreateActivity) {
            Logger.logDebug(LOG_TAG, "Recreating activity");
            TermuxActivity.this.recreate();
        }
    }

    public static void startTermuxActivity(@NonNull final Context context) {
        ActivityUtils.startActivity(context, newInstance(context));
    }

    public static Intent newInstance(@NonNull final Context context) {
        Intent intent = new Intent(context, TermuxActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
