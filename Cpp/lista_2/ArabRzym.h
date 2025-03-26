#ifndef ARABRZYM_H
#define ARABRZYM_H

#include <string>

class ArabRzymException {
public:
    std::string message;
    ArabRzymException(std::string msg);
};

class ArabRzym {
public:
    static bool isValidRzym(std::string input);

    static bool isValidArab(std::string input);

    static int getArabic(std::string rzymDigit);

    static std::string getRzym(int arabicDigit);

    static int rzym2arab(std::string rzym);

    static std::string arab2rzym(int arab);
};

#endif
