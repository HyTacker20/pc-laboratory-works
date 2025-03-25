import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Error! Nie podałeś argumentów.");
            return;
        }

        try {
            int n = Integer.parseInt(args[0]);
            WierszTrojkataPascala wiersz = new WierszTrojkataPascala(n);
            System.out.println("Wiersz " + n + ": " + Arrays.toString(wiersz.getTablica()));

            for (int i = 1; i < args.length; i++) {
                try {
                    int m = Integer.parseInt(args[i]);
                    System.out.println(m + " - " + wiersz.MtyElementWiersza(m));
                } catch (NumberFormatException e) {
                    System.out.println(args[i] + " - nieprawidłowa dana");
                } catch (IndexOutOfBoundsException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(args[0] + " - nieprawidłowy numer wiersza");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}