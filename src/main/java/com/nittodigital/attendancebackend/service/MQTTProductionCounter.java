package com.nittodigital.attendancebackend.service;

import com.nittodigital.attendancebackend.logic.AxisCreateProductionClient;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class MQTTProductionCounter implements MqttCallback {

   @Autowired
    private AxisCreateProductionClient axisClient;
    @Autowired
    private ValidateCard validateCard;
    //private String mqttBroker = "tcp://iot.eclipse.org:1883";
    private String mqttBroker = "tcp://192.168.0.130";
    private String clientId = "nitto-door";
    private String topic = "door";
    //private CountDownLatch processingFinishedLatch;

    private MqttClient client;
    private MqttConnectOptions connectOptions;
    private String matchineNo;
    private String cardNo;
    public MQTTProductionCounter(){}

    public void mqttSubscriberFetch(){
        try{
            client = new MqttClient(mqttBroker, clientId);
            connectOptions = new MqttConnectOptions();
            connectOptions.setUserName("nitto");
            connectOptions.setPassword("nitto".toCharArray());
            connectOptions.setAutomaticReconnect(true);
            connectOptions.setCleanSession(true);
            connectOptions.setKeepAliveInterval(10);
            //connectOptions.setConnectionTimeout(999999);
            connectOptions.setCleanSession(false);
            client.setCallback(this);
            try{
                client.connect(connectOptions);
                client.subscribe(topic);
            } catch (Exception e){
                //AttendancebackendApplication.restart();
                e.printStackTrace();
            }
            //client.connect();
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        try{
            validateCard.getAuthValue();
        } catch (Exception e){
            Logger.getLogger(MQTTProductionCounter.class.getName()).log(Level.WARNING, "Ofbiz server might be down");
        }
        List<String> result = validateCard.getDataList();

        //System.out.println("Card List: " + result);

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
                    Logger.getLogger(MQTTProductionCounter.class.getName()).log(Level.WARNING, "Ofbiz server might be down");
                }
                MqttMessage authMessage = new MqttMessage("3".getBytes());
                authMessage.setQos(2);
                client.publish("door1", authMessage);
                Logger.getLogger(MQTTProductionCounter.class.getName()).log(Level.INFO, "Requested Card No: " + parts[1] + " \tStatus: Authorized");

            }else if(!loginFlag) {
                MqttMessage authMessage = new MqttMessage("9".getBytes());
                authMessage.setQos(2);
                client.publish("door1", authMessage);
                Logger.getLogger(MQTTProductionCounter.class.getName()).log(Level.INFO, "Requested Card No: " + parts[1] + " \tStatus: Unauthorized");
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void connectionLost(Throwable cause) {
        Logger.getLogger(MQTTProductionCounter.class.getName()).log(Level.WARNING, "Connection Lost. Will be reconnected soon.");
        /*if (!client.isConnected()){
            try{
                connectOptions.setCleanSession(false);
                client.connect(connectOptions);
                client.subscribe(topic);
            } catch (Exception e){
                //AttendancebackendApplication.restart();
                e.printStackTrace();
            }
        }*/
    }
}
