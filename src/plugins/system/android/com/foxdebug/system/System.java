package com.foxdebug.system;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings.Global;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import androidx.core.content.FileProvider;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import com.foxdebug.system.Ui.Theme;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class System extends CordovaPlugin {

  private CallbackContext requestPermissionCallback;
  private Activity activity;
  private Context context;
  private int REQ_PERMISSIONS = 1;
  private int REQ_PERMISSION = 2;
  private int systemBarColor = 0xFF000000;
  private Theme theme;
  private CallbackContext intentHandler;
  private CordovaWebView webView;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    this.context = cordova.getContext();
    this.activity = cordova.getActivity();
    this.webView = webView;
  }

  public boolean execute(
    String action,
    final JSONArray args,
    final CallbackContext callbackContext
  ) throws JSONException {
    final String arg1 = args.optString(0);
    final String arg2 = args.optString(1);
    final String arg3 = args.optString(2);
    final String arg4 = args.optString(3);
    final String arg5 = args.optString(4);
    final String arg6 = args.optString(5);

    switch (action) {
      case "get-webkit-info":
      case "file-action":
      case "is-powersave-mode":
      case "get-app-info":
      case "add-shortcut":
      case "remove-shortcut":
      case "pin-shortcut":
      case "get-android-version":
      case "request-permissions":
      case "request-permission":
      case "has-permission":
      case "open-in-browser":
      case "launch-app":
      case "get-global-setting":
      case "get-available-encodings":
      case "decode":
      case "encode":
        break;
      case "get-configuration":
        getConfiguration(callbackContext);
        return true;
      case "set-input-type":
        setInputType(arg1);
        callbackContext.success();
        return true;
      case "get-cordova-intent":
        getCordovaIntent(callbackContext);
        return true;
      case "set-intent-handler":
        setIntentHandler(callbackContext);
        return true;
      case "set-ui-theme":
        this.cordova.getActivity()
          .runOnUiThread(
            new Runnable() {
              public void run() {
                setUiTheme(arg1, args.optJSONObject(1), callbackContext);
              }
            }
          );
        return true;
      case "clear-cache":
        this.cordova.getActivity()
          .runOnUiThread(
            new Runnable() {
              public void run() {
                clearCache(callbackContext);
              }
            }
          );
        return true;
      default:
        return false;
    }

    cordova
      .getThreadPool()
      .execute(
        new Runnable() {
          public void run() {
            switch (action) {
              case "get-webkit-info":
                getWebkitInfo(callbackContext);
                break;
              case "file-action":
                fileAction(arg1, arg2, arg3, arg4, callbackContext);
                break;
              case "is-powersave-mode":
                isPowerSaveMode(callbackContext);
                break;
              case "get-app-info":
                getAppInfo(callbackContext);
                break;
              case "add-shortcut":
                addShortcut(
                  arg1,
                  arg2,
                  arg3,
                  arg4,
                  arg5,
                  arg6,
                  callbackContext
                );
                break;
              case "remove-shortcut":
                removeShortcut(arg1, callbackContext);
                break;
              case "pin-shortcut":
                pinShortcut(arg1, callbackContext);
                break;
              case "get-android-version":
                getAndroidVersion(callbackContext);
                break;
              case "request-permissions":
                requestPermissions(args.optJSONArray(0), callbackContext);
                break;
              case "request-permission":
                requestPermission(arg1, callbackContext);
                break;
              case "has-permission":
                hasPermission(arg1, callbackContext);
                break;
              case "open-in-browser":
                openInBrowser(arg1, callbackContext);
                break;
              case "launch-app":
                launchApp(arg1, arg2, arg3, callbackContext);
                break;
              case "get-global-setting":
                getGlobalSetting(arg1, callbackContext);
                break;
              case "get-available-encodings":
                getAvailableEncodings(callbackContext);
                break;
              case "decode":
                decode(arg1, arg2, callbackContext);
                break;
              case "encode":
                encode(arg1, arg2, callbackContext);
                break;
              default:
                break;
            }
          }
        }
      );

    return true;
  }

  private void getConfiguration(CallbackContext callback) {
    try {
      JSONObject result = new JSONObject();
      Configuration config = context.getResources().getConfiguration();
      InputMethodManager imm = (InputMethodManager) context.getSystemService(
        Context.INPUT_METHOD_SERVICE
      );
      Method method =
        InputMethodManager.class.getMethod("getInputMethodWindowVisibleHeight");

      result.put("isAcceptingText", imm.isAcceptingText());
      result.put("keyboardHeight", method.invoke(imm));
      result.put("locale", config.locale.toString());
      result.put("fontScale", config.fontScale);
      result.put("keyboard", config.keyboard);
      result.put("keyboardHidden", config.keyboardHidden);
      result.put("hardKeyboardHidden", config.hardKeyboardHidden);
      result.put("navigationHidden", config.navigationHidden);
      result.put("navigation", config.navigation);
      result.put("orientation", config.orientation);
      callback.success(result);
    } catch (
      JSONException
      | NoSuchMethodException
      | IllegalAccessException
      | InvocationTargetException error
    ) {
      callback.error(error.toString());
    }
  }

  private void decode(
    String content, // base64 encoded string
    String charSetName,
    CallbackContext callback
  ) {
    try {
      byte[] bytes = Base64.decode(content, Base64.DEFAULT);

      if (Charset.isSupported(charSetName) == false) {
        callback.error("Charset not supported: " + charSetName);
        return;
      }

      Charset charSet = Charset.forName(charSetName);
      CharBuffer charBuffer = charSet.decode(ByteBuffer.wrap(bytes));
      String result = String.valueOf(charBuffer);
      callback.success(result);
    } catch (Exception e) {
      callback.error(e.toString());
    }
  }

  private void encode(
    String content, // string to encode
    String charSetName,
    CallbackContext callback
  ) {
    try {
      if (Charset.isSupported(charSetName) == false) {
        callback.error("Charset not supported: " + charSetName);
        return;
      }

      Charset charSet = Charset.forName(charSetName);
      ByteBuffer byteBuffer = charSet.encode(content);
      byte[] bytes = new byte[byteBuffer.remaining()];
      byteBuffer.get(bytes);
      callback.success(bytes);
    } catch (Exception e) {
      callback.error(e.toString());
    }
  }

  private void getAvailableEncodings(CallbackContext callback) {
    try {
      Map<String, Charset> charsets = Charset.availableCharsets();
      JSONObject result = new JSONObject();
      for (Map.Entry<String, Charset> entry : charsets.entrySet()) {
        JSONObject obj = new JSONObject();
        Charset charset = entry.getValue();
        obj.put("label", charset.displayName());
        obj.put("aliases", new JSONArray(charset.aliases()));
        obj.put("name", charset.name());
        result.put(charset.name(), obj);
      }
      callback.success(result);
    } catch (Exception e) {
      callback.error(e.toString());
    }
  }

  private void requestPermissions(JSONArray arr, CallbackContext callback) {
    try {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        int[] res = new int[arr.length()];
        for (int i = 0; i < res.length; ++i) {
          res[i] = 1;
        }
        callback.success(1);
        return;
      }

      String[] permissions = checkPermissions(arr);

      if (permissions.length > 0) {
        requestPermissionCallback = callback;
        cordova.requestPermissions(this, REQ_PERMISSIONS, permissions);
        return;
      }
      callback.success(new JSONArray());
    } catch (Exception e) {
      callback.error(e.toString());
    }
  }

  private void requestPermission(String permission, CallbackContext callback) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      callback.success(1);
      return;
    }

    if (permission != null || !permission.equals("")) {
      if (!cordova.hasPermission(permission)) {
        requestPermissionCallback = callback;
        cordova.requestPermission(this, REQ_PERMISSION, permission);
        return;
      }

      callback.success(1);
      return;
    }

    callback.error("No permission passed to request.");
  }

  private void hasPermission(String permission, CallbackContext callback) {
    if (permission != null || !permission.equals("")) {
      int res = 0;
      if (cordova.hasPermission(permission)) {
        res = 1;
      }

      callback.success(res);
      return;
    }
    callback.error("No permission passed to check.");
  }

  public void onRequestPermissionResult(
    int code,
    String[] permissions,
    int[] resCodes
  ) {
    if (requestPermissionCallback == null) return;

    if (code == REQ_PERMISSIONS) {
      JSONArray resAr = new JSONArray();
      for (int res : resCodes) {
        if (res == PackageManager.PERMISSION_DENIED) {
          resAr.put(0);
        }
        resAr.put(1);
      }

      requestPermissionCallback.success(resAr);
      requestPermissionCallback = null;
      return;
    }

    if (
      resCodes.length >= 1 && resCodes[0] == PackageManager.PERMISSION_DENIED
    ) {
      requestPermissionCallback.success(0);
      requestPermissionCallback = null;
      return;
    }
    requestPermissionCallback.success(1);
    requestPermissionCallback = null;
    return;
  }

  private String[] checkPermissions(JSONArray arr) throws Exception {
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < arr.length(); i++) {
      try {
        String permission = arr.getString(i);
        if (permission != null || !permission.equals("")) {
          throw new Exception("Permission cannot be null or empty");
        }
        if (!cordova.hasPermission(permission)) {
          list.add(permission);
        }
      } catch (JSONException e) {}
    }

    String[] res = new String[list.size()];
    return list.toArray(res);
  }

  private void getAndroidVersion(CallbackContext callback) {
    callback.success(Build.VERSION.SDK_INT);
  }

  private void getWebkitInfo(CallbackContext callback) {
    PackageInfo info = null;
    JSONObject res = new JSONObject();

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        info = WebView.getCurrentWebViewPackage();
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        Class webViewFactory = Class.forName("android.webkit.WebViewFactory");
        Method method = webViewFactory.getMethod("getLoadedPackageInfo");
        info = (PackageInfo) method.invoke(null);
      } else {
        PackageManager packageManager = activity.getPackageManager();

        try {
          info = packageManager.getPackageInfo("com.google.android.webview", 0);
        } catch (PackageManager.NameNotFoundException e) {
          callback.error("Package not found");
        }

        return;
      }

      res.put("packageName", info.packageName);
      res.put("versionName", info.versionName);
      res.put("versionCode", info.versionCode);

      callback.success(res);
    } catch (
      JSONException
      | InvocationTargetException
      | ClassNotFoundException
      | NoSuchMethodException
      | IllegalAccessException e
    ) {
      callback.error(
        "Cannot determine current WebView engine. (" + e.getMessage() + ")"
      );

      return;
    }
  }

  private void isPowerSaveMode(CallbackContext callback) {
    PowerManager powerManager = (PowerManager) context.getSystemService(
      Context.POWER_SERVICE
    );
    boolean powerSaveMode = powerManager.isPowerSaveMode();

    callback.success(powerSaveMode ? 1 : 0);
  }

  private void fileAction(
    String fileURI,
    String filename,
    String action,
    String mimeType,
    CallbackContext callback
  ) {
    Activity activity = this.activity;
    Context context = this.context;
    Uri uri = this.getContentProviderUri(fileURI);
    try {
      Intent intent = new Intent(action);

      if (mimeType.equals("")) {
        mimeType = "text/plain";
      }

      if (action.equals(Intent.ACTION_SEND)) {
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (!filename.equals("")) {
          intent.putExtra(Intent.EXTRA_TEXT, filename);
        }
        intent.setType(mimeType);
      } else {
        int flags =
          Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
          Intent.FLAG_GRANT_READ_URI_PERMISSION;

        if (action.equals(Intent.ACTION_EDIT)) {
          flags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        }

        intent.setFlags(flags);
        intent.setDataAndType(uri, mimeType);
      }

      activity.startActivity(intent);
      callback.success(uri.toString());
    } catch (Exception e) {
      callback.error(e.getMessage());
    }
  }

  private void getAppInfo(CallbackContext callback) {
    JSONObject res = new JSONObject();
    try {
      PackageManager pm = activity.getPackageManager();
      PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
      ApplicationInfo appInfo = context.getApplicationInfo();
      int isDebuggable = appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE;

      res.put("firstInstallTime", pInfo.firstInstallTime);
      res.put("lastUpdateTime", pInfo.lastUpdateTime);
      res.put("label", appInfo.loadLabel(pm).toString());
      res.put("packageName", pInfo.packageName);
      res.put("versionName", pInfo.versionName);
      res.put("versionCode", pInfo.getLongVersionCode());
      res.put("isDebuggable", isDebuggable);

      callback.success(res);
    } catch (JSONException e) {
      callback.error(e.getMessage());
    } catch (Exception e) {
      callback.error(e.getMessage());
    }
  }

  private void openInBrowser(String src, CallbackContext callback) {
    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(src));
    activity.startActivity(browserIntent);
  }

  private void launchApp(
    String appId,
    String action,
    String value,
    CallbackContext callback
  ) {
    Intent intent = context
      .getPackageManager()
      .getLaunchIntentForPackage(appId);
    if (intent == null) {
      callback.error("App not found");
      return;
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
    callback.success("Launched " + appId);
  }

  private void addShortcut(
    String id,
    String label,
    String description,
    String iconSrc,
    String action,
    String data,
    CallbackContext callback
  ) {
    try {
      Intent intent;
      ImageDecoder.Source imgSrc;
      Bitmap bitmap;
      IconCompat icon;

      imgSrc =
        ImageDecoder.createSource(
          context.getContentResolver(),
          Uri.parse(iconSrc)
        );
      bitmap = ImageDecoder.decodeBitmap(imgSrc);
      icon = IconCompat.createWithBitmap(bitmap);
      intent =
        activity
          .getPackageManager()
          .getLaunchIntentForPackage(activity.getPackageName());
      intent.putExtra("action", action);
      intent.putExtra("data", data);

      ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(context, id)
        .setShortLabel(label)
        .setLongLabel(description)
        .setIcon(icon)
        .setIntent(intent)
        .build();

      ShortcutManagerCompat.pushDynamicShortcut(context, shortcut);
      callback.success();
    } catch (Exception e) {
      callback.error(e.toString());
    }
  }

  private void pinShortcut(String id, CallbackContext callback) {
    ShortcutManager shortcutManager = context.getSystemService(
      ShortcutManager.class
    );

    if (shortcutManager.isRequestPinShortcutSupported()) {
      ShortcutInfo pinShortcutInfo = new ShortcutInfo.Builder(context, id)
        .build();

      Intent pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(
        pinShortcutInfo
      );

      PendingIntent successCallback = PendingIntent.getBroadcast(
        context,
        0,
        pinnedShortcutCallbackIntent,
        0
      );

      shortcutManager.requestPinShortcut(
        pinShortcutInfo,
        successCallback.getIntentSender()
      );

      callback.success();
      return;
    }

    callback.error("Not supported");
  }

  private void removeShortcut(String id, CallbackContext callback) {
    try {
      List<String> list = new ArrayList<String>();
      list.add(id);
      ShortcutManagerCompat.removeDynamicShortcuts(context, list);
      callback.success();
    } catch (Exception e) {
      callback.error(e.toString());
    }
  }

  private void setUiTheme(
    final String systemBarColor,
    final JSONObject schema,
    final CallbackContext callback
  ) {
    this.systemBarColor = Color.parseColor(systemBarColor);
    this.theme = new Theme(schema);

    final Window window = activity.getWindow();
    // Method and constants not available on all SDKs but we want to be able to compile this code with any SDK
    window.clearFlags(0x04000000); // SDK 19: WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    window.addFlags(0x80000000); // SDK 21: WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    try {
      // Using reflection makes sure any 5.0+ device will work without having to compile with SDK level 21

      window
        .getClass()
        .getMethod("setNavigationBarColor", int.class)
        .invoke(window, this.systemBarColor);

      window
        .getClass()
        .getMethod("setStatusBarColor", int.class)
        .invoke(window, this.systemBarColor);

      if (Build.VERSION.SDK_INT < 30) {
        setStatusBarStyle(window);
        setNavigationBarStyle(window);
      } else {
        String themeType = theme.getType();
        WindowInsetsController controller = window.getInsetsController();
        int appearance =
          WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
          WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;

        if (themeType.equals("light")) {
          controller.setSystemBarsAppearance(appearance, appearance);
        } else {
          controller.setSystemBarsAppearance(0, appearance);
        }
      }
      callback.success("OK");
    } catch (IllegalArgumentException error) {
      callback.error(error.toString());
    } catch (Exception error) {
      callback.error(error.toString());
    }
  }

  private void setStatusBarStyle(final Window window) {
    View decorView = window.getDecorView();
    int uiOptions = decorView.getSystemUiVisibility();
    String themeType = theme.getType();

    if (themeType.equals("light")) {
      decorView.setSystemUiVisibility(
        uiOptions | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      );
      return;
    }
    decorView.setSystemUiVisibility(
      uiOptions & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    );
  }

  private void setNavigationBarStyle(final Window window) {
    View decorView = window.getDecorView();
    int uiOptions = decorView.getSystemUiVisibility();
    String themeType = theme.getType();

    // 0x80000000 FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
    // 0x00000010 SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

    if (themeType.equals("light")) {
      decorView.setSystemUiVisibility(uiOptions | 0x80000000 | 0x00000010);
      return;
    }
    decorView.setSystemUiVisibility(uiOptions | 0x80000000 & ~0x00000010);
  }

  private void getCordovaIntent(CallbackContext callback) {
    Intent intent = activity.getIntent();
    callback.sendPluginResult(
      new PluginResult(PluginResult.Status.OK, getIntentJson(intent))
    );
  }

  private void setIntentHandler(CallbackContext callback) {
    intentHandler = callback;
    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    result.setKeepCallback(true);
    callback.sendPluginResult(result);
  }

  @Override
  public void onNewIntent(Intent intent) {
    if (intentHandler != null) {
      PluginResult result = new PluginResult(
        PluginResult.Status.OK,
        getIntentJson(intent)
      );
      result.setKeepCallback(true);
      intentHandler.sendPluginResult(result);
    }
  }

  private JSONObject getIntentJson(Intent intent) {
    JSONObject json = new JSONObject();
    try {
      json.put("action", intent.getAction());
      json.put("data", intent.getDataString());
      json.put("type", intent.getType());
      json.put("package", intent.getPackage());
      json.put("extras", getExtrasJson(intent.getExtras()));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return json;
  }

  private JSONObject getExtrasJson(Bundle extras) {
    JSONObject json = new JSONObject();
    if (extras != null) {
      for (String key : extras.keySet()) {
        try {
          Object value = extras.get(key);
          if (value instanceof String) {
            json.put(key, (String) value);
          } else if (value instanceof Integer) {
            json.put(key, (Integer) value);
          } else if (value instanceof Long) {
            json.put(key, (Long) value);
          } else if (value instanceof Double) {
            json.put(key, (Double) value);
          } else if (value instanceof Float) {
            json.put(key, (Float) value);
          } else if (value instanceof Boolean) {
            json.put(key, (Boolean) value);
          } else if (value instanceof Bundle) {
            json.put(key, getExtrasJson((Bundle) value));
          } else {
            json.put(key, value.toString());
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }
    return json;
  }

  private Uri getContentProviderUri(String fileUri) {
    return this.getContentProviderUri(fileUri, "");
  }

  private Uri getContentProviderUri(String fileUri, String filename) {
    Uri uri = Uri.parse(fileUri);
    String Id = context.getPackageName();
    if (fileUri.matches("file:///(.*)")) {
      File file = new File(uri.getPath());
      if (filename.equals("")) {
        return FileProvider.getUriForFile(context, Id + ".provider", file);
      }

      return FileProvider.getUriForFile(
        context,
        Id + ".provider",
        file,
        filename
      );
    }
    return uri;
  }

  private boolean isPackageInstalled(
    String packageName,
    PackageManager packageManager,
    CallbackContext callback
  ) {
    try {
      packageManager.getPackageInfo(packageName, 0);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    }
  }

  private void getGlobalSetting(String setting, CallbackContext callback) {
    int value = (int) Global.getFloat(
      context.getContentResolver(),
      setting,
      -1
    );
    callback.success(value);
  }

  private void clearCache(CallbackContext callback) {
    webView.clearCache(true);
    callback.success("Cache cleared");
  }

  private void setInputType(String type) {
    int mode = -1;
    if (type.equals("NO_SUGGESTIONS")) {
      mode = 0;
    } else if (type.equals("NO_SUGGESTIONS_AGGRESSIVE")) {
      mode = 1;
    }
    webView.setInputType(mode);
  }
}
