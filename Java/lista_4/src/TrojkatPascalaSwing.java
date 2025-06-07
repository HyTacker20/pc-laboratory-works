import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigInteger;

public class TrojkatPascalaSwing extends JFrame {
    private JTextField poleRozmiaru;
    private JButton przyciskGeneruj;
    private JPanel panelTrojkata;
    private JScrollPane scrollPane;

    public TrojkatPascalaSwing() {
        super("Trojkat Pascala Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        JPanel gornyPanel = new JPanel();
        gornyPanel.setBackground(Color.lightGray);

        JLabel etykieta = new JLabel("Rozmiar trójkąta:");
        etykieta.setFont(new Font("Arial", Font.BOLD, 14));
        gornyPanel.add(etykieta);

        poleRozmiaru = new JTextField(5);
        poleRozmiaru.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    rysujTrojkat();
                }
            }
        });
        gornyPanel.add(poleRozmiaru);

        przyciskGeneruj = new JButton("Pokaż trójkąt");
        przyciskGeneruj.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rysujTrojkat();
            }
        });
        gornyPanel.add(przyciskGeneruj);

        add(gornyPanel, BorderLayout.NORTH);

        panelTrojkata = new JPanel();
        panelTrojkata.setLayout(new BoxLayout(panelTrojkata, BoxLayout.Y_AXIS));
        panelTrojkata.setBackground(new Color(240, 240, 255));

        scrollPane = new JScrollPane(panelTrojkata);
        add(scrollPane, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void rysujTrojkat() {
        try {
            if(poleRozmiaru.getText().trim().equals("")) {
                JOptionPane.showMessageDialog(this, "Wpisz rozmiar!",
                        "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int rozmiar = Integer.parseInt(poleRozmiaru.getText());

            if(rozmiar <= 0) {
                JOptionPane.showMessageDialog(this, "Rozmiar musi być większy od zera!",
                        "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if(rozmiar > 50) {
                JOptionPane.showMessageDialog(this, "Za duży rozmiar - max 50!",
                        "Błąd", JOptionPane.ERROR_MESSAGE);
                return;
            }

            panelTrojkata.removeAll();

            BigInteger[][] trojkat = generujTrojkat(rozmiar);

            JPanel trojkatPanel = new JPanel();
            trojkatPanel.setLayout(new BoxLayout(trojkatPanel, BoxLayout.Y_AXIS));
            trojkatPanel.setBackground(new Color(240, 240, 255));

            int maxSzer = String.valueOf(trojkat[rozmiar-1][rozmiar/2]).length();

            for(int i = 0; i < rozmiar; i++) {
                JPanel wierszPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                wierszPanel.setBackground(new Color(240, 240, 255));

                for(int j = 0; j <= i; j++) {
                    String wartosc = trojkat[i][j].toString();
                    JLabel liczba = new JLabel(String.format("%" + maxSzer + "s", wartosc));
                    liczba.setFont(new Font("Monospaced", Font.PLAIN, 14));
                    liczba.setForeground(new Color(0, 0, 150));
                    liczba.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
                    wierszPanel.add(liczba);
                }

                trojkatPanel.add(wierszPanel);
            }

            panelTrojkata.add(trojkatPanel);
            panelTrojkata.revalidate();
            panelTrojkata.repaint();

            scrollPane.getVerticalScrollBar().setValue(0);
            scrollPane.getHorizontalScrollBar().setValue(0);

        } catch(NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "To nie jest prawidłowa liczba!",
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Wystąpił błąd: " + e.getMessage(),
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BigInteger[][] generujTrojkat(int rozmiar) {
        BigInteger[][] wynik = new BigInteger[rozmiar][];

        for(int i = 0; i < rozmiar; i++) {
            wynik[i] = new BigInteger[i+1];
            wynik[i][0] = BigInteger.ONE;

            for(int j = 1; j < i; j++) {
                wynik[i][j] = wynik[i-1][j-1].add(wynik[i-1][j]);
            }

            if(i > 0) {
                wynik[i][i] = BigInteger.ONE;
            }
        }

        return wynik;
    }

    public static void main(String[] args) {
        new TrojkatPascalaSwing();
    }
}