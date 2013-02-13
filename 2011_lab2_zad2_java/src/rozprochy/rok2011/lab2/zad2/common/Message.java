package rozprochy.rok2011.lab2.zad2.common;

import java.io.Serializable;

/**
 * Class representing published messages
 */
public class Message implements Serializable {

    private String topic;
    private String content;
    
    public Message(String topic, String content) {
        this.topic = topic;
        this.content = content;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
}
