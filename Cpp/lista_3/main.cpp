#include "figures.h"
#include <iostream>
#include <vector>

using namespace std;

class Figura;

int main(int argc, char* argv[]) {
    vector<string> args;
    for (int i = 1; i < argc; i++) {
        args.push_back(argv[i]);
    }

    try {
        vector<Figura*> figury;
        int i = 0;
        while (i < args.size()) {
            string typ = args[i];
            Figura* figura;

            if (typ == "o") {
                figura = utworzKolo(args, i);
                i += 2;
            } else if (typ == "p") {
                figura = utworzPieciokat(args, i);
                i += 2;
            } else if (typ == "s") {
                figura = utworzSzesciokat(args, i);
                i += 2;
            } else if (typ == "c") {
                int przesuniecie = liczPrzesuniecieCzworokata(args, i);
                figura = utworzCzworokat(args, i);
                i += przesuniecie;
            } else {
                throw invalid_argument("Nieznany typ figury: " + typ);
            }

            figury.push_back(figura);
        }

        for (Figura* f : figury) {
            cout << "Figura: " << f->nazwaFigury() << endl;
            cout << "Pole: " << f->obliczPole() << endl;
            cout << "Obwód: " << f->obliczObwod() << endl;
            cout << "------------------------" << endl;
        }
        
    } catch (const invalid_argument& e) {
        cout << "Error: " << e.what() << endl;
    } catch (const exception& e) {
        cout << "Error: " << e.what() << endl;
    }

    return 0;
}
