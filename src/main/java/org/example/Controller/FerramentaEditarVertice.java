package org.example.Controller;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

import java.util.List;

public class FerramentaEditarVertice implements Ferramenta {

    private static final double RAIO_SELECAO = 8.0; // tolerância em pixels

    private Ponto verticeSelecionado = null;
    private FormaGeometrica formaEmEdicao = null;
    private boolean arrastando = false;

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        if (evento.getButton() != MouseButton.PRIMARY) return;

        double x = evento.getX();
        double y = evento.getY();

        // Procura um vértice próximo ao clique nas formas selecionadas
        for (FormaGeometrica forma : contexto.getFormasSelecionadas()) {
            Ponto encontrado = encontrarVerticeProximo(forma.getPontos(), x, y);
            if (encontrado != null) {
                verticeSelecionado = encontrado;
                formaEmEdicao = forma;
                arrastando = true;

                // Registra estado antes de editar (Memento)
                contexto.registrarEstado();
                return;
            }
        }

        // Nenhum vértice encontrado: tenta selecionar forma
        FormaGeometrica alvo = contexto.encontrarFormaNoPonto(x, y);
        contexto.desselecionarTudo();
        if (alvo != null) {
            contexto.selecionarForma(alvo);
        }

        verticeSelecionado = null;
        formaEmEdicao = null;
        arrastando = false;
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        if (!arrastando || verticeSelecionado == null) return;

        double x = contexto.aplicarSnap(evento.getX());
        double y = contexto.aplicarSnap(evento.getY());

        verticeSelecionado.setX(x);
        verticeSelecionado.setY(y);

        contexto.redesenhar();
        destacarVertices(contexto);
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        arrastando = false;
        verticeSelecionado = null;
        formaEmEdicao = null;
        contexto.redesenhar();
    }

    @Override
    public void aoClicar(MouseEvent evento, EditorContext contexto) {
        // Clique já tratado em aoPressionarMouse
    }

    // -------------------------------------------------------------------------
    // Auxiliares
    // -------------------------------------------------------------------------

    private Ponto encontrarVerticeProximo(List<Ponto> pontos, double px, double py) {
        for (Ponto p : pontos) {
            double dx = p.getX() - px;
            double dy = p.getY() - py;
            if (Math.sqrt(dx * dx + dy * dy) <= RAIO_SELECAO) {
                return p;
            }
        }
        return null;
    }

    private void destacarVertices(EditorContext contexto) {
        GraphicsContext gc = contexto.getCanvas().getGraphicsContext2D();

        for (FormaGeometrica forma : contexto.getFormasSelecionadas()) {
            for (Ponto p : forma.getPontos()) {
                boolean esteVertice = (p == verticeSelecionado);
                gc.setFill(esteVertice ? Color.ORANGE : Color.WHITE);
                gc.setStroke(Color.DODGERBLUE);
                gc.setLineWidth(1.5);
                gc.fillOval(p.getX() - 5, p.getY() - 5, 10, 10);
                gc.strokeOval(p.getX() - 5, p.getY() - 5, 10, 10);
            }
        }
    }
}