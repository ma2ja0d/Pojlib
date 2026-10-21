package pojlib.util;

import android.app.Activity;

public class Constants {

    public static final String MOJANG_META_URL = "https://piston-meta.mojang.com";

    public static final String MOJANG_RESOURCES_URL = "https://resources.download.minecraft.net";

    public static final String FABRIC_META_URL = "https://meta.fabricmc.net/v2";

    public static final String QUILT_META_URL = "https://meta.quiltmc.org/v3";

    public static final String OAUTH_TOKEN_URL = "https://login.live.com/oauth20_token.srf";

    public static final String XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";

    public static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";

    public static final String MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";

    public static final String MC_STORE_URL = "https://api.minecraftservices.com/entitlements/mcstore";

    public static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    public static final String MINOTAR_URL = "https://minotar.net";
    public static final String GIT_BRANCH = "QuestCraft-6.0.1";
    public static String USER_HOME;

    // ---- Relocated "internal files" (formerly /data/user/0/<pkg>/files) ----
    // These now live under USER_HOME/app_files, reachable without root
    // (SideQuest / adb / MTP): /sdcard/Android/data/<pkg>/files/app_files
    public static String APP_FILES;     // root of the relocated folder
    public static String ACCOUNTS_DIR;  // saved Microsoft/Minecraft account files
    public static String MG_DIR;        // MobileGLUES renderer config/cache (MG_DIR_PATH)
    public static String RUNTIME_DIR;   // Java runtime (JRE)

    // MobileGLUES config + shader cache. External storage can be slower than internal;
    // set false to keep it internal if you notice stutter.
    public static final boolean MG_ON_EXTERNAL = true;

    // The JRE contains native code (libjvm.so etc.) that Android normally refuses
    // to load from external storage. Leave this false unless you have tested it.
    public static final boolean RUNTIME_ON_EXTERNAL = false;

    public static void initConstants(Activity activity) {
        USER_HOME = activity.getExternalFilesDir(null).getAbsolutePath();

        String internal = activity.getFilesDir().getAbsolutePath();
        APP_FILES = USER_HOME + "/app_files";
        ACCOUNTS_DIR = APP_FILES + "/accounts";
        MG_DIR = (MG_ON_EXTERNAL ? APP_FILES : internal) + "/mg";
        RUNTIME_DIR = (RUNTIME_ON_EXTERNAL ? APP_FILES : internal) + "/runtimes/JRE";

        new java.io.File(ACCOUNTS_DIR).mkdirs();
        migrateOld(internal + "/accounts", ACCOUNTS_DIR);
        if (MG_ON_EXTERNAL) {
            new java.io.File(MG_DIR).mkdirs();
            migrateOld(internal + "/mg", MG_DIR);
        }
    }

    // One-time copy of files saved by the stock build so you keep your login/settings.
    private static void migrateOld(String oldDir, String newDir) {
        try {
            java.io.File[] files = new java.io.File(oldDir).listFiles();
            if (files == null) return;
            for (java.io.File f : files) {
                if (!f.isFile()) continue;
                java.io.File dst = new java.io.File(newDir, f.getName());
                if (!dst.exists()) {
                    java.nio.file.Files.copy(f.toPath(), dst.toPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}