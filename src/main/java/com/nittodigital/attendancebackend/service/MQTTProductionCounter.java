package com.nittodigital.attendancebackend.service;

import com.nittodigital.attendancebackend.logic.AxisCreateProductionClient;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class MQTTProductionCounter implements MqttCallback {

   @Autowired
    private AxisCreateProductionClient axisClient;
    @Autowired
    private ValidateCard validateCard;
    private String mqttBroker = "tcp://iot.eclipse.org:1883";
    private String clientId = "nitto-door";
    private String topic = "door";
    //private CountDownLatch processingFinishedLatch;

    private MqttClient client;

    private String matchineNo;
    private String cardNo;
    public MQTTProductionCounter(){mqttSubscriberFetch();}

    public void mqttSubscriberFetch(){
        try{
            client = new MqttClient(mqttBroker, clientId);
            client.connect();
            client.setCallback(this);
            client.subscribe(topic);
            //processingFinishedLatch = new CountDownLatch(1);
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        try{
            validateCard.getAuthValue();
        } catch (Exception e){
            System.out.println("Ofbiz Server Might be down.");
        }
        List<String> result = validateCard.getDataList();
        System.out.println("Message Arrived " + message);
        boolean loginFlag = false;

        if(new String(message.getPayload()).contains("-")){
            String [] parts = new String(message.getPayload()).split("-");
            for(int i=0; i<result.size(); i++){
                if (result.get(i).equals(parts[1])){
                    loginFlag = true;
                    break;
                }else {
                    loginFlag = false;
                }
            }
            if (loginFlag){
                try{
                    axisClient.sendForInsert(parts[1], parts[0], new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                } catch (Exception e){
                    System.out.println("Ofbiz Server Might be down.");
                }
                client.publish("door1", new MqttMessage("3".getBytes()));

            }else {
                client.publish("door1", new MqttMessage("9".getBytes()));
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
       System.out.println("Message Successfully delivered.");
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection Lost");
        try {
            if (!client.isConnected()){
                System.out.println("Reconnecting ...");
                client.reconnect();
                System.out.println("Connected");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
