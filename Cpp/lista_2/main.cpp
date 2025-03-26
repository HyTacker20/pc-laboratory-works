#include <iostream>
#include "ArabRzym.h"

int main(int argc, char *argv[]) {
    if (argc < 2) {
        throw ArabRzymException("Napisz liczbę arabską lub rzymską jako argument.");
    }
    for (int i = 1; i < argc; ++i) {
        try {
            std::string input = argv[i];
            for (char &c: input) c = toupper(c);

            if (ArabRzym::isValidRzym(input)) {
                int result = ArabRzym::rzym2arab(input);
                printf("%s -> %i\n", input.c_str(), result);
            } else if (ArabRzym::isValidArab(input)) {
                int arab = std::stoi(input);
                std::string result = ArabRzym::arab2rzym(arab);
                printf("%i -> %s\n", arab, result.c_str());
            } else {
                throw ArabRzymException("Nieprawidłowa dana: " + input);
            }
        } catch (ArabRzymException e) {
            std::cout << "Error: " << e.message << std::endl;
        }
    }

    return 0;
}
