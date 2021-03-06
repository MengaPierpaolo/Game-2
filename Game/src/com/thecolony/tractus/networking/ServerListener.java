package com.thecolony.tractus.networking;

import com.thecolony.tractus.networking.messages.UpdateMessage;
import com.jme3.network.MessageListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;



public class ServerListener implements MessageListener<HostedConnection> {

    public void messageReceived(HostedConnection source, Message message) {
//        if (message instanceof GreetingMessage) {
//            GreetingMessage helloMessage = (GreetingMessage) message;
//            System.out.println("Server received '"
//                    + helloMessage.getGreeting()
//                    + "' from client #" + source.getId());
//            helloMessage.setGreeting("Welcome client #" + source.getId() + "!");
//            source.send(helloMessage);
//        } 
        if(message instanceof UpdateMessage)
        {
	  UpdateMessage msg = (UpdateMessage) message;
	  System.out.println("Sever recieved Msg: " + msg.getMessage());
        }
    }
}