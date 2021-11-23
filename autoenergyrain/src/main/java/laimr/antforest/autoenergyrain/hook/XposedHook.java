package laimr.antforest.autoenergyrain.hook;


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

    /**
     *防止重复执行Hook代码
     * @param flag 判断标识,针对不同Hook代码分别进行判断
     * @return 是否已经注入Hook代码
     * @other 暂时没用
     */
    //private boolean isInjecter(String flag) {
    //    try {
    //        if (TextUtils.isEmpty(flag)) return false;
    //        Field methodCacheField = XposedHelpers.class.getDeclaredField("methodCache");
    //        methodCacheField.setAccessible(true);
    //        HashMap<String, Method> methodCache = (HashMap<String, Method>) methodCacheField.get(null);
    //        Method method=XposedHelpers.findMethodBestMatch(Application.class,"onCreate");
    //        String key=String.format("%s#%s",flag,method.getName());
    //        if (methodCache.containsKey(key)) return true;
    //        methodCache.put(key, method);
    //        return false;
    //    } catch (Throwable e) {
    //        e.printStackTrace();
    //    }
    //    return false;
    //}

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        //if (isInjecter(this.getClass().getName())) {
        //    return;
        //}
        if (loadPackageParam.packageName.equals(GlobalInfo.hookedPackageName) && loadPackageParam.processName.equals(GlobalInfo.hookedPackageName)) {
            XposedHook.intfXposed = this;
            Log.e("NMSL", "handleLoadPackage:"+loadPackageParam.packageName + " " + loadPackageParam.processName);
            gsClassLoader = loadPackageParam.classLoader;
            if(mLauncherActivity == null){
                launcherActivityResume();
            }
            if(mService == null){
                launcherSerivice_init();
            }
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
            }

            private void registerLauncherServiceReceiver() {
                isBroadcastInit = true;
                Log.e("NMSL","注册广播接收器成功");
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(GlobalInfo.BROADCASTTAG+".rpccallfromclient");
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
                    Object alipayApplication = XposedHelpers.callStaticMethod(alipayApplicationClazz, "getInstance");
                    aliPayContext = XposedHelpers.callMethod(alipayApplication, "getMicroApplicationContext");
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
        Object rpcServiceImpl = XposedHelpers.callMethod(aliPayContext, "findServiceByInterface", "com.alipay.mobile.framework.service.common.RpcService");
        if (rpcServiceImpl == null) {
            Log.e("NMSL", "出问题1");
        }
        try {
            Class simpleRpcService = this.gsClassLoader.loadClass("com.alipay.mobile.framework.service.ext.SimpleRpcService");
            rpcSenderProxy = XposedHelpers.callMethod(
                    rpcServiceImpl,
                    "getRpcProxy",
                    simpleRpcService);
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
            Object myAccountInfoModel = XposedHelpers.callMethod(socialSdkContactServiceImpl, "getMyAccountInfoModelByLocal");
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
