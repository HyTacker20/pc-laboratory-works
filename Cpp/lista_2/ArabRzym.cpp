#include "ArabRzym.h"
#include <cctype>

int liczbyArab[] = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
std::string liczbyRzym[] = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

ArabRzymException::ArabRzymException(std::string msg) {
    message = msg;
}

bool ArabRzym::isValidRzym(std::string input) {
    for (char rzym : input) {
        rzym = toupper(rzym);
        if (rzym != 'I' && rzym != 'V' && rzym != 'X' && rzym != 'L' &&
            rzym != 'C' && rzym != 'D' && rzym != 'M') {
            return false;
        }
    }
    return true;
}

bool ArabRzym::isValidArab(std::string input) {
    for (char arab : input) {
        if (!isdigit(arab)) return false;
    }
    return true;
}

int ArabRzym::getArabic(std::string rzymDigit) {
    int arabValue = 0;
    for (int j = 0; j < 13; j++) {
        if (liczbyRzym[j] == rzymDigit) {
            arabValue = liczbyArab[j];
        }
    }
    return arabValue;
}

std::string ArabRzym::getRzym(int arabicDigit) {
    std::string rzymValue = "";
    for (int j = 0; j < 13; j++) {
        if (liczbyArab[j] == arabicDigit) {
            rzymValue = liczbyRzym[j];
        }
    }
    return rzymValue;
}

std::string ArabRzym::arab2rzym(int arab) {
    if (arab <= 0 || arab > 4000) {
        throw ArabRzymException("Liczba z poza zakresu.");
    }

    std::string result = "";
    while (arab != 0) {
        for (int i = 0; i < 13; i++) {
            while (liczbyArab[i] <= arab) {
                arab -= liczbyArab[i];
                result += liczbyRzym[i];
            }
        }
    }

    return result;
}


int ArabRzym::rzym2arab(std::string rzym) {
    if (rzym.empty()) {
        throw ArabRzymException("Wartość pusta.");
    }

    for (int i = 0; i < rzym.size(); i++) {
        rzym[i] = toupper(rzym[i]);
    }

    if (!isValidRzym(rzym)) {
        throw ArabRzymException("Nieprawidłowa liczba rzymska: " + rzym);
    }

    int result = 0;
    for (int i = 0; i < rzym.length(); i++) {
        std::string rzym_elem(1, rzym[i]);
        int arabValue = getArabic(rzym_elem);

        if (i != rzym.length() - 1) {
            std::string possible_rzym = rzym_elem + rzym[i + 1];
            if (getArabic(possible_rzym) != 0) {
                arabValue = getArabic(possible_rzym);
                i++;
            }
        }
        result += arabValue;
    }

    if (arab2rzym(result) != rzym) {
        throw ArabRzymException("Nieprawidłowa liczba rzymska: " + rzym);
    }

    return result;
}