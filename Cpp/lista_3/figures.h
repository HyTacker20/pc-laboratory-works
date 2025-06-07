#ifndef FIGURES_H
#define FIGURES_H

#include <string>
#include <vector>

using namespace std;

class Figura {
protected:
    string nazwa;

public:
    virtual double obliczPole() = 0;
    virtual double obliczObwod() = 0;

    string nazwaFigury() {
        return this->nazwa;
    }

    virtual ~Figura() {}
};

class Czworokat : public Figura {
protected:
    double bok1;
    double bok2;
    double bok3;
    double bok4;
    double kat;

public:
    double obliczObwod() override {
        return this->bok1 + this->bok2 + this->bok3 + this->bok4;
    }

    virtual ~Czworokat() {}
};

class Kolo : public Figura {
protected:
    double promien;

public:
    Kolo(double promien);
    double obliczPole() override;
    double obliczObwod() override;
};

class Pieciokat : public Figura {
protected:
    double bok;

public:
    Pieciokat(double bok);
    double obliczPole() override;
    double obliczObwod() override;
};

class Szesciokat : public Figura {
protected:
    double bok;

public:
    Szesciokat(double bok);
    double obliczPole() override;
    double obliczObwod() override;
};

class Kwadrat : public Czworokat {
public:
    Kwadrat(double bok);
    double obliczPole() override;
};

class Prostokat : public Czworokat {
public:
    Prostokat(double bok1, double bok2);
    double obliczPole() override;
};

class Romb : public Czworokat {
public:
    Romb(double bok, double kat);
    double obliczPole() override;
};

bool isParsowalnaLiczba(const string& s);
double parseParam(const vector<string>& args, int index);
Figura* utworzKolo(const vector<string>& args, int i);
Figura* utworzPieciokat(const vector<string>& args, int i);
Figura* utworzSzesciokat(const vector<string>& args, int i);
Figura* utworzCzworokat(const vector<string>& args, int i);
bool czyPelnyOpisCzworokata(int remaining, const vector<string>& args, int i);
bool czySkroconyOpisCzworokata(int remaining, const vector<string>& args, int i);
Figura* utworzCzworokatZ5Parametrow(const vector<string>& args, int i);
Figura* utworzCzworokatZ2Parametrow(const vector<string>& args, int i);
int liczPrzesuniecieCzworokata(const vector<string>& args, int i);

#endif