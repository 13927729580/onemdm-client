package com.multunus.onemdm.heartbeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.multunus.onemdm.MQTTConnector;
import com.multunus.onemdm.network.HeartbeatRecorder;
import com.multunus.onemdm.util.Logger;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;

public class HeartbeatListener extends BroadcastReceiver {

    private HeartbeatRecorder heartbeatRecorder;

    public HeartbeatListener(){
         this.heartbeatRecorder = new HeartbeatRecorder();
    }

    HeartbeatListener(HeartbeatRecorder heartbeatRecorder){
        this.heartbeatRecorder = heartbeatRecorder;
    }
    @Override
    public void onReceive(final Context context, Intent intent) {
        Logger.debug("broadcast received for alarm manager");

        heartbeatRecorder.sendHeartbeatToServer(context);
        MQTTConnector.getInstance(context).publish();
    }
}
