package com.xinrui.smart.util.udp;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    public static void send(String hostAddress,String data,int port){
        try {
            InetAddress address=InetAddress.getByName(hostAddress);

            byte[] bytes=data.getBytes("utf-8");
            int len=bytes.length;
            DatagramPacket packet=new DatagramPacket(bytes,len,address,port);
            DatagramSocket datagramSocket=new DatagramSocket();
            datagramSocket.send(packet);
            datagramSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
