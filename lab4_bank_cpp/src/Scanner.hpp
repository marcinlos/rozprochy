
#ifndef SCANNER_HPP_
#define SCANNER_HPP_

#include <istream>
#include <stdexcept>

namespace rozprochy
{
namespace lab4
{

struct ParseError : public std::runtime_error
{
    ParseError()
    : std::runtime_error("parse error")
    { }
};


class Scanner
{
private:
    std::istream& input_;

public:
    Scanner(std::istream& input)
    : input_(input)
    { }

    std::string next()
    {
        std::string s;
        input_ >> s;
        if (s.empty())
        {
            throw ParseError();
        }
        return s;
    }

    int nextInt()
    {
        int n;
        input_ >> n;
        if (input_.fail())
        {
            throw ParseError();
        }
        return n;
    }
};

} /* namespace lab4 */
} /* namespace rozprochy */
#endif /* SCANNER_HPP_ */
