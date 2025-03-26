public class Main {
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
//                throw new ArabRzymException("Napisz liczbę arabską lub rzymską jako argument.");
                for (int i = 1; i <= 4000; i++) {
                    String rzymNumber = ArabRzym.arab2rzym(i);
                    System.out.println(i + " -> " + rzymNumber);
                    System.out.println(rzymNumber + " -> " + ArabRzym.rzym2arab(rzymNumber));
                }
                return;
            }

            String input = args[0].toUpperCase();

            if (ArabRzym.isValidRoman(input)) {
                int result = ArabRzym.rzym2arab(input);
                System.out.println(input + " -> " + result);
            } else if (ArabRzym.isValidArabic(input)) {
                int arabic = Integer.parseInt(input);
                String result = ArabRzym.arab2rzym(arabic);
                System.out.println(input + " -> " + result);
            } else {
                throw new ArabRzymException("Nieprawidłowa dana: " + input);
            }

        } catch (ArabRzymException e) {
            System.out.println("Error: " + e.getMessage());
//        } catch (NumberFormatException e) {
//            System.out.println("Błąd: Nieprawidłowy format liczby arabskiej.");
        }
    }
}