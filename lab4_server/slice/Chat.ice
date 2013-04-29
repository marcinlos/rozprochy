
#ifndef CHAT_ICE
#define CHAT_ICE

#include "Users.ice"

module Chat {

    /*
        Description of the architecture
        
        SystemManager handles registration, login & session management.
        Rooms notify manager about users who joined, so that they can be 
        notified when user times out.  Messages are persisted with sequence
        numbers. Failed deliveries are repeated immediately if the session
        remains valid when the user joins back. Otherwise, user needs to log
        in. For each user and room he's in, sequence number of the last 
        delivered message is persisted. Upon login, all the older messages
        are delivered.
        
    */


    exception ChatException { };


    sequence<string> Rooms;

    interface SystemManager {
        void createAccount(string pesel, string password) 
            throws Users::RegisterException;
            
        string login(string pesel, string password) 
            throws Users::LoginException;
            
        void keepalive(string sessionId) throws Users::SessionException;
        
        void logout(string sessionId) throws Users::SessionException;  
        
        Rooms getRooms(string sessionId) throws Users::SessionException;
    };

    struct Message {
        long id;
        string room;
        string author;
        string content;
    };
    
    sequence<Message> Messages;
    
    exception NotAMember extends ChatException { };
    
    interface Member {
        void keepalive();
        void newMessage(Message msg);
        void newMultipleMessages(Messages msgs);
        void userJoined(string login);
        void userLeaved(string login);
    };
    
    interface Room {
        void join(string sessionId, Member* callback) 
            throws Users::SessionException;
            
        void leave(string sessionId) throws Users::SessionException, NotAMember;
        
        void sendMessage(string sessionId, string text) throws 
            Users::SessionException, NotAMember;
            
        Messages fetchMessages(long since) throws Users::SessionException, 
            NotAMember;
        
    };


};


#endif /* CHAT_ICE */
