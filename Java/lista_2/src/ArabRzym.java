import java.util.*;

class ArabRzymException extends Exception {
    public ArabRzymException(String message) {
        super(message);
    }
}

public class ArabRzym {
    public static boolean isValidRoman(String input) {
        return input != null && input.matches("^[IVXLCDM]+$");
    }

    public static boolean isValidArabic(String input) {
        return input != null && input.matches("^\\d+$");
    }

    private static final int[] liczbyArab = {
            1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
    };

    private static final String[] liczbyRzym = {
            "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
    };

    private static int getArabic(String rzymDigit) {
        int arabValue = 0;
        for (int j = 0; j < liczbyRzym.length; j++) {
            if (liczbyRzym[j].equals(rzymDigit)) {arabValue = liczbyArab[j];}
        }
        return arabValue;
    }

    private static String getRzym(int arabicDigit) {
        String rzymValue = "";
        for (int j = 0; j < liczbyRzym.length; j++) {
            if (liczbyArab[j] == arabicDigit) {rzymValue = liczbyRzym[j];}
        }
        return rzymValue;
    }

    public static int rzym2arab(String rzym) throws ArabRzymException {
        if (rzym == null || rzym.isEmpty()) {
            throw new ArabRzymException("Wartość nie może być pusta.");
        }

        rzym = rzym.toUpperCase();

        if (!isValidRoman(rzym)) {
            throw new ArabRzymException("Nieprawidłowa liczba rzymska: " + rzym);
        }

        int wynik = 0;
        for (int i = 0; i < rzym.length(); i++) {
            String rzym_elem = String.valueOf(rzym.charAt(i));
            int arabValue = getArabic(rzym_elem);

            if (i != rzym.length() - 1) {
                String possible_rzym = rzym_elem + rzym.charAt(i + 1);
                if (getArabic(possible_rzym) != 0) {
                    arabValue = getArabic(possible_rzym);
                    i += 1;
                }
            }
            wynik += arabValue;
        }
        return wynik;
    }

     public static String arab2rzym(int arab) throws ArabRzymException {
         String result = "";

         while (arab != 0){
             for (int i = 0; i < liczbyArab.length; i++) {
                 while (liczbyArab[i] <= arab){
                     arab -= liczbyArab[i];
                     result += getRzym(liczbyArab[i]);
                 }
             }
         }

         return result;
     }
}