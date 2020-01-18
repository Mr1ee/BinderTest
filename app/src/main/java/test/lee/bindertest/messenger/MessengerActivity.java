package test.lee.bindertest.messenger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import test.lee.bindertest.Constants;
import test.lee.bindertest.R;
import test.lee.bindertest.ThirdActivity;

public class MessengerActivity extends AppCompatActivity {
    private static final String TAG = "MessengerActivity";
    private Messenger mMessenger;
    //创建Messenger对象
    private Messenger mGetReplyMessenger = new Messenger(new MessengerHandler());

    //创建handler对象
    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_FROM_SERVICE:
                    Log.i("lhy", "receive msg from Service:" + msg.getData().getString("reply"));
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMessenger = new Messenger(service);
            Message msg = Message.obtain(null, Constants.MSG_FROM_CLIENT);
            Bundle data = new Bundle();
            data.putString("msg", "hello,this is client");
            msg.setData(data);
            msg.replyTo = mGetReplyMessenger;
            try {
                mMessenger.send(msg);//发送message
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        Intent intent = new Intent(this, MessengerService.class);
        //绑定Service
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        findViewById(R.id.tv_messenger).setOnClickListener(v -> {
            startActivity(new Intent(this, ThirdActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        //解绑
        unbindService(mConnection);
        super.onDestroy();
    }
}
