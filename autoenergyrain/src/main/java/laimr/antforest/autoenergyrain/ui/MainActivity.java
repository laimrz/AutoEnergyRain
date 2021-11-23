package laimr.antforest.autoenergyrain.ui;


import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import laimr.antforest.autoenergyrain.R;
import laimr.antforest.autoenergyrain.data.GlobalInfo;
import laimr.antforest.autoenergyrain.ui.core.CallBackBroadcastReceiver;

public class MainActivity extends AppCompatActivity {
    //public static Context MainActivityContext;
    //{
    //    MainActivity.MainActivityContext = this;
    //}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("NMSL", "我插件UI起了，一枪秒了");
        this.registerReceiver(new CallBackBroadcastReceiver(), new IntentFilter(GlobalInfo.CALLBACKTAG));


        ((Button) this.findViewById(R.id.getUserId)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sendBroadcast(new Intent("com.alipay.laimr.rpccallfromclient").putExtras(createBundle("getCurrentId")));
                sendBroadcast(createIntentWithBundle("getCurrentId", "", ""));
            }
        });

        ((Button) this.findViewById(R.id.grantOtherEnergyRain)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //MainActivity.this.sendBroadcast(
                //        new Intent("com.alipay.laimr.rpccallfromclient")
                //                .putExtras(
                //                        createBundle("grantEnergyRain",
                //                                "alipay.antforest.forest.h5.grantEnergyRainChance",
                //                                "[{\"targetUserId\":" + ((EditText) findViewById(R.id.grantedUserid)).getText() + "}]"
                //                        )
                //                )
                //);
                MainActivity.this.sendBroadcast(createIntentWithBundle(
                        "grantEnergyRain",
                        "alipay.antforest.forest.h5.grantEnergyRainChance",
                        "[{\"targetUserId\":" + ((EditText) findViewById(R.id.grantedUserid)).getText() + "}]"
                        )
                );
                //Toast.makeText(MainActivity.this, "发送广播成功", Toast.LENGTH_SHORT).show();
            }
        });
        ((Button) this.findViewById(R.id.playenerygrain)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //MainActivity.this.sendBroadcast(new Intent("com.alipay.laimr.rpccallfromclient").putExtras(createBundle("finishEnergyRain3times")));
                MainActivity.this.sendBroadcast(createIntentWithBundle("finishEnergyRain3times", "", ""));
            }
        });
    }

    private Bundle createBundle(String goal) {
        Bundle bundle = new Bundle();
        bundle.putString("goal", goal);
        return bundle;
    }

    private Intent createIntentWithBundle(String goal, String api, String data) {
        Bundle bundle = new Bundle();
        bundle.putString("goal", goal);
        bundle.putString("api", api);
        bundle.putString("data", data);
        return new Intent(GlobalInfo.BROADCASTTAG + ".rpccallfromclient").putExtras(bundle);
    }

}