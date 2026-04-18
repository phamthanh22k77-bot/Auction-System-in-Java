package client.message;

import java.io.Serializable;
import java.util.Objects;

public class PacketMessage implements Serializable {
    //Biến
    private MessageType type;
    private Object payload;

    //Constructor
    public PacketMessage(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    //Set, get
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    //phương thức trả về thông tin
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PacketMessage that = (PacketMessage) o;
        return type == that.type &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, payload);
    }

    @Override
    public String toString() {
        return "PacketMessage{" +
                "type=" + type +
                ", payload=" + payload.toString() +
                '}';
    }
}