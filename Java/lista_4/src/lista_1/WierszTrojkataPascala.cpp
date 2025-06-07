#include "WierszTrojkataPascala.h"

using namespace std;

WierszTrojkataPascala::WierszTrojkataPascala(int n) {
    if (n < 0) {
        throw invalid_argument(to_string(n) + " - nieprawidłowy numer wiersza");
    }
    size = n + 1;
    tablica = new int[size];
    obliczenieNtegoWiersza(n);
}

WierszTrojkataPascala::~WierszTrojkataPascala() {
    delete[] tablica;
}

int WierszTrojkataPascala::MtyElementWiersza(int m) {
    if (m < 0 || m >= size) {
        throw out_of_range(to_string(m) + " - liczba spoza zakresu");
    }
    return tablica[m];
}

void WierszTrojkataPascala::obliczenieNtegoWiersza(int n) {
    if (n == 0) {
        tablica[0] = 1;
        return;
    }

    if (n == 1) {
        tablica[0] = 1;
        tablica[1] = 1;
        return;
    }

    WierszTrojkataPascala poprzedni_wiersz(n - 1);

    for (int i = 0; i <= n; ++i) {
        if (i == 0 || i == n) {
            tablica[i] = 1;
        } else {
            tablica[i] = poprzedni_wiersz.tablica[i - 1] + poprzedni_wiersz.tablica[i];
        }
    }
}