package pojlib.account;

import android.app.Activity;
import android.util.Log;

import com.microsoft.aad.msal4j.DeviceCode;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import pojlib.API;
import pojlib.util.Constants;
import pojlib.util.GsonUtils;
import pojlib.util.Logger;
import pojlib.util.MSAException;

public class LoginHelper {
    public static final Set<String> SCOPES;
    private static Thread loginThread;
    private static PublicClientApplication pca;

    static {
        try {
            File cache = new File(Constants.USER_HOME + "/cache_data/serialized_cache.json");
            if(!cache.exists()) {
                cache.getParentFile().mkdirs();
                cache.createNewFile();
            }

            BufferedReader reader = new BufferedReader(new FileReader(cache.getPath()));
            String dataToInitCache = reader.readLine();
            reader.close();

            TokenPersistence persistenceAspect = new TokenPersistence(dataToInitCache, cache);

            pca = PublicClientApplication.builder("d17a73a2-707c-40f5-8c90-d3eda0956f10")
                    .setTokenCacheAccessAspect(persistenceAspect)
                    .authority("https://login.microsoftonline.com/consumers/")
                    .build();
        } catch (IOException e) {
            Logger.getInstance().appendToLog("Unable to initialize auth cache and/or application! | " + e);
        }

        SCOPES = new HashSet<>();
        SCOPES.add("XboxLive.SignIn");
        SCOPES.add("XboxLive.offline_access");
    }

    public static MinecraftAccount refreshAccount(Activity activity, String uuid) {
        if (pca == null) {
            return MinecraftAccount.load(Constants.ACCOUNTS_DIR, uuid);
        }
        Set<IAccount> accountsInCache = pca.getAccounts().join();
        IAuthenticationResult result;
        try {
            for (IAccount account : accountsInCache) {
                SilentParameters silentParameters =
                        SilentParameters
                                .builder(SCOPES, account)
                                .build();

                result = pca.acquireTokenSilently(silentParameters).join();
                MinecraftAccount acc = new Msa(activity).performLogin(result.accessToken());
                GsonUtils.objectToJsonFile(Constants.ACCOUNTS_DIR + "/" + acc.uuid + ".json", acc);
                if (!acc.uuid.equals(uuid)) {
                    continue;
                }
                return acc;
            }
            return MinecraftAccount.load(Constants.ACCOUNTS_DIR, uuid);
        } catch (Exception ex) {
            Logger.getInstance().appendToLog("Couldn't refresh token! " + ex);
            return MinecraftAccount.load(Constants.ACCOUNTS_DIR, uuid);
        }
    }

    public static void login(Activity activity) {
        loginThread = new Thread(() -> {
            try {
                MinecraftAccount account = MinecraftAccount.load(Constants.ACCOUNTS_DIR, "e72b3018-8a1a-38a8-9ded-b94ea7012936");
                if (account == null) {
                    account = new MinecraftAccount();
                    account.username = "ray_gamign";
                    account.uuid = "51ac51b2-211c-3e46-97d8-2cd8491645b0";
                    account.accessToken = "0";
                    account.clientToken = "0";
                    account.userType = "legacy";
                    account.isDemoMode = false;
                    account.expiresOn = Long.MAX_VALUE;
                    GsonUtils.objectToJsonFile(Constants.ACCOUNTS_DIR + "/" + account.uuid + ".json", account);
                }
                API.currentAcc = account;
                API.profileName = API.currentAcc.username;
                API.profileUUID = API.currentAcc.uuid;
                API.profileImage = MinecraftAccount.getSkinFaceUrl(API.currentAcc);
                API.isDemoMode = API.currentAcc.isDemoMode;
                API.msaMessage = "";
            } catch (Exception e) {
                Logger.getInstance().appendToLog("OfflineLogin | Failed to provision offline account: " + e);
                API.msaMessage = "OfflineLogin | Failed to provision account.";
            }
        });

        loginThread.start();
    }
}