package pojlib.account;

import android.app.Activity;

import com.google.gson.Gson;

import org.json.JSONException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.annotation.Nullable;

import pojlib.util.Constants;
import pojlib.util.GsonUtils;
import pojlib.util.Logger;
import pojlib.util.MSAException;

public class MinecraftAccount {
    public String accessToken;
    public String clientToken;
    public String uuid;
    public String username;
    public boolean isDemoMode = false;
    public long expiresOn;
    public String userType = "legacy";

    public static MinecraftAccount login(Activity activity, String gameDir, String msToken) throws MSAException, IOException, JSONException {
        if (msToken == null || msToken.equals("0") || msToken.trim().isEmpty()) {
            MinecraftAccount account = new MinecraftAccount();
            account.username = "ray_gamign";
            account.uuid = "51ac51b2-211c-3e46-97d8-2cd8491645b0";
            account.accessToken = "0";
            account.clientToken = "0";
            account.userType = "legacy";
            account.isDemoMode = false;
            account.expiresOn = Long.MAX_VALUE;

            GsonUtils.objectToJsonFile(gameDir + "/" + account.uuid + ".json", account);
            return account;
        }

        Msa instance = new Msa(activity);
        MinecraftAccount account = instance.performLogin(msToken);

        GsonUtils.objectToJsonFile(gameDir + "/" + account.uuid + ".json", account);
        return account;
    }

    public static boolean removeAccount(Activity activity, String uuid) {
        File accountFile = new File(Constants.ACCOUNTS_DIR + "/" + uuid + ".json");
        return !accountFile.exists() || accountFile.delete();
    }

    //Try this before using login - the account will have been saved to disk if previously logged in
    public static MinecraftAccount load(String path, String uuid) {
        return GsonUtils.jsonFileToObject(path + "/" + uuid + ".json", MinecraftAccount.class);
    }

    public static String getSkinFaceUrl(MinecraftAccount account) {
        if (account == null) return null;
        if (account.isDemoMode) {
            return Constants.MINOTAR_URL + "/helm/MHF_Steve";
        } else {
            try {
                return Constants.MINOTAR_URL + "/helm/" + account.uuid;
            } catch (NullPointerException e) {
                Logger.getInstance().appendToLog("Username likely not set! Please set your username at Minecraft.net and try again. | " + e);
                return null;
            }
        }
    }
}