package com.xy.android.videocall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by xy on 2017/12/28.
 */

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_toVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,VideoCallActivity.class));
                CallManager.getInstance().setChatId("userId");
                CallManager.getInstance().setInComingCall(true);
                CallManager.getInstance().setCallType(CallManager.CallType.VIDEO);
            }
        });
    }
}
