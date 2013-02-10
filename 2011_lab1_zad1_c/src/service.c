#include <stdio.h>
#include "service.h"


uint8_t process_1b(uint8_t value)
{
    return value + 1;
}

uint16_t process_2b(uint16_t value)
{
    printf("Got: %hu\n", value);
    return value + 1;
}

uint32_t process_4b(uint32_t value)
{
    printf("Got: %u\n", value);
    return value + 1;
}

uint64_t process_8b(uint64_t value)
{
    printf("Got: %lu\n", value);
    return value + 1;
}


