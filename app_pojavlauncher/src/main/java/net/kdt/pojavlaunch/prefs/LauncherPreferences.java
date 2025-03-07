package net.kdt.pojavlaunch.prefs;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.P;
import static net.kdt.pojavlaunch.Architecture.is32BitsDevice;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;

import com.movtery.utils.UnpackJRE;
import com.movtery.utils.ZHTools;
import com.movtery.plugins.renderer.RendererPlugin;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.utils.JREUtils;

public class LauncherPreferences {
    public static final String PREF_KEY_CURRENT_PROFILE = "currentProfile";
    public static final String PREF_KEY_SKIP_NOTIFICATION_CHECK = "skipNotificationPermissionCheck";

    public static SharedPreferences DEFAULT_PREF;
    public static String PREF_RENDERER = "opengles2";
    public static String PREF_MESA_LIB = "default";
    public static String PREF_TURNIP_LIBS = "default";
    public static String PREF_DRIVER_MODEL = "gallium_zink";
    public static String PREF_BRIDGE_CONFIG = "default";
    public static String PREF_LOCAL_LOADER_OVERRIDE = "kgsl";
    public static String PREF_LIBGL_GL = "default";

    public static boolean PREF_VERTYPE_RELEASE = true;
    public static boolean PREF_VERTYPE_SNAPSHOT = false;
    public static boolean PREF_VERTYPE_OLDALPHA = false;
    public static boolean PREF_VERTYPE_OLDBETA = false;
    public static boolean PREF_HIDE_SIDEBAR = false;
    public static boolean PREF_IGNORE_NOTCH = false;
    public static int PREF_NOTCH_SIZE = 0;
    public static float PREF_BUTTONSIZE = 100f;
    public static float PREF_MOUSESCALE = 100f;
    public static int PREF_LONGPRESS_TRIGGER = 300;
    public static String PREF_DEFAULTCTRL_PATH = Tools.CTRLDEF_FILE;
    public static String PREF_CUSTOM_JAVA_ARGS;
    public static boolean PREF_FORCE_ENGLISH = false;
    public static final String PREF_VERSION_REPOS = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    public static boolean PREF_CHECK_LIBRARY_SHA = true;
    public static boolean PREF_DISABLE_GESTURES = false;
    public static boolean PREF_DISABLE_SWAP_HAND = false;
    public static float PREF_MOUSESPEED = 1f;
    public static int PREF_RAM_ALLOCATION;
    public static String PREF_DEFAULT_RUNTIME = "";
    public static boolean PREF_SUSTAINED_PERFORMANCE = false;
    public static boolean PREF_VIRTUAL_MOUSE_START = false;
    public static boolean PREF_ARC_CAPES = false;
    public static boolean PREF_USE_ALTERNATE_SURFACE = true;
    public static boolean PREF_JAVA_SANDBOX = true;
    public static int PREF_SCALE_FACTOR = 100;
    public static boolean PREF_ENABLE_GYRO = false;
    public static float PREF_GYRO_SENSITIVITY = 1f;
    public static int PREF_GYRO_SAMPLE_RATE = 16;
    public static boolean PREF_GYRO_SMOOTHING = true;

    public static boolean PREF_GYRO_INVERT_X = false;

    public static boolean PREF_GYRO_INVERT_Y = false;
    public static boolean PREF_FORCE_VSYNC = false;

    public static boolean PREF_BUTTON_ALL_CAPS = true;
    public static boolean PREF_DUMP_SHADERS = false;
    public static float PREF_DEADZONE_SCALE = 1f;
    public static boolean PREF_BIG_CORE_AFFINITY = false;
    public static boolean PREF_ZINK_PREFER_SYSTEM_DRIVER = false;

    public static boolean PREF_EXP_SETUP = false;

