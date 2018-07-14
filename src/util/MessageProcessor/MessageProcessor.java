package util.MessageProcessor;

import util.Objects.Message;

import java.io.*;
import java.net.Socket;

/**
 * 处理消息发送与接收。
 */
public class MessageProcessor
{
    public static void sendMessage(Message message, Socket socket) throws IOException
    {
        try (OutputStream out = socket.getOutputStream();
             ObjectOutputStream objOut = new ObjectOutputStream(out))
        {
            objOut.writeObject(message);
            objOut.flush();
        }
    }

    public static Message parseMessage(byte[] bytes) throws IOException, ClassNotFoundException
    {
        try (InputStream in = new ByteArrayInputStream(bytes);
             ObjectInputStream objIn = new ObjectInputStream(in))
        {
            return (Message) objIn.readObject();
        }
    }
}
