#include "figures.h"
#include <iostream>
#include <cmath>
#include <stdexcept>

using namespace std;

Kolo::Kolo(double promien) {
    this->nazwa = "Kolo";
    this->promien = promien;
}

double Kolo::obliczPole() {
    return 3.1415 * pow(this->promien, 2);
}

double Kolo::obliczObwod() {
    return 2 * 3.1415 * this->promien;
}

Pieciokat::Pieciokat(double bok) {
    this->nazwa = "Pieciokat";
    this->bok = bok;
}

double Pieciokat::obliczPole() {
    return sqrt(5 * (5 + 2 * sqrt(2)) * pow(this->bok, 2)) / 4;
}

double Pieciokat::obliczObwod() {
    return 5 * this->bok;
}

Szesciokat::Szesciokat(double bok) {
    this->nazwa = "Szesciokat";
    this->bok = bok;
}

double Szesciokat::obliczPole() {
    return (3 * sqrt(3) * pow(this->bok, 2)) / 2;
}

double Szesciokat::obliczObwod() {
    return 6 * this->bok;
}

Kwadrat::Kwadrat(double bok) {
    this->nazwa = "Kwadrat";
    this->bok1 = bok;
    this->bok2 = bok;
    this->bok3 = bok;
    this->bok4 = bok;
    this->kat = 90;
}

double Kwadrat::obliczPole() {
    return pow(this->bok1, 2);
}

Prostokat::Prostokat(double bok1, double bok2) {
    this->nazwa = "Prostokat";
    this->bok1 = bok1;
    this->bok2 = bok2;
    this->bok3 = bok1;
    this->bok4 = bok2;
    this->kat = 90;
}

double Prostokat::obliczPole() {
    return this->bok1 * this->bok2;
}

Romb::Romb(double bok, double kat) {
    this->nazwa = "Romb";
    this->bok1 = bok;
    this->bok2 = bok;
    this->bok3 = bok;
    this->bok4 = bok;
    this->kat = kat;
}

double Romb::obliczPole() {
    return pow(this->bok1, 2) * sin(this->kat);
}

bool isParsowalnaLiczba(const string& s) {
    try {
        stod(s);
        return true;
    } catch (const invalid_argument&) {
        return false;
    } catch (const out_of_range&) {
        return false;
    }
}

double parseParam(const vector<string>& args, int index) {
    if (index >= args.size() || !isParsowalnaLiczba(args[index])) {
        throw invalid_argument("Brakuje parametru.");
    }
    return stod(args[index]);
}

Figura* utworzKolo(const vector<string>& args, int i) {
    double promien = parseParam(args, i + 1);
    return new Kolo(promien);
}

Figura* utworzPieciokat(const vector<string>& args, int i) {
    double bok = parseParam(args, i + 1);
    return new Pieciokat(bok);
}

Figura* utworzSzesciokat(const vector<string>& args, int i) {
    double bok = parseParam(args, i + 1);
    return new Szesciokat(bok);
}

bool czyPelnyOpisCzworokata(int remaining, const vector<string>& args, int i) {
    return remaining >= 5 &&
            isParsowalnaLiczba(args[i + 1]) &&
            isParsowalnaLiczba(args[i + 2]) &&
            isParsowalnaLiczba(args[i + 3]) &&
            isParsowalnaLiczba(args[i + 4]) &&
            isParsowalnaLiczba(args[i + 5]);
}

bool czySkroconyOpisCzworokata(int remaining, const vector<string>& args, int i) {
    return remaining >= 2 &&
            isParsowalnaLiczba(args[i + 1]) &&
            isParsowalnaLiczba(args[i + 2]);
}

Figura* utworzCzworokatZ5Parametrow(const vector<string>& args, int i) {
    double b1 = parseParam(args, i + 1);
    double b2 = parseParam(args, i + 2);
    double b3 = parseParam(args, i + 3);
    double b4 = parseParam(args, i + 4);
    double kat = parseParam(args, i + 5);

    if (b1 == b2 && b1 == b3 && b1 == b4 && kat == 90) return new Kwadrat(b1);
    if (b1 == b3 && b2 == b4 && kat == 90) return new Prostokat(b1, b2);
    if (b1 == b2 && b1 == b3 && b1 == b4) return new Romb(b1, kat * M_PI / 180.0);

    throw invalid_argument("Nie rozpoznano czworokąta.");
}

Figura* utworzCzworokatZ2Parametrow(const vector<string>& args, int i) {
    double bok = parseParam(args, i + 1);
    double kat = parseParam(args, i + 2);
    if (kat == 90) return new Kwadrat(bok);
    return new Romb(bok, kat * M_PI / 180.0);
}

int liczPrzesuniecieCzworokata(const vector<string>& args, int i) {
    int remaining = args.size() - i - 1;

    if (czyPelnyOpisCzworokata(remaining, args, i)) return 6;
    if (czySkroconyOpisCzworokata(remaining, args, i)) return 3;
    throw invalid_argument("Za mało parametrów dla czworokąta.");
}

Figura* utworzCzworokat(const vector<string>& args, int i) {
    int remaining = args.size() - i - 1;

    if (czyPelnyOpisCzworokata(remaining, args, i)) {
        return utworzCzworokatZ5Parametrow(args, i);
    }

    if (czySkroconyOpisCzworokata(remaining, args, i)) {
        return utworzCzworokatZ2Parametrow(args, i);
    }

    throw invalid_argument("Czworokat wymaga 2 lub 5 parametrów.");
}