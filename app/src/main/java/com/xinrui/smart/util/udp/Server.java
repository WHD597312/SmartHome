package com.xinrui.smart.util.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {
    public static String accept(){
        StringBuffer sb=new StringBuffer();
        DatagramPacket datagramPacket = null;
        DatagramSocket datagramSocket;

        try {
            datagramSocket=new DatagramSocket(1112);

            while(true){
                byte[] buffer=new byte[1024*10];
                datagramPacket=new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);
                byte[] bytes=datagramPacket.getData();
                String data=new String(bytes, 0, bytes.length);
                sb.append(data);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sb.toString();
    }
}
