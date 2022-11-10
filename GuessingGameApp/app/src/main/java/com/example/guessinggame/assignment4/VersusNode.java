package com.example.guessinggame.assignment4;

import java.io.Serializable;
import java.util.UUID;

//Interface for Bluetooth functionality needed on both client & server
public interface VersusNode extends Runnable, Serializable {
    public static final UUID SERVICE_UUID = UUID.fromString("f9ac9130-e4f7-4a41-96fb-ea8f8b545b76");
    public static final String SERVICE_NAME = "Versus Match Service";
    public void forwardMessage(String message);
    public void stop();
    public void registerActivity(VersusActivity versusActivity);
}
