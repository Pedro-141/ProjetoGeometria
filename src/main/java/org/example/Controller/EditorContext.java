package org.example.Controller;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.Models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EditorContext {

    // -------------------------------------------------------------------------
    // Estado do editor
    // -------------------------------------------------------------------------

    private final List<FormaGeometrica> formas = new ArrayList<>();
    private final List<FormaGeometrica> formasSelecionadas = new ArrayList<>();

    private Ferramenta ferramentaAtual;
    private final HistoricoController historico = new HistoricoController();

    private String corPreenchimentoAtual = "#AAAAAA";
    private String corBordaAtual = "#000000";
    private double espessuraBordaAtual = 1.0;

    private boolean gridSnappingAtivo = false;
    private double tamanhoGrid = 20.0;

    private Canvas canvas;

    // -------------------------------------------------------------------------
    // Construtor
    // -------------------------------------------------------------------------

    public EditorContext(Canvas canvas) {
        this.canvas = canvas;
        this.ferramentaAtual = new FerramentaDesenhar();

        // Registra eventos do mouse no canvas
        canvas.setOnMousePressed(e -> ferramentaAtual.aoPressionarMouse(e, this));
        canvas.setOnMouseDragged(e -> ferramentaAtual.aoArrastarMouse(e, this));
        canvas.setOnMouseReleased(e -> ferramentaAtual.aoSoltarMouse(e, this));
        canvas.setOnMouseClicked(e -> ferramentaAtual.aoClicar(e, this));
    }

    // -------------------------------------------------------------------------
    // Gerenciamento de ferramenta ativa (State)
    // -------------------------------------------------------------------------

    public void setFerramenta(Ferramenta ferramenta) {
        this.ferramentaAtual = ferramenta;
    }

    public Ferramenta getFerramenta() {
        return ferramentaAtual;
    }

    // -------------------------------------------------------------------------
    // Gerenciamento de formas
    // -------------------------------------------------------------------------

    public void adicionarForma(FormaGeometrica forma) {
        registrarEstado();
        forma.setCorPreenchimentoHex(corPreenchimentoAtual);
        forma.setCorBordaHex(corBordaAtual);
        forma.setEspessuraBorda(espessuraBordaAtual);
        formas.add(forma);
        redesenhar();
    }

    public void removerForma(FormaGeometrica forma) {
        registrarEstado();
        formas.remove(forma);
        formasSelecionadas.remove(forma);
        redesenhar();
    }

    public void removerFormasSelecionadas() {
        if (formasSelecionadas.isEmpty()) return;
        registrarEstado();
        formas.removeAll(formasSelecionadas);
        formasSelecionadas.clear();
        redesenhar();
    }

    public List<FormaGeometrica> getFormas() {
        return formas;
    }

    // -------------------------------------------------------------------------
    // Seleção
    // -------------------------------------------------------------------------

    public void selecionarForma(FormaGeometrica forma) {
        if (!formasSelecionadas.contains(forma)) {
            formasSelecionadas.add(forma);
        }
        redesenhar();
    }

    public void desselecionarTudo() {
        formasSelecionadas.clear();
        redesenhar();
    }

    public List<FormaGeometrica> getFormasSelecionadas() {
        return formasSelecionadas;
    }

    public boolean estaSelecionada(FormaGeometrica forma) {
        return formasSelecionadas.contains(forma);
    }

    /**
     * Retorna a forma mais ao topo que contém o ponto (px, py), ou null.
     */
    public FormaGeometrica encontrarFormaNoPonto(double px, double py) {
        for (int i = formas.size() - 1; i >= 0; i--) {
            if (formas.get(i).contemPonto(px, py)) {
                return formas.get(i);
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Agrupamento / Desagrupamento
    // -------------------------------------------------------------------------

    public void agruparSelecionadas() {
        if (formasSelecionadas.size() < 2) return;
        registrarEstado();
        String novoId = UUID.randomUUID().toString();
        for (FormaGeometrica f : formasSelecionadas) {
            f.setGroupId(novoId);
        }
        redesenhar();
    }

    public void desagruparSelecionadas() {
        registrarEstado();
        for (FormaGeometrica f : formasSelecionadas) {
            f.setGroupId(null);
        }
        redesenhar();
    }

    // -------------------------------------------------------------------------
    // Camadas (ordenação Z)
    // -------------------------------------------------------------------------

    public void trazerParaFrente() {
        if (formasSelecionadas.isEmpty()) return;
        registrarEstado();
        for (FormaGeometrica f : formasSelecionadas) {
            if (formas.remove(f)) {
                formas.add(f);
            }
        }
        redesenhar();
    }

    public void enviarParaTras() {
        if (formasSelecionadas.isEmpty()) return;
        registrarEstado();
        for (FormaGeometrica f : formasSelecionadas) {
            if (formas.remove(f)) {
                formas.add(0, f);
            }
        }
        redesenhar();
    }

    // -------------------------------------------------------------------------
    // Duplicar formas selecionadas (Prototype)
    // -------------------------------------------------------------------------

    public void duplicarSelecionadas() {
        if (formasSelecionadas.isEmpty()) return;
        registrarEstado();
        List<FormaGeometrica> clones = new ArrayList<>();
        for (FormaGeometrica f : formasSelecionadas) {
            FormaGeometrica clone = f.clonar();
            clone.transladar(20, 20); // offset visual
            clones.add(clone);
        }
        formas.addAll(clones);
        formasSelecionadas.clear();
        formasSelecionadas.addAll(clones);
        redesenhar();
    }

    // -------------------------------------------------------------------------
    // Histórico (Memento)
    // -------------------------------------------------------------------------

    public void registrarEstado() {
        historico.registrarEstado(formas);
    }

    public void desfazer() {
        List<FormaGeometrica> anterior = historico.desfazer(formas);
        if (anterior != null) {
            formas.clear();
            formas.addAll(anterior);
            formasSelecionadas.clear();
            redesenhar();
        }
    }

    public void refazer() {
        List<FormaGeometrica> proximo = historico.refazer(formas);
        if (proximo != null) {
            formas.clear();
            formas.addAll(proximo);
            formasSelecionadas.clear();
            redesenhar();
        }
    }

    public boolean podeDesfazer() { return historico.podeDesfazer(); }
    public boolean podeRefazer()  { return historico.podeRefazer(); }

    // -------------------------------------------------------------------------
    // Snap to Grid
    // -------------------------------------------------------------------------

    public double aplicarSnap(double valor) {
        if (!gridSnappingAtivo) return valor;
        return Math.round(valor / tamanhoGrid) * tamanhoGrid;
    }

    public boolean isGridSnappingAtivo() { return gridSnappingAtivo; }
    public void setGridSnappingAtivo(boolean ativo) { this.gridSnappingAtivo = ativo; }
    public double getTamanhoGrid() { return tamanhoGrid; }
    public void setTamanhoGrid(double tamanho) { this.tamanhoGrid = tamanho; }

    // -------------------------------------------------------------------------
    // Estilos ativos
    // -------------------------------------------------------------------------

    public String getCorPreenchimentoAtual() { return corPreenchimentoAtual; }
    public void setCorPreenchimentoAtual(String hex) { this.corPreenchimentoAtual = hex; }

    public String getCorBordaAtual() { return corBordaAtual; }
    public void setCorBordaAtual(String hex) { this.corBordaAtual = hex; }

    public double getEspessuraBordaAtual() { return espessuraBordaAtual; }
    public void setEspessuraBordaAtual(double e) { this.espessuraBordaAtual = e; }

    // -------------------------------------------------------------------------
    // Canvas
    // -------------------------------------------------------------------------

    public Canvas getCanvas() { return canvas; }

    // -------------------------------------------------------------------------
    // Renderização
    // -------------------------------------------------------------------------

    /**
     * Redesenha todas as formas no canvas.
     */
    public void redesenhar() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Grade opcional
        if (gridSnappingAtivo) {
            desenharGrade(gc);
        }

        for (FormaGeometrica forma : formas) {
            boolean selecionada = estaSelecionada(forma);
            desenharForma(gc, forma, selecionada);
        }
    }

    private void desenharGrade(GraphicsContext gc) {
        gc.setStroke(Color.web("#DDDDDD"));
        gc.setLineWidth(0.5);
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        for (double x = 0; x <= w; x += tamanhoGrid) {
            gc.strokeLine(x, 0, x, h);
        }
        for (double y = 0; y <= h; y += tamanhoGrid) {
            gc.strokeLine(0, y, w, y);
        }
    }

    private void desenharForma(GraphicsContext gc, FormaGeometrica forma, boolean selecionada) {
        List<org.example.Models.Ponto> pontos = forma.getPontos();

        gc.setFill(Color.web(forma.getCorPreenchimentoHex()));
        gc.setStroke(selecionada ? Color.DODGERBLUE : Color.web(forma.getCorBordaHex()));
        gc.setLineWidth(selecionada ? forma.getEspessuraBorda() + 1.5 : forma.getEspessuraBorda());

        if (forma instanceof org.example.Models.Circulo circulo) {
            double cx = circulo.getCentro().getX();
            double cy = circulo.getCentro().getY();
            double r  = circulo.getRaio();
            gc.fillOval(cx - r, cy - r, r * 2, r * 2);
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
        } else if (pontos.size() >= 2) {
            double[] xs = pontos.stream().mapToDouble(org.example.Models.Ponto::getX).toArray();
            double[] ys = pontos.stream().mapToDouble(org.example.Models.Ponto::getY).toArray();
            gc.fillPolygon(xs, ys, pontos.size());
            gc.strokePolygon(xs, ys, pontos.size());
        }

        // Destaca vértices quando selecionada
        if (selecionada) {
            gc.setFill(Color.WHITE);
            gc.setStroke(Color.DODGERBLUE);
            gc.setLineWidth(1);
            for (org.example.Models.Ponto p : pontos) {
                gc.fillRect(p.getX() - 4, p.getY() - 4, 8, 8);
                gc.strokeRect(p.getX() - 4, p.getY() - 4, 8, 8);
            }
        }
    }
}