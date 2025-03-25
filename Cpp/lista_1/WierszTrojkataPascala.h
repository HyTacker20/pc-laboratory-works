#ifndef WIERSZTROJKATAPASCALA_H
#define WIERSZTROJKATAPASCALA_H

#include <iostream>

class WierszTrojkataPascala {
public:
    explicit WierszTrojkataPascala(int n);
    ~WierszTrojkataPascala();
    int MtyElementWiersza(int m);
    int* tablica;
    int size;

private:
    void obliczenieNtegoWiersza(int n);
};

#endif