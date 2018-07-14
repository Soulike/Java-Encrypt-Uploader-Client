package util.Objects;

import java.io.Serializable;

/**
 * 双方通信消息
 */
public class Message implements Serializable
{
    private final boolean isSuccessful;
    private final String message;

    public Message(boolean isSuccessful, String message)
    {
        this.isSuccessful = isSuccessful;
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public boolean isSuccessful()
    {
        return isSuccessful;
    }
}
