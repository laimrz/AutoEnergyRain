package laimr.antforest.autoenergyrain.hook;

import static laimr.antforest.autoenergyrain.data.GlobalInfo.CALLBACKTAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Printer;
import android.widget.ResourceCursorAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import laimr.antforest.autoenergyrain.data.GlobalInfo;
import laimr.antforest.autoenergyrain.data.GoalList;

/**
 * @author laiyulong
 * @date 2021/11/21 18:59
 */
public class launcherServiceBroadcastReciever extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("支付宝接收到广播：", intent.getAction());

        //Log.e("NMSL接收到广播：",intent.getExtras().toString());
        if (intent.getAction().equals(GlobalInfo.BROADCASTTAG+".rpccallfromclient")) {
            new Thread() {
                @Override
                public void run() {
                    String result = solveIntent(intent);
                    broadCastSendBack("nomalRpcCall", result);
                }
            }.start();
        }

    }

    private String solveIntent(Intent intent) {
        String result = "";
        if (intent.getExtras().get("goal") == null) {
            result = XposedHook.rpcCall_send_intf(intent.getExtras().getString("api"), intent.getExtras().getString("data"));
        } else {
            String goal = intent.getExtras().getString("goal");
            if ("finishEnergyRain3times".equals(goal)) {
                result = finishEnergyRain3times();
            } else if ("getCurrentId".equals(goal)) {
                result = getCurrentId();
            } else if("grantEnergyRain".equals(goal)){
                result = grantEnergyRain(intent.getExtras());
            }else{
                result = "请求出错:"+"goal";

            }
        }
        return result;
    }

    private String grantEnergyRain(Bundle bundle) {
        String queryResponse = XposedHook.rpcCall_send_intf(bundle.getString("api"), bundle.getString("data"));
        try {
            JSONObject jb = new JSONObject(queryResponse);
            if(jb.getBoolean("success")){
                return "赠送能量雨成功";
            }else{
                return "失败："+jb.getString("resultDesc");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "赠送能量雨错误，联系开发者";
    }

    private String getCurrentId() {
        XposedHook.intfXposed.getCurrentUserId();
        return "当前UID:"+GlobalInfo.userId;
    }

    private String finishEnergyRain3times() {
        String queryResponse = XposedHook.rpcCall_send_intf("alipay.antforest.forest.h5.startEnergyRain", "[{}]");
        try {
            JSONObject jb = new JSONObject(queryResponse);
            if (jb.getString("resultDesc").equals("成功")) {
                JSONArray sizeArray = jb.getJSONObject("difficultyInfo").getJSONArray("bubbleEnergyList");
                int size = 0;
                for (int i = 0; i < sizeArray.length(); i++) {
                    size += sizeArray.getInt(i);
                }
                String token = jb.getString("token");
                Log.e("NMSL",String.valueOf(size));
                Log.e("NMSL",token);
                String result = XposedHook.rpcCall_send_intf(
                        "alipay.antforest.forest.h5.energyRainSettlement",
                        "[{\"activityPropNums\":0,\"saveEnergy\":" + size + ",\"token\":\"" + token + "\"}]"
                );
                Log.e("NMSL",result);
                JSONObject result_jb = new JSONObject(result);
                if(result_jb.getBoolean("success")){
                    return result_jb.getString("resultDesc")+" 收获:"+size+"g";
                }else{
                    return result;
                }
            }else{
                return "没有能量雨机会哦！";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 广播回调函数，返回给能量雨助手
     *
     * @param type       回调类型
     * @param returnData 回调数据
     */
    private void broadCastSendBack(String type, String returnData) {
        XposedHook.mService.sendBroadcast(new Intent(CALLBACKTAG).
                putExtra("type", type).
                putExtra("returnData", returnData)
        );
    }
}
