package laimr.antforest.autoenergyrain.ui.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import laimr.antforest.autoenergyrain.ui.MainActivity;

/**
 * @author laiyulong
 * @date 2021/11/21 22:50
 */
public class CallBackBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //神奇的context
        Log.i("NMSL",context.getClass().getName());
        switch (intent.getStringExtra("type")){
            //case "currentUserId":
            //    Toast.makeText(context,"当前支付宝登录UserId:"+intent.getStringExtra("returnData"), Toast.LENGTH_SHORT).show();
            //    break;
            default:
                Toast.makeText(context,intent.getStringExtra("returnData"), Toast.LENGTH_SHORT).show();

        }
    }
}
