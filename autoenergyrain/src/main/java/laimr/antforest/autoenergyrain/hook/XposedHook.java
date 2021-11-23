package laimr.antforest.autoenergyrain.hook;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import laimr.antforest.autoenergyrain.data.GlobalInfo;

/**
 * @author laiyulong
 * @date 2021/11/21 12:16
 */
public class XposedHook implements IXposedHookLoadPackage {


    private boolean isRpcInit = false;
    private boolean isBroadcastInit = false;
    private boolean isServiceInit = false;
    private ClassLoader gsClassLoader;
    private Object aliPayContext;
    public static Object rpcSenderProxy;
    public static Service mService;
    public static Context mLauncherActivity;
    public static Map neededHashMap;
    public static XposedHook intfXposed;

    public static String rpcCall_send_intf(String api, String data) {
        return (String) XposedHelpers.callMethod(XposedHook.rpcSenderProxy, "executeRPC", new Object[]{api, data, XposedHook.neededHashMap});
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHook.intfXposed = this;
        if (loadPackageParam.packageName.equals("com.eg.android.AlipayGphone")) {
            Log.e("NMSL", loadPackageParam.packageName + " " + loadPackageParam.processName);
            gsClassLoader = loadPackageParam.classLoader;
            launcherActivityResume();
            launcherSerivice_init();

        }

    }

    //支付宝服务启动 要做的事情
    private void launcherSerivice_init() {
        XposedHelpers.findAndHookMethod("com.alipay.android.launcher.service.LauncherService", this.gsClassLoader, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mService = (Service) param.thisObject;
                if(GlobalInfo.userId.equals("")){
                    getCurrentUserId();
                    Log.e("NMSL","Service给uid赋值");
                }
                Log.e("NMSL","oncreate LauncherService");
                if (!isBroadcastInit) {
                    registerLauncherServiceReceiver();
                }

                isServiceInit = mService != null;

            }

            private void registerLauncherServiceReceiver() {
                isBroadcastInit = true;
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(GlobalInfo.BROADCASTTAG+".rpccallfromclient");
                //intentFilter.addAction("com.alipays.laimr.getcurrentid");
                mService.registerReceiver(new launcherServiceBroadcastReciever(), intentFilter);
            }
        });
    }

    //软件启动Resume要做的事情
    private void launcherActivityResume() {
        XposedHelpers.findAndHookMethod("com.alipay.mobile.quinox.LauncherActivity", gsClassLoader, "onResume", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mLauncherActivity = (Context) param.thisObject;
                //super.afterHookedMethod(param);
                if (aliPayContext == null) {
                    Class<?> alipayApplicationClazz = XposedHelpers.findClass("com.alipay.mobile.framework.AlipayApplication", gsClassLoader);
                    Object alipayApplication = XposedHelpers.callStaticMethod(alipayApplicationClazz, "getInstance", new Object[0]);
                    aliPayContext = XposedHelpers.callMethod(alipayApplication, "getMicroApplicationContext", new Object[0]);
                }
                if (!isRpcInit) {
                    rpcCall_init();
                    //初始化userid信息
                }
                getCurrentUserId();


            }
        });
    }


    private void rpcCall_init() {
        Object rpcServiceImpl = XposedHelpers.callMethod(aliPayContext, "findServiceByInterface", new Object[]{"com.alipay.mobile.framework.service.common.RpcService"});
        if (rpcServiceImpl == null) {
            Log.e("NMSL", "出问题1");
        }
        try {
            Class simpleRpcService = this.gsClassLoader.loadClass("com.alipay.mobile.framework.service.ext.SimpleRpcService");
            Object proxy = XposedHelpers.callMethod(
                    rpcServiceImpl,
                    "getRpcProxy",
                    new Object[]{simpleRpcService}
            );
            rpcSenderProxy = proxy;
            if (rpcServiceImpl != null) {
                isRpcInit = true;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }


    public void getCurrentUserId() {
        try {
            //Class<?> socialSdkContactServiceClazz = XposedHelpers.findClass("com.alipay.mobile.personalbase.service.SocialSdkContactService", gsClassLoader);
            Object socialSdkContactServiceImpl = XposedHelpers.callMethod(aliPayContext, "findServiceByInterface", "com.alipay.mobile.personalbase.service.SocialSdkContactService");
            Object myAccountInfoModel = XposedHelpers.callMethod(socialSdkContactServiceImpl, "getMyAccountInfoModelByLocal", new Object[0]);
            if (!(((String) XposedHelpers.getObjectField(myAccountInfoModel, "userId")).equals(GlobalInfo.userId))) {
                GlobalInfo.userId = (String) XposedHelpers.getObjectField(myAccountInfoModel, "userId");
                GlobalInfo.loginId = (String) XposedHelpers.getObjectField(myAccountInfoModel, "loginId");
                Toast.makeText(mLauncherActivity, "能量雨助手：登录成功", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
