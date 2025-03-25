#include <iostream>
#include "WierszTrojkataPascala.h"

using namespace std;

int main(int argc, char* argv[]) {
    if (argc < 2) {
        printf("Error! Nie podałeś argumentów.");
        return 1;
    }

    try {
        int n = stoi(argv[1]);
        WierszTrojkataPascala wiersz(n);

        cout << "Wiersz " << n << ": ";
        for (int i = 0; i <= n; ++i) {
            cout << wiersz.tablica[i] << " ";
        }
        cout << endl;

        for (int i = 2; i < argc; ++i) {
            int m;
            try {
                m = stoi(argv[i]);
            } catch (const exception& e) {
                printf("%s - nieprawidłowa dana\n", argv[i]);
                continue;
            }
            try {
                int element = wiersz.MtyElementWiersza(m);
                printf("%d - %d\n", m, element);
            } catch (const exception& e) {
                printf("%s\n", e.what());
            }
        }

    } catch (const exception& e) {
        printf("%s", e.what());
    }

    return 0;
}
