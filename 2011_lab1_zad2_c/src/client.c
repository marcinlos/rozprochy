#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <arpa/inet.h>


#define DEFAULT_PORT 5000
#define MSG_BUFFER 1024

// Prints usage instruction and exits with error code
static void arguments_error(void)
{
    fprintf(stderr, "Usage: client file <port> <address>\n");
    exit(-1);
}


static int choose_port(int argc, char* argv[])
{
    int port = DEFAULT_PORT;
    if (argc > 2)
    {
        if (sscanf(argv[2], "%d", &port) != 1)
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
    if (argc > 3)
    {
        address = argv[3];
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


static int open_file(const char* path)
{
    int fd = open(path, O_RDONLY);
    if (fd < 0)
    {
        char buffer[MSG_BUFFER];
        sprintf(buffer, "Cannot open() file `%s'", path);
        perror(buffer);
        exit(-1);
    }
    return 0;
}


static void safe_close(int fd)
{
    if (close(fd) < 0)
    {
        perror("close() has failed");
        exit(-1);
    }
}


// Sends file (fd) through the socket (sd)
static void send_file(int fd, int sd)
{

}


int main(int argc, char* argv[])
{
    if (argc < 2)
    {
        fprintf(stderr, "Too few arguments!\n");
        arguments_error();
    }
    // Determine connection parameters
    int port = choose_port(argc, argv);
    in_addr_t address = choose_address(argc, argv);
    const char* path = argv[1];

    int fd = open_file(path);
    int sd = create_socket(address, port);

    send_file(fd, sd);

    safe_close(sd);
    safe_close(fd);
    return 0;
}

