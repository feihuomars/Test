package com.example.android.mqttservicetest;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    public FragmentTabHost fragmentTabHost;
    final String serverUri = "tcp://47.94.246.26:1883";
    private String clientId;
    private MqttAndroidClient mqttAndroidClient;
    private ArrayList<String> historyList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        fragmentTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        fragmentTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        Bundle bundle = new Bundle();

        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("history").setIndicator("历史"), HistoryFragment.class, bundle);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("订阅").setIndicator("订阅"), SubscriptionFragment.class, bundle);

        initMqtt();
    }

    private void initMqtt() {
        clientId = MqttClient.generateClientId();
        mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(), serverUri,
                        clientId);

        try {
            mqttAndroidClient.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    HistoryFragment historyFragment = (HistoryFragment) getSupportFragmentManager().findFragmentByTag("history");
                    View view = historyFragment.getView();
                    final TextView textView = (TextView) view.findViewById(R.id.history_text);
                    //final ListView listView = view.findViewById(R.id.history_list_view);
                    textView.setText("success");
                    //historyList.add("warning");
                    try {

                        mqttAndroidClient.subscribe("warning", 0, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Log.i(TAG, "onSuccess: successfully subscribe");
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Log.i(TAG, "onFailure: failed to subscribe");
                            }
                        });


                        mqttAndroidClient.subscribe("warning", 0, new IMqttMessageListener() {

                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {
                                String recMessage = new String(message.getPayload());
                                Log.i(TAG, "messageArrived: " + recMessage);
                                textView.setText(recMessage);
                                //historyList.add(recMessage);

                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, historyList);
                    //listView.setAdapter(adapter);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "onFailure: failed to connect to server");
                }
            });




        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}