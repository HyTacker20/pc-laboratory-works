import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class TrojkatPascalaGUI extends JFrame {
    private JTextField wierszField;
    private JTextField elementField;
    private JButton obliczButton;
    private JTextArea wynikArea;
    private JPanel panelWyniku;

    private final String PROGRAM_PATH = "out/production/lista_4/lista_1/trojkat";

    public TrojkatPascalaGUI() {
        setTitle("Wiersz Trójkąta Pascala");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 248, 255));

        JPanel panelWprowadzania = new JPanel(new GridLayout(3, 2, 10, 10));
        panelWprowadzania.setBackground(new Color(240, 248, 255));
        panelWprowadzania.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel wierszLabel = new JLabel("Numer wiersza (n):");
        wierszLabel.setFont(new Font("Arial", Font.BOLD, 14));
        wierszField = new JTextField(5);
        wierszField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel elementLabel = new JLabel("Numer elementu (m) (opcjonalnie):");
        elementLabel.setFont(new Font("Arial", Font.BOLD, 14));
        elementField = new JTextField(5);
        elementField.setFont(new Font("Arial", Font.PLAIN, 14));

        obliczButton = new JButton("Oblicz");
        obliczButton.setFont(new Font("Arial", Font.BOLD, 14));
        obliczButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uruchomObliczenia();
            }
        });

        panelWprowadzania.add(wierszLabel);
        panelWprowadzania.add(wierszField);
        panelWprowadzania.add(elementLabel);
        panelWprowadzania.add(elementField);
        panelWprowadzania.add(new JLabel());
        panelWprowadzania.add(obliczButton);

        panelWyniku = new JPanel(new BorderLayout());
        panelWyniku.setBorder(BorderFactory.createTitledBorder("Wynik"));

        wynikArea = new JTextArea(10, 30);
        wynikArea.setEditable(false);
        wynikArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        wynikArea.setBackground(new Color(248, 248, 255));

        JScrollPane scrollPane = new JScrollPane(wynikArea);
        panelWyniku.add(scrollPane, BorderLayout.CENTER);

        add(panelWprowadzania, BorderLayout.NORTH);
        add(panelWyniku, BorderLayout.CENTER);

        pack();
        setSize(500, 400);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(400, 300));
    }

    private void uruchomObliczenia() {
        try {
            String numerWiersza = wierszField.getText().trim();
            String numerElementu = elementField.getText().trim();

            if (numerWiersza.isEmpty()) {
                showError("Pole numeru wiersza nie może być puste");
                return;
            }

            int n, m = -1;
            int[] numbersM = null;
            try {
                n = Integer.parseInt(numerWiersza);
                if (!numerElementu.isEmpty()) {
                    String[] partsM = numerElementu.trim().split("\\s+");
                    numbersM = new int[partsM.length];
                    for (int i = 0; i < partsM.length; i++) {
                        numbersM[i] = Integer.parseInt(partsM[i]);
                    }
                }
            } catch (NumberFormatException e) {
                showError("Wprowadzone wartości muszą być liczbami całkowitymi");
//                showError(e.getMessage());
                return;
            }

            List<String> command = new ArrayList<>();
            command.add(PROGRAM_PATH);
            command.add(String.valueOf(n));

//            if (numbersM > 0) {
//                command.add(String.valueOf(m));
//            }

            for (int i = 0; i < numbersM.length; i++) {
                command.add(String.valueOf(numbersM[i]));
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                wynikArea.setText(output.toString());
            } else {
                wynikArea.setText("Błąd wykonania programu (kod: " + exitCode + ")\n" + output.toString());
            }

        } catch (IOException e) {
            showError("Błąd wejścia/wyjścia: " + e.getMessage());
        } catch (InterruptedException e) {
            showError("Proces został przerwany: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            showError("Wystąpił nieoczekiwany błąd: " + e.getMessage());
        }
    }


    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Błąd", JOptionPane.ERROR_MESSAGE);
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TrojkatPascalaGUI app = new TrojkatPascalaGUI();
                app.setVisible(true);
            }
        });
    }
}