package com.nittodigital.attendancebackend;

import com.nittodigital.attendancebackend.service.MQTTProductionCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class AttendancebackendApplication {

    @Autowired
    private MQTTProductionCounter client;

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        client.mqttSubscriberFetch();
    }
    public static void main(String[] args) {
        SpringApplication.run(AttendancebackendApplication.class, args);
    }

}

