import java.util.ArrayList;

public class Figury {

    interface FiguraJedenParametrInterface {
        double obliczPole();
        double obliczObwod();
        String podajNazwe();
    }

    interface FiguraDwaParametryInterface {
        double obliczPole();
        double obliczObwod();
        String podajNazwe();
    }

    public enum FiguryJedenParametr implements FiguraJedenParametrInterface {
        KOLO {
            private double promien;

            public void ustawParametr(double promien) {
                this.promien = promien;
            }

            public double obliczPole() {
                return 3.1415 * Math.pow(this.promien, 2);
            }

            public double obliczObwod() {
                return 2 * 3.1415 * this.promien;
            }

            public String podajNazwe() {
                return "Kolo";
            }
        },

        KWADRAT {
            private double bok;

            public void ustawParametr(double bok) {
                this.bok = bok;
            }

            public double obliczPole() {
                return Math.pow(this.bok, 2);
            }

            public double obliczObwod() {
                return 4 * this.bok;
            }

            public String podajNazwe() {
                return "Kwadrat";
            }
        },

        PIECIOKAT {
            private double bok;

            public void ustawParametr(double bok) {
                this.bok = bok;
            }

            public double obliczPole() {
                return Math.sqrt(5 * (5 + 2 * Math.sqrt(2)) * Math.pow(this.bok, 2)) / 4;
            }

            public double obliczObwod() {
                return 5 * this.bok;
            }

            public String podajNazwe() {
                return "Pieciokat";
            }
        },

        SZESCIOKAT {
            private double bok;

            public void ustawParametr(double bok) {
                this.bok = bok;
            }

            public double obliczPole() {
                return (3 * Math.sqrt(3) * Math.pow(this.bok, 2)) / 2;
            }

            public double obliczObwod() {
                return 6 * this.bok;
            }

            public String podajNazwe() {
                return "Szesciokat";
            }
        };

        public abstract void ustawParametr(double param);
    }

    public enum FiguryDwaParametry implements FiguraDwaParametryInterface {
        PROSTOKAT {
            private double bok1;
            private double bok2;

            public void ustawParametry(double bok1, double bok2) {
                this.bok1 = bok1;
                this.bok2 = bok2;
            }

            public double obliczPole() {
                return this.bok1 * this.bok2;
            }

            public double obliczObwod() {
                return 2 * (this.bok1 + this.bok2);
            }

            public String podajNazwe() {
                return "Prostokat";
            }
        },

        ROMB {
            private double bok;
            private double kat;

            public void ustawParametry(double bok, double kat) {
                this.bok = bok;
                this.kat = kat;
            }

            public double obliczPole() {
                return Math.pow(this.bok, 2) * Math.sin(Math.toRadians(this.kat));
            }

            public double obliczObwod() {
                return 4 * this.bok;
            }

            public String podajNazwe() {
                return "Romb";
            }
        };

        public abstract void ustawParametry(double param1, double param2);
    }

    public static void main(String[] args) {
        ArrayList<Object> figury = new ArrayList<>();

        try {
            int i = 0;
            while (i < args.length) {
                String typ = args[i];
                Object figura;

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

            for (Object f : figury) {
                if (f instanceof FiguraJedenParametrInterface) {
                    FiguraJedenParametrInterface fig = (FiguraJedenParametrInterface) f;
                    System.out.println("Figura: " + fig.podajNazwe());
                    System.out.println("Pole: " + fig.obliczPole());
                    System.out.println("Obwód: " + fig.obliczObwod());
                } else if (f instanceof FiguraDwaParametryInterface) {
                    FiguraDwaParametryInterface fig = (FiguraDwaParametryInterface) f;
                    System.out.println("Figura: " + fig.podajNazwe());
                    System.out.println("Pole: " + fig.obliczPole());
                    System.out.println("Obwód: " + fig.obliczObwod());
                }
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

    static FiguraJedenParametrInterface utworzKolo(String[] args, int i) {
        double promien = parseParam(args, i + 1);
        FiguryJedenParametr kolo = FiguryJedenParametr.KOLO;
        kolo.ustawParametr(promien);
        return kolo;
    }

    static FiguraJedenParametrInterface utworzPieciokat(String[] args, int i) {
        double bok = parseParam(args, i + 1);
        FiguryJedenParametr pieciokat = FiguryJedenParametr.PIECIOKAT;
        pieciokat.ustawParametr(bok);
        return pieciokat;
    }

    static FiguraJedenParametrInterface utworzSzesciokat(String[] args, int i) {
        double bok = parseParam(args, i + 1);
        FiguryJedenParametr szesciokat = FiguryJedenParametr.SZESCIOKAT;
        szesciokat.ustawParametr(bok);
        return szesciokat;
    }

    static Object utworzCzworokat(String[] args, int i) {
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

    static Object utworzCzworokatZ5Parametrow(String[] args, int i) {
        double b1 = parseParam(args, i + 1);
        double b2 = parseParam(args, i + 2);
        double b3 = parseParam(args, i + 3);
        double b4 = parseParam(args, i + 4);
        double kat = parseParam(args, i + 5);

        if (b1 == b2 && b1 == b3 && b1 == b4 && kat == 90) {
            FiguryJedenParametr kwadrat = FiguryJedenParametr.KWADRAT;
            kwadrat.ustawParametr(b1);
            return kwadrat;
        }
        if (b1 == b3 && b2 == b4 && kat == 90) {
            FiguryDwaParametry prostokat = FiguryDwaParametry.PROSTOKAT;
            prostokat.ustawParametry(b1, b2);
            return prostokat;
        }
        if (b1 == b2 && b1 == b3 && b1 == b4) {
            FiguryDwaParametry romb = FiguryDwaParametry.ROMB;
            romb.ustawParametry(b1, kat);
            return romb;
        }

        throw new IllegalArgumentException("Nie rozpoznano czworokąta.");
    }

    static Object utworzCzworokatZ2Parametrow(String[] args, int i) {
        double bok = parseParam(args, i + 1);
        double kat = parseParam(args, i + 2);
        if (kat == 90) {
            FiguryJedenParametr kwadrat = FiguryJedenParametr.KWADRAT;
            kwadrat.ustawParametr(bok);
            return kwadrat;
        }

        FiguryDwaParametry romb = FiguryDwaParametry.ROMB;
        romb.ustawParametry(bok, kat);
        return romb;
    }

    static int liczPrzesuniecieCzworokata(String[] args, int i) {
        int remaining = args.length - i - 1;

        if (czyPelnyOpisCzworokata(remaining, args, i)) return 6;
        if (czySkroconyOpisCzworokata(remaining, args, i)) return 3;
        throw new IllegalArgumentException("Za mało parametrów dla czworokąta.");
    }
}