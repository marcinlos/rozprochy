
#ifndef NET_H_
#define NET_H_

// Converts 64-bit value to network endianess
uint64_t htonll(uint64_t hostlong);

// Converts 64-bit value to host endianess
uint64_t ntohll(uint64_t netlong);


// Repeatedly, if necessary, calls recv to gather all the expected data
// In case of an error, prints error message and returns -1.
int recv_total(int fd, void* buffer, int size);


int send_total(int fd, void* buffer, int size);



#endif /* NET_H_ */
