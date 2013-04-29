
#ifndef USERS_ICE
#define USERS_ICE

module Users {

    exception UserException { };

    exception DbError { };
    
    // Exceptions generated by account creation operation
    exception RegisterException extends UserException { };
    
    exception AccountAlreadyExists extends RegisterException { };
    exception InvalidLogin extends RegisterException { string reason; };
    exception InvalidPassword extends RegisterException { string reason; };
    
    // Exceptions generated by login operation
    exception LoginException extends UserException { };
    
    exception MultiLogin extends LoginException { };
    exception AuthenticationFailed extends LoginException { };
    
    // Exceptions generated by business operations
    exception OperationException extends UserException { };
    
    // Session exceptions
    exception SessionException extends UserException { };
    
    exception InvalidSession extends SessionException { };
    exception SessionExpired extends SessionException { };

};


#endif /* USERS_ICE */