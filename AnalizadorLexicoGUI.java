/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codigo;

/**
 *
 * @author Alec
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import javax.swing.text.DefaultHighlighter;

public class AnalizadorLexicoGUI extends JFrame {

    private JTable tablaTokens;
    private DefaultTableModel modeloTabla;
    private JTextArea textoingresado;

    public AnalizadorLexicoGUI() {
        setTitle("Analizador Léxico");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JLabel titulo = new JLabel("-Analizador Léxico-", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        titulo.setOpaque(true);
        titulo.setBackground(new Color(209, 193, 243));
        titulo.setForeground(Color.BLACK);
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        add(titulo, BorderLayout.NORTH);
        // Para el panel del código
        textoingresado = new JTextArea();
        textoingresado.setText("Robot r1\n" + "r1.iniciar\n" + "r1.velocidad=50\n" + "r1.base=180\n" + "r1.cuerpo=45\n" + "r1.garra=90\n" + "r1.cerrarGarra()\n" + "r1.finalizar");
        textoingresado.setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollCodigo = new JScrollPane(textoingresado);
        scrollCodigo.setBorder(BorderFactory.createTitledBorder("Lenguaje artificial ingresado"));
        // Panel para la tabla de los tokens
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("TOKEN");
        modeloTabla.addColumn("TIPO");
        modeloTabla.addColumn("VALOR");
        tablaTokens = new JTable(modeloTabla) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                String tipo = (String) getValueAt(row, 1); // Columna "TIPO"
                if ("DESCONOCIDA".equals(tipo)) {
                    comp.setBackground(new Color(255, 153, 153)); // Rojo claro para tokens inválidos
                } else {
                    comp.setBackground(Color.WHITE); // Fondo normal
                }
                return comp;
            }
        };

        JScrollPane scrollTabla = new JScrollPane(tablaTokens);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Tokens identificados"));
        // Botón para analizar el lenguaje ingresado
        JButton botonAnalizar = new JButton("Analizar Lenguaje");
        botonAnalizar.addActionListener(e -> analizarCodigo());
        // Organización de los componentes
        JPanel panelPrincipal = new JPanel(new GridLayout(2, 1));
        panelPrincipal.setBackground(new Color(125, 253, 193));
        panelPrincipal.add(scrollCodigo);
        panelPrincipal.add(scrollTabla);
        add(panelPrincipal, BorderLayout.CENTER);
        add(botonAnalizar, BorderLayout.SOUTH);
    }

    private void analizarCodigo() {
        // Para limpiar la tabla
        modeloTabla.setRowCount(0);
        textoingresado.getHighlighter().removeAllHighlights();
        AnalizadorLexico lexer = new AnalizadorLexico();
        try {
            // Para analizar el lenguaje
            List<AnalizadorLexico.Token> tokens = lexer.analizar(textoingresado.getText());
            // Para llenar la tabla de tokens
            int posicionBusqueda = 0;

            for (AnalizadorLexico.Token token : tokens) {
                modeloTabla.addRow(new Object[]{
                    token.valor,
                    token.tipo,
                    obtenerValorToken(token)
                });
                if (token.tipo.equals("DESCONOCIDA")) {
                    try {
                        int start = textoingresado.getText().indexOf(token.valor, posicionBusqueda);
                        if (start >= 0) {
                            int end = start + token.valor.length();
                            textoingresado.getHighlighter().addHighlight(start, end,
                                    new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 153, 153))); // Rojo claro
                            posicionBusqueda = end; // Actualizamos la posición de búsqueda
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }

        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String obtenerValorToken(AnalizadorLexico.Token token) {
        if (token.tipo.equals("NUMERO_ENTERO")) {
            return token.valor;
        } else if (token.tipo.equals("METODO") && token.valor.matches("base|cuerpo|garra|velocidad")) {
            return "1";
        }
        return "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AnalizadorLexicoGUI interfaz = new AnalizadorLexicoGUI();
            interfaz.setVisible(true);
        });
    }
}

class AnalizadorLexico {

    private final List<ReglaToken> reglas = new ArrayList<>();

    private static class ReglaToken {

        final Pattern patronRegex;
        final String tipoToken;

        ReglaToken(String regex, String tipoToken) {
            this.patronRegex = Pattern.compile("^" + regex);
            this.tipoToken = tipoToken;
        }
    }

    public AnalizadorLexico() {
        // Reglas para el lenguaje del brazo robótico
        agregarRegla("\\b(Robot)\\b", "OBJETO_ROBOT");
        agregarRegla("\\b(iniciar|detener|cerrarGarra|abrirGarra|repetir|finalizar)\\b", "ACCION");
        agregarRegla("\\b(b1|r1)\\b", "IDENTIFICADOR");
        agregarRegla("\\b(base|cuerpo|garra|velocidad)\\b", "METODO");
        agregarRegla("\\d+", "NUMERO_ENTERO");
        agregarRegla("\\.", "OPERADOR_PUNTO");
        agregarRegla("=", "OPERADOR_ASIGNACION");
        agregarRegla("[(),;]", "DELIMITADOR");
        agregarRegla("\\s+", "ESPACIO");
        agregarRegla("//.*", "COMENTARIO");
    }

    public void agregarRegla(String regex, String tipoToken) {
        reglas.add(new ReglaToken(regex, tipoToken));
    }

    public List<Token> analizar(String codigoFuente) {
        List<Token> tokens = new ArrayList<>();
        int posicionActual = 0;
        int longitudCodigo = codigoFuente.length();
        while (posicionActual < longitudCodigo) {
            boolean coincide = false;
            for (ReglaToken regla : reglas) {
                Matcher emparejador = regla.patronRegex.matcher(codigoFuente.substring(posicionActual));
                if (emparejador.find()) {
                    String valorToken = emparejador.group();
                    if (!regla.tipoToken.equals("ESPACIO") && !regla.tipoToken.equals("COMENTARIO")) {
                        tokens.add(new Token(regla.tipoToken, valorToken));
                    }
                    posicionActual += valorToken.length();
                    coincide = true;
                    break;
                }
            }
            if (!coincide) {
                String valorToken = String.valueOf(codigoFuente.charAt(posicionActual));
                tokens.add(new Token("DESCONOCIDA", valorToken));
                posicionActual++;
            }
        }
        return tokens;
    }

    public static class Token {

        public final String tipo;
        public final String valor;

        public Token(String tipo, String valor) {
            this.tipo = tipo;
            this.valor = valor;
        }
    }
}
