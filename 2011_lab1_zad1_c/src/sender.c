#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <ctype.h>
#include "net.h"


#define MAX_LINE 256

// Connection parameters - global to avoid threading them through
// the call stack
int port;
in_addr_t address;


// Prints usage instruction and exits with error code
static void arguments_error(void)
{
    fprintf(stderr, "Usage: <port> <address> \n");
    exit(-1);
}


static int choose_port(int argc, char* argv[])
{
    int port = DEFAULT_PORT;
    if (argc > 1)
    {
        if (sscanf(argv[1], "%d", &port) != 1)
        {
            fprintf(stderr, "Invalid port number: `%s'\n", argv[1]);
            arguments_error();
        }
    }
    else
    {
        printf("Port number missing, using default port %d\n", port);
    }
    return port;
}


static in_addr_t choose_address(int argc, char* argv[])
{
    const char* address = "127.0.0.1";;
    if (argc > 2)
    {
        address = argv[2];
    }
    else
    {
        printf("Address missing, using local address %s\n", address);
    }

    in_addr_t addr = inet_addr(address);
    if (addr == INADDR_NONE)
    {
        fprintf(stderr, "Invalid address: `%s'\n", address);
        arguments_error();
    }
    return addr;
}


// Establishes a connection to given address:port endpoint
static int create_socket(in_addr_t address, int port)
{
    int fd = socket(AF_INET, SOCK_STREAM, 0);
    if (fd < 0)
    {
        perror("socket creation has failed");
        exit(-1);
    }

    struct sockaddr_in sv_addr;
    memset(&sv_addr, 0, sizeof(sv_addr));

    sv_addr.sin_family = AF_INET;
    sv_addr.sin_addr.s_addr = address;
    sv_addr.sin_port = htons(port);

    printf("Connecting...\n");
    if (connect(fd, (struct sockaddr*) &sv_addr, sizeof(sv_addr)) < 0)
    {
        perror("connect() has failed");
        exit(-1);
    }
    printf("Connected\n");
    return fd;
}


void print_help(void)
{
    const char* help_string =
        "Usage: \n"
        "    help           - prints this usage description\n"
        "    exit           - finishes program execution\n"
        "    <c><num>       - sends a request to the server\n"
        "\n"
        "  Possible values of <c>:\n"
        "     b - 1 byte\n"
        "     s - 2 bytes (short)\n"
        "     w - 4 bytes (word)\n"
        "     d - 8 bytes (double word)\n";
    puts(help_string);
}


char* trim(char* s)
{
    while (isspace(*s)) ++ s;
    int length = strlen(s), i = length - 1;
    for (; i >= 0 && isspace(s[i]); -- i);
    if (i < length - 1)
        s[i + 1] = '\0';
    return s;
}


void wait_for_response(int fd, int size)
{
    printf("Waiting for %d bytes\n", size);
    char buffer[8];
    if (recv_total(fd, buffer, size) < 0)
    {
        return;
    }
    uint64_t value;

    if (size == 1)
        value = buffer[0];
    else if (size == 2)
    {
        uint16_t n;
        memcpy(&n, buffer, sizeof(n));
        value = ntohs(n);
    }
    else if (size == 4)
    {
        uint32_t n;
        memcpy(&n, buffer, sizeof(n));
        value = ntohl(n);
    }
    else if (size == 8)
    {
        uint64_t n;
        memcpy(&n, buffer, sizeof(n));
        value = ntohll(n);
    }

    printf("Returned value: %ld\n", value);
}


int fill_buffer(char* buffer, uint64_t value, char c)
{
    buffer[0] = c;
    if (c == 'b')
    {
        uint8_t v = value;
        buffer[1] = v;
        return 1 + sizeof(v);
    }
    else if (c == 's')
    {
        uint16_t v = htons(value);
        memcpy(buffer + 1, &v, sizeof(v));
        return 1 + sizeof(v);
    }
    else if (c == 'w')
    {
        uint32_t v = htonl(value);
        memcpy(buffer + 1, &v, sizeof(v));
        return 1 + sizeof(v);
    }
    else
    {
        uint64_t v = htonll(value);
        memcpy(buffer + 1, &v, sizeof(v));
        return 1 + sizeof(v);
    }
}


void send_value(uint64_t value, char c)
{
    char buffer[9];
    int size = fill_buffer(buffer, value, c);

    int fd = create_socket(address, port);
    // Send the size indicator + data
    send_total(fd, buffer, size);
    // Wait for processed data
    wait_for_response(fd, size - 1);
    close_socket(fd);
}


int interpret_send(const char* s)
{
    uint64_t value;
    if (sscanf(s + 1, "%lu", &value) != 1)
    {
        fprintf(stderr, "Invalid value `%s'\n", s + 1);
        return -1;
    }
    switch (*s)
    {
    case 'b':
    case 's':
    case 'w':
    case 'd': send_value(value, *s); return 0;
    default: return -1;
    }
}


void input_loop(void)
{
    char buffer[MAX_LINE];
    printf(">>> ");
    while (fgets(buffer, sizeof(buffer), stdin) != NULL)
    {
        const char* line = trim(buffer);
        if (strcmp(line, "help") == 0)
        {
            print_help();
        }
        else if (strcmp(line, "exit") == 0)
        {
            break;
        }
        else interpret_send(line);
        printf(">>> ");
    }
}


int main(int argc, char* argv[])
{
    // Determin connection parameters
    port = choose_port(argc, argv);
    address = choose_address(argc, argv);

    input_loop();
    return 0;
}

