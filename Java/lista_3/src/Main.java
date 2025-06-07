import java.util.ArrayList;

interface FiguraInterface {
    double obliczPole();
    double obliczObwod();
    String nazwaFigury();
}

abstract class Figura implements FiguraInterface {
    protected String nazwa;

    public String nazwaFigury() {
        return this.nazwa;
    }
}

abstract class Czworokat extends Figura {
    protected double bok1;
    protected double bok2;
    protected double bok3;
    protected double bok4;
    protected double kat;

    public double obliczObwod() {
        return this.bok1 + this.bok2 + this.bok3 + this.bok4;
    }
}

class Kolo extends Figura {
    protected double promien;

    public Kolo(double promien) {
        this.nazwa = "Kolo";
        this.promien = promien;
    }

    public double obliczPole() {
        return 3.1415 * Math.pow(this.promien, 2);
    }

    public double obliczObwod() {
        return 2 * 3.1415 * this.promien;
    }
}

class Pieciokat extends Figura {
    protected double bok;

    public Pieciokat(double bok) {
        this.nazwa = "Pieciokat";
        this.bok = bok;
    }

    public double obliczPole() {
        return Math.sqrt(5 * (5 + 2 * Math.sqrt(2)) * Math.pow(this.bok, 2)) / 4;
    }

    public double obliczObwod() {
        return 5 * this.bok;
    }
}

class Szesciokat extends Figura {
    protected double bok;

    public Szesciokat(double bok) {
        this.nazwa = "Szesciokat";
        this.bok = bok;
    }

    public double obliczPole() {
        return (3 * Math.sqrt(3) * Math.pow(this.bok, 2)) / 2;
    }

    public double obliczObwod() {
        return 6 * this.bok;
    }
}

class Kwadrat extends Czworokat {
    public Kwadrat(double bok) {
        this.nazwa = "Kwadrat";
        this.bok1 = bok;
        this.bok2 = bok;
        this.bok3 = bok;
        this.bok4 = bok;
        this.kat = 90;
    }

    public double obliczPole() {
        return Math.pow(this.bok1, 2);
    }
}

class Prostokat extends Czworokat {
    public Prostokat(double bok1, double bok2) {
        this.nazwa = "Prostokat";
        this.bok1 = bok1;
        this.bok2 = bok2;
        this.bok3 = bok1;
        this.bok4 = bok2;
        this.kat = 90;
    }

    public double obliczPole() {
        return this.bok1 * this.bok2;
    }
}

class Romb extends Czworokat {
    public Romb(double bok, double kat) {
        this.nazwa = "Romb";
        this.bok1 = bok;
        this.bok2 = bok;
        this.bok3 = bok;
        this.bok4 = bok;
        this.kat = kat;
    }

    public double obliczPole() {
        return Math.pow(this.bok1, 2) * Math.sin(this.kat);
    }
}

public class Main {
    public static void main(String[] args) {
        ArrayList<Figura> figury = new ArrayList<>();

        try {
            int i = 0;
            while (i < args.length) {
                String typ = args[i];
                Figura figura;

                switch (typ) {
                    case "o":
                        figura = utworzKolo(args, i);
                        i += 2;
                        break;
                    case "p":
                        figura = utworzPieciokat(args, i);
                        i += 2;
                        break;
                    case "s":
                        figura = utworzSzesciokat(args, i);
                        i += 2;
                        break;
                    case "c":
                        int przesuniecie = liczPrzesuniecieCzworokata(args, i);
                        figura = utworzCzworokat(args, i);
                        i += przesuniecie;
                        break;
                    default:
                        throw new IllegalArgumentException("Nieznany typ figury: " + typ);
                }

                figury.add(figura);
            }

            for (Figura f : figury) {
                System.out.println("Figura: " + f.nazwaFigury());
                System.out.println("Pole: " + f.obliczPole());
                System.out.println("Obwód: " + f.obliczObwod());
                System.out.println("------------------------");
            }

        } catch (NumberFormatException e) {
            System.out.println("Error: Niepoprawny format liczby.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    static boolean isParsowalnaLiczba(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static double parseParam(String[] args, int index) {
        if (index >= args.length || !isParsowalnaLiczba(args[index])) {
            throw new IllegalArgumentException("Brakuje parametru.");
        }
        return Double.parseDouble(args[index]);
    }

    static Figura utworzKolo(String[] args, int i) {
        double promien = parseParam(args, i + 1);
        return new Kolo(promien);
    }

    static Figura utworzPieciokat(String[] args, int i) {
        double bok = parseParam(args, i + 1);
        return new Pieciokat(bok);
    }

    static Figura utworzSzesciokat(String[] args, int i) {
        double bok = parseParam(args, i + 1);
        return new Szesciokat(bok);
    }

    static Figura utworzCzworokat(String[] args, int i) {
        int remaining = args.length - i - 1;

        if (czyPelnyOpisCzworokata(remaining, args, i)) {
            return utworzCzworokatZ5Parametrow(args, i);
        }

        if (czySkroconyOpisCzworokata(remaining, args, i)) {
            return utworzCzworokatZ2Parametrow(args, i);
        }

        throw new IllegalArgumentException("Czworokat wymaga 2 lub 5 parametrów.");
    }

    static boolean czyPelnyOpisCzworokata(int remaining, String[] args, int i) {
        return remaining >= 5 &&
                isParsowalnaLiczba(args[i + 1]) &&
                isParsowalnaLiczba(args[i + 2]) &&
                isParsowalnaLiczba(args[i + 3]) &&
                isParsowalnaLiczba(args[i + 4]) &&
                isParsowalnaLiczba(args[i + 5]);
    }

    static boolean czySkroconyOpisCzworokata(int remaining, String[] args, int i) {
        return remaining >= 2 &&
                isParsowalnaLiczba(args[i + 1]) &&
                isParsowalnaLiczba(args[i + 2]);
    }

    static Figura utworzCzworokatZ5Parametrow(String[] args, int i) {
        double b1 = parseParam(args, i + 1);
        double b2 = parseParam(args, i + 2);
        double b3 = parseParam(args, i + 3);
        double b4 = parseParam(args, i + 4);
        double kat = parseParam(args, i + 5);

        if (b1 == b2 && b1 == b3 && b1 == b4 && kat == 90) return new Kwadrat(b1);
        if (b1 == b3 && b2 == b4 && kat == 90) return new Prostokat(b1, b2);
        if (b1 == b2 && b1 == b3 && b1 == b4) return new Romb(b1, Math.toRadians(kat));

        throw new IllegalArgumentException("Nie rozpoznano czworokąta.");
    }

    static Figura utworzCzworokatZ2Parametrow(String[] args, int i) {
        double bok = parseParam(args, i + 1);
        double kat = parseParam(args, i + 2);
        if (kat == 90) return new Kwadrat(bok);
        return new Romb(bok, Math.toRadians(kat));
    }

    static int liczPrzesuniecieCzworokata(String[] args, int i) {
        int remaining = args.length - i - 1;

        if (czyPelnyOpisCzworokata(remaining, args, i)) return 6;
        if (czySkroconyOpisCzworokata(remaining, args, i)) return 3;
        throw new IllegalArgumentException("Za mało parametrów dla czworokąta.");
    }
}