package test.lee.bindertest.messenger;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import test.lee.bindertest.Constants;

/**
 * 使用Messenger实现进程间通讯
 *
 * @author lee
 */
public class MessengerService extends Service {
    private static final String TAG = "MessengerService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //返回messenger底层binder
        return mMessenger.getBinder();
    }

    //创建Handler
    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_FROM_CLIENT:
                    Log.i("lhy", "receive msg from Client:" + msg.getData().getString("msg"));
                    Messenger client = msg.replyTo;
                    Message replyMessage = Message.obtain(null, Constants.MSG_FROM_SERVICE);
                    Bundle bundle = new Bundle();
                    bundle.putString("reply", "嗯，你的消息我已经收到，稍后会回复你。");
                    replyMessage.setData(bundle);
                    try {
                        client.send(replyMessage);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    ;
    //创建一个Messenger对象
    private final Messenger mMessenger = new Messenger(new MessengerHandler());
}