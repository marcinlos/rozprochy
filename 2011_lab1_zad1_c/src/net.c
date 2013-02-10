#include <string.h>
#include <stdio.h>
#include <stdint.h>
#include <netinet/in.h>


// Converts 64-bit value to network endianess
uint64_t htonll(uint64_t hostlong)
{
    char buffer[8];
    // Extract high and low words, according to native endianess
    uint32_t high = hostlong >> 32;
    uint32_t low = hostlong & 0xffffffff;

    // Ensure network endianess in words
    high = htonl(high);
    low = htonl(low);

    // Ensure network (big!) endianess
    memcpy(buffer, &high, sizeof(high));
    memcpy(buffer + 4, &low, sizeof(low));

    // Copy to value
    uint64_t value;
    memcpy(&value, buffer, sizeof(value));
    return value;
}

// Converts 64-bit value to host endianess
uint64_t ntohll(uint64_t netlong)
{
    char buffer[8];
    memcpy(buffer, &netlong, sizeof(netlong));

    // Obtain low and high words
    uint32_t high, low;
    memcpy(&high, buffer, sizeof(high));
    memcpy(&low, buffer + 4, sizeof(low));

    // Enforce host ordering on words
    high = ntohl(high);
    low = ntohl(low);

    // Combine high/low parts according to host ordering
    return ((uint64_t) high) << 32 | low;
}


// Repeatedly, if necessary, calls recv to gather all the expected data
// In case of an error, prints error message and returns -1.
int recv_total(int fd, void* buffer, int size)
{
    ssize_t total = 0;
    char* buf = buffer;
    while (total < size)
    {
        char* dest = buf + total;
        int rest = size - total;
        ssize_t n = recv(fd, dest, rest, 0);
        if (n < 0)
        {
            perror("recv error");
            return -1;
        }
        total += n;
    }
    return 0;
}


int send_total(int fd, void* buffer, int size)
{
    ssize_t total = 0;
    char* buf = buffer;
    while (total < size)
    {
        char* src = buf + total;
        int rest = size - total;
        ssize_t n = send(fd, src, rest, 0);
        if (n < 0)
        {
            perror("send error");
            return -1;
        }
        total += n;
    }
    return 0;
}
