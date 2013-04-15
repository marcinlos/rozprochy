
#include "DIIParser.hpp"

namespace rozprochy {
namespace lab3 {


void DIIParser::init_map_()
{
    handlers["byte"]   = &DIIParser::read_<Ice::Byte>;
    handlers["short"]  = &DIIParser::read_<Ice::Short>;
    handlers["int"]    = &DIIParser::read_<Ice::Int>;
    handlers["long"]   = &DIIParser::read_<Ice::Long>;
    handlers["float"]  = &DIIParser::read_<Ice::Float>;
    handlers["double"] = &DIIParser::read_<Ice::Double>;
    handlers["string"] = &DIIParser::read_<std::string>;

    modes["normal"] = Ice::Normal;
    modes["id"]     = Ice::Idempotent;
}


void DIIParser::build_()
{
    read_mode_();
    check_stream_();
    input >> operation >> std::ws;
    while (input.good())
    {
        process_one_();
    }
    stream->finished(inParams_);
}

void DIIParser::read_mode_()
{
    std::string word;
    input >> word >> std::ws;
    std::map<std::string, Ice::OperationMode>::iterator it = modes.find(word);
    if (it != modes.end())
    {
        mode = it->second;
    }
    else
    {
        throw DIIUnknownMode(word);
    }
}


void DIIParser::process_one_()
{
    std::string word;
    input >> word >> std::ws;
    map_iter it = handlers.find(word);
    if (it != handlers.end())
    {
        (this->*it->second)();
    }
    else
    {
        throw DIIUnknownType(word);
    }
}

void DIIParser::check_stream_()
{
    if (input.eof())
    {
        throw DIIIncomplete();
    }
    else if (input.fail())
    {
        input.clear();
        std::string word;
        input >> word >> std::ws;
        throw DIIInvalidValue(word);
    }
}


} // lab3
} // rozprochy
