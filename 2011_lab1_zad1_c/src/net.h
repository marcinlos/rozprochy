
#ifndef NET_H_
#define NET_H_


// Value used when the port is not specified
#define DEFAULT_PORT 5000


// Converts 64-bit value to network endianess
uint64_t htonll(uint64_t hostlong);

// Converts 64-bit value to host endianess
uint64_t ntohll(uint64_t netlong);


// Repeatedly, if necessary, calls recv to gather all the expected data
// In case of an error, prints error message and returns -1.
int recv_total(int fd, void* buffer, int size);


// Repeatedly, if nesessary, calls send to push the whole buffer through
// the network. In case of an error returns -1.
int send_total(int fd, void* buffer, int size);


// Closes the connection, "taking care" of possible errors
void close_socket(int fd);


#endif /* NET_H_ */
