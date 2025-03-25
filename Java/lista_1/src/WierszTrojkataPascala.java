public class WierszTrojkataPascala {
    private final int[] tablica;

    public WierszTrojkataPascala(int n) {
        if (n < 0) {
            throw new IllegalArgumentException(n + " - Nieprawidłowy numer wiersza");
        }
        this.tablica = obliczenieNtegoWiersza(n);
    }

    public int MtyElementWiersza(int m) {
        if (m < 0 || m >= tablica.length) {
            throw new IndexOutOfBoundsException(m + " - liczba spoza zakresu");
        }
        return tablica[m];
    }

    private int[] obliczenieNtegoWiersza(int n) {
        int[] wiersz = new int[n + 1];

        if (n == 0) {
            wiersz[0] = 1;
            return wiersz;
        }

        if (n == 1) {
            wiersz[0] = 1;
            wiersz[1] = 1;
            return wiersz;
        }

        int[] poprzedni_wiersz = new WierszTrojkataPascala(n - 1).getTablica();

        for (int i = 0; i <= n; i++) {
            if (i == 0 || i == n) {
                wiersz[i] = 1;
            } else {
                wiersz[i] = poprzedni_wiersz[i - 1] + poprzedni_wiersz[i];
            }
        }
        return wiersz;
    }

    public int[] getTablica() {
        return tablica;
    }
}