    public static boolean PREF_SPARE_FRAME_BUFFER = false;
    public static boolean PREF_EXP_ENABLE_SYSTEM = true;
    public static boolean PREF_EXP_ENABLE_SPECIFIC = false;
    public static boolean PREF_EXP_ENABLE_CUSTOM = false;
    public static boolean PREF_LOADER_OVERRIDE = false;
    public static boolean PREF_USE_DRM_SHIM = false;
    public static boolean FIX_Q3_BEHAVIOR = false;

    public static boolean PREF_VERIFY_MANIFEST = true;
    public static String PREF_DOWNLOAD_SOURCE = "default";
    public static boolean PREF_SKIP_NOTIFICATION_PERMISSION_CHECK = false;
    public static boolean PREF_VSYNC_IN_ZINK = true;

    public static String PREF_MESA_GL_VERSION;
    public static String PREF_MESA_GLSL_VERSION;
    public static boolean PREF_ENABLE_LOG_OUTPUT = false;
    public static boolean PREF_QUIT_LAUNCHER = true;
    public static boolean PREF_AUTOMATICALLY_SET_GAME_LANGUAGE = true;
    public static boolean PREF_GAME_LANGUAGE_OVERRIDDEN = false;
    public static String PREF_GAME_LANGUAGE = ZHTools.getSystemLanguage();

