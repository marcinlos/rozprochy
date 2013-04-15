
#ifndef DIIPARSER_HPP_
#define DIIPARSER_HPP_

#include <vector>
#include <map>
#include <sstream>
#include <stdexcept>
#include <Ice/Config.h>
#include <Ice/Stream.h>
#include <Ice/Initialize.h>


namespace rozprochy {
namespace lab3 {

struct DIIException : std::runtime_error
{
    DIIException()
    : std::runtime_error("DII failure")
    { }

    ~DIIException() throw () { }
};

#define DII_EXCEPTION_STRING(name, field)           \
    struct name : DIIException                      \
    {                                               \
        std::string field;                          \
        name(const std::string& field)              \
        : field(field)                              \
        { }                                         \
                                                    \
        ~name() throw () { }                        \
    };

DII_EXCEPTION_STRING(DIIUnknownType, type)
DII_EXCEPTION_STRING(DIIUnknownMode, mode)
DII_EXCEPTION_STRING(DIIInvalidValue, value)

struct DIIIncomplete : DIIException { };


class DIIParser
{
private:
    std::istream& input;
    Ice::OutputStreamPtr stream;
    std::vector<Ice::Byte> inParams_, outParams_;
    std::string operation;
    Ice::OperationMode mode;

    typedef void (DIIParser::*handler)();
    std::map<std::string, handler> handlers;
    typedef std::map<std::string, handler>::iterator map_iter;

    std::map<std::string, Ice::OperationMode> modes;

    void init_map_();
    void build_();
    void read_mode_();
    void process_one_();

    void check_stream_();

    template <typename T>
    void read_()
    {
        T value;
        check_stream_();
        input >> value;
        if (input.fail())
        {
            input.clear();
            std::string word;
            input >> word >> std::ws;
            throw DIIInvalidValue(word);
        }
        stream->write(value);
    }

public:
    DIIParser(std::istream& input, Ice::CommunicatorPtr ice)
    : input(input), stream(Ice::createOutputStream(ice))
    {
        init_map_();
        build_();
    }

    std::vector<Ice::Byte>& inParams() { return inParams_; }
    std::vector<Ice::Byte>& outParams() { return outParams_; }

    bool operator () (Ice::ObjectPrx obj)
    {
        return obj->ice_invoke(operation, mode, inParams_, outParams_);
    }
};



} // lab3
} // rozprochy

#endif /* DIIPARSER_HPP_ */
