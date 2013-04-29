package rozprochy.lab4.chat.server;

import rozprochy.lab4.generic.BasicAccountData;

public class ChatAccountData extends BasicAccountData {

    public ChatAccountData(String login, byte[] hashedPassword, byte[] salt) {
        super(login, hashedPassword, salt);
    }

}