    public static void loadPreferences(Context ctx) {
        //Required for CTRLDEF_FILE and MultiRT
        Tools.initStorageConstants(ctx);

        if (!RendererPlugin.isInitialized()) RendererPlugin.initRenderers(ctx);

        PREF_RENDERER = DEFAULT_PREF.getString("renderer", "opengles2");
        PREF_BUTTONSIZE = DEFAULT_PREF.getInt("buttonscale", 100);
        PREF_MOUSESCALE = DEFAULT_PREF.getInt("mousescale", 100);
        PREF_MOUSESPEED = ((float) DEFAULT_PREF.getInt("mousespeed", 100)) / 100f;
        PREF_HIDE_SIDEBAR = DEFAULT_PREF.getBoolean("hideSidebar", false);
        PREF_IGNORE_NOTCH = DEFAULT_PREF.getBoolean("ignoreNotch", false);
        PREF_VERTYPE_RELEASE = DEFAULT_PREF.getBoolean("vertype_release", true);
        PREF_VERTYPE_SNAPSHOT = DEFAULT_PREF.getBoolean("vertype_snapshot", false);
        PREF_VERTYPE_OLDALPHA = DEFAULT_PREF.getBoolean("vertype_oldalpha", false);
        PREF_VERTYPE_OLDBETA = DEFAULT_PREF.getBoolean("vertype_oldbeta", false);
        PREF_LONGPRESS_TRIGGER = DEFAULT_PREF.getInt("timeLongPressTrigger", 300);
        PREF_DEFAULTCTRL_PATH = DEFAULT_PREF.getString("defaultCtrl", Tools.CTRLDEF_FILE);
        PREF_FORCE_ENGLISH = DEFAULT_PREF.getBoolean("force_english", false);
        PREF_CHECK_LIBRARY_SHA = DEFAULT_PREF.getBoolean("checkLibraries", true);
        PREF_DISABLE_GESTURES = DEFAULT_PREF.getBoolean("disableGestures", false);
        PREF_DISABLE_SWAP_HAND = DEFAULT_PREF.getBoolean("disableDoubleTap", false);
        PREF_RAM_ALLOCATION = DEFAULT_PREF.getInt("allocation", findBestRAMAllocation(ctx));
        PREF_CUSTOM_JAVA_ARGS = DEFAULT_PREF.getString("javaArgs", "");
        PREF_SUSTAINED_PERFORMANCE = DEFAULT_PREF.getBoolean("sustainedPerformance", false);
        PREF_VIRTUAL_MOUSE_START = DEFAULT_PREF.getBoolean("mouse_start", false);
        PREF_ARC_CAPES = DEFAULT_PREF.getBoolean("arc_capes", false);
        PREF_USE_ALTERNATE_SURFACE = DEFAULT_PREF.getBoolean("alternate_surface", false);
        PREF_JAVA_SANDBOX = DEFAULT_PREF.getBoolean("java_sandbox", true);
        PREF_SCALE_FACTOR = DEFAULT_PREF.getInt("resolutionRatio", 100);
        PREF_ENABLE_GYRO = DEFAULT_PREF.getBoolean("enableGyro", false);
        PREF_GYRO_SENSITIVITY = ((float) DEFAULT_PREF.getInt("gyroSensitivity", 100)) / 100f;
        PREF_GYRO_SAMPLE_RATE = DEFAULT_PREF.getInt("gyroSampleRate", 16);
        PREF_GYRO_SMOOTHING = DEFAULT_PREF.getBoolean("gyroSmoothing", true);
        PREF_GYRO_INVERT_X = DEFAULT_PREF.getBoolean("gyroInvertX", false);
        PREF_GYRO_INVERT_Y = DEFAULT_PREF.getBoolean("gyroInvertY", false);
        PREF_FORCE_VSYNC = DEFAULT_PREF.getBoolean("force_vsync", false);
        PREF_BUTTON_ALL_CAPS = DEFAULT_PREF.getBoolean("buttonAllCaps", true);
        PREF_DUMP_SHADERS = DEFAULT_PREF.getBoolean("dump_shaders", false);
        PREF_DEADZONE_SCALE = ((float) DEFAULT_PREF.getInt("gamepad_deadzone_scale", 100)) / 100f;
        PREF_BIG_CORE_AFFINITY = DEFAULT_PREF.getBoolean("bigCoreAffinity", false);
        PREF_ZINK_PREFER_SYSTEM_DRIVER = DEFAULT_PREF.getBoolean("zinkPreferSystemDriver", false);
        PREF_DOWNLOAD_SOURCE = DEFAULT_PREF.getString("downloadSource", "default");
        PREF_VERIFY_MANIFEST = DEFAULT_PREF.getBoolean("verifyManifest", true);
        PREF_SKIP_NOTIFICATION_PERMISSION_CHECK = DEFAULT_PREF.getBoolean(PREF_KEY_SKIP_NOTIFICATION_CHECK, false);
        PREF_VSYNC_IN_ZINK = DEFAULT_PREF.getBoolean("vsync_in_zink", true);

        PREF_BRIDGE_CONFIG = DEFAULT_PREF.getString("configBridge", "default");
        PREF_SPARE_FRAME_BUFFER = DEFAULT_PREF.getBoolean("SpareFrameBuffer", false);
        PREF_EXP_ENABLE_SYSTEM = DEFAULT_PREF.getBoolean("ebSystem", true);
        PREF_EXP_ENABLE_SPECIFIC = DEFAULT_PREF.getBoolean("ebSpecific", false);
        PREF_EXP_ENABLE_CUSTOM = DEFAULT_PREF.getBoolean("ebCustom", false);
        PREF_LOADER_OVERRIDE = DEFAULT_PREF.getBoolean("ebChooseMldo", false);
        PREF_USE_DRM_SHIM = DEFAULT_PREF.getBoolean("ebDrmShim", false);
        FIX_Q3_BEHAVIOR = DEFAULT_PREF.getBoolean("q3behavior", false);

        PREF_EXP_SETUP = DEFAULT_PREF.getBoolean("ExperimentalSetup", false);
        PREF_MESA_LIB = DEFAULT_PREF.getString("CMesaLibrary", "default");
        PREF_TURNIP_LIBS = DEFAULT_PREF.getString("chooseTurnipDriver", "default");
        PREF_DRIVER_MODEL = DEFAULT_PREF.getString("CDriverModels", "gallium_zink");
        PREF_LOCAL_LOADER_OVERRIDE = DEFAULT_PREF.getString("ChooseMldo", "kgsl");
        PREF_LIBGL_GL = DEFAULT_PREF.getString("CLibglGL", "default");

        PREF_MESA_GL_VERSION = DEFAULT_PREF.getString("mesaGLVersion", "4.6");
        PREF_MESA_GLSL_VERSION = DEFAULT_PREF.getString("mesaGLSLVersion", "460");

        PREF_ENABLE_LOG_OUTPUT = DEFAULT_PREF.getBoolean("enableLogOutput", false);
        PREF_QUIT_LAUNCHER = DEFAULT_PREF.getBoolean("quitLauncher", true);
        PREF_AUTOMATICALLY_SET_GAME_LANGUAGE = DEFAULT_PREF.getBoolean("autoSetGameLanguage", true);
        PREF_GAME_LANGUAGE_OVERRIDDEN = DEFAULT_PREF.getBoolean("gameLanguageOverridden", false);
        PREF_GAME_LANGUAGE = DEFAULT_PREF.getString("setGameLanguage", ZHTools.getSystemLanguage());

        String argLwjglLibname = "-Dorg.lwjgl.opengl.libname=";
        for (String arg : JREUtils.parseJavaArguments(PREF_CUSTOM_JAVA_ARGS)) {
            if (arg.startsWith(argLwjglLibname)) {
                // purge arg
                DEFAULT_PREF.edit().putString("javaArgs",
                        PREF_CUSTOM_JAVA_ARGS.replace(arg, "")).apply();
            }
        }
        reloadRuntime();
    }

