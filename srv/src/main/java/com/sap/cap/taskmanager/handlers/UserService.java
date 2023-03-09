package com.sap.cap.taskmanager.handlers;

import java.lang.Math;   
import com.nimbusds.jose.shaded.json.JSONObject;
import com.sap.cds.services.cds.CqnService;

import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.messaging.MessagingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import cds.gen.adminservice.User;
import cds.gen.adminservice.User_;

@Component
@ServiceName("AdminService")
public class UserService implements EventHandler{
    
    Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final String PASSWORD_PREFIX = "Welcome";


    @Autowired
    @Qualifier("taskmanager-events")
    MessagingService messagingService;

    @Before(event = CqnService.EVENT_CREATE , entity = User_.CDS_NAME)
    public void onCreate(User userData) {

        String password = PASSWORD_PREFIX + String.valueOf( Math.random()) ;

        userData.setPassword(password);

        logger.info("Updated default password for {}", userData.getFirstName());
    }

    @After(event = CqnService.EVENT_CREATE , entity = User_.CDS_NAME)
    public void afterCreate(User userData) {
        
        JSONObject payload = new JSONObject();

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("firstName", userData.getFirstName());
        jsonObject.put("otp", userData.getPassword());

        payload.put("data", jsonObject);

        logger.info("Sending message to the queue");

        messagingService.emit("sap/taskmanager-events/event-mesh/user-registration-topic", payload);

    }
}