    public static void reloadRuntime() {
        if (DEFAULT_PREF.contains("defaultRuntime")) {
            PREF_DEFAULT_RUNTIME = DEFAULT_PREF.getString("defaultRuntime", "");
        } else if (!MultiRTUtils.getRuntimes().isEmpty()) {
            PREF_DEFAULT_RUNTIME = UnpackJRE.InternalRuntime.JRE_8.name;
            LauncherPreferences.DEFAULT_PREF.edit().putString("defaultRuntime", PREF_DEFAULT_RUNTIME).apply();
        }
    }

    /**
     * This functions aims at finding the best default RAM amount,
     * according to the RAM amount of the physical device.
     * Put not enough RAM ? Minecraft will lag and crash.
     * Put too much RAM ?
     * The GC will lag, android won't be able to breathe properly.
     *
     * @param ctx Context needed to get the total memory of the device.
     * @return The best default value found.
     */
    private static int findBestRAMAllocation(Context ctx) {
        int deviceRam = Tools.getTotalDeviceMemory(ctx);
        if (deviceRam < 1024) return 300;
        if (deviceRam < 1536) return 450;
        if (deviceRam < 2048) return 600;
        // Limit the max for 32 bits devices more harshly
        if (is32BitsDevice()) return 700;

        if (deviceRam < 3064) return 936;
        if (deviceRam < 4096) return 1148;
        if (deviceRam < 6144) return 1536;
        return 2048; //Default RAM allocation for 64 bits
    }

    /**
     * Compute the notch size to avoid being out of bounds
     */
    public static void computeNotchSize(Activity activity) {
        if (Build.VERSION.SDK_INT < P) return;
        try {
            final Rect cutout;
            if (SDK_INT >= Build.VERSION_CODES.S) {
                cutout = activity.getWindowManager().getCurrentWindowMetrics().getWindowInsets().getDisplayCutout().getBoundingRects().get(0);
            } else {
                cutout = activity.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout().getBoundingRects().get(0);
            }
            // Notch values are rotation sensitive, handle all cases
            int orientation = activity.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT)
                LauncherPreferences.PREF_NOTCH_SIZE = cutout.height();
            else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                LauncherPreferences.PREF_NOTCH_SIZE = cutout.width();
            else LauncherPreferences.PREF_NOTCH_SIZE = Math.min(cutout.width(), cutout.height());
        } catch (Exception e) {
            Log.i("NOTCH DETECTION", "No notch detected, or the device if in split screen mode");
            LauncherPreferences.PREF_NOTCH_SIZE = -1;
        }
        Tools.updateWindowSize(activity);
    }
}
