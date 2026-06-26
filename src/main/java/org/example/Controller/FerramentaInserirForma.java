package org.example.Controller;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.example.Models.*;

public class FerramentaInserirForma implements Ferramenta {

    public enum TipoForma { CIRCULO, QUADRADO, HEXAGONO }

    private final TipoForma tipoForma;
    private Ponto centroInsercao;
    private double raioAtual;
    private boolean arrastando = false;

    public FerramentaInserirForma(TipoForma tipoForma) {
        this.tipoForma = tipoForma;
    }

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        if (evento.getButton() != MouseButton.PRIMARY) return;
        double x = contexto.aplicarSnap(evento.getX());
        double y = contexto.aplicarSnap(evento.getY());
        centroInsercao = new Ponto(x, y);
        raioAtual = 0;
        arrastando = true;
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        if (!arrastando || centroInsercao == null) return;

        double x = contexto.aplicarSnap(evento.getX());
        double y = contexto.aplicarSnap(evento.getY());

        double dx = x - centroInsercao.getX();
        double dy = y - centroInsercao.getY();
        raioAtual = Math.sqrt(dx * dx + dy * dy);

        // Preview visual
        contexto.redesenhar();
        desenharPreview(contexto);
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        if (!arrastando || centroInsercao == null) return;
        arrastando = false;

        if (raioAtual < 5) {
            contexto.redesenhar();
            return; // forma muito pequena — ignora
        }

        FormaGeometrica novaForma = criarForma();
        if (novaForma != null) {
            contexto.adicionarForma(novaForma);
        }

        centroInsercao = null;
        raioAtual = 0;
    }

    @Override
    public void aoClicar(MouseEvent evento, EditorContext contexto) {
        // Não usado nesta ferramenta
    }

    // -------------------------------------------------------------------------
    // Auxiliares
    // -------------------------------------------------------------------------

    private FormaGeometrica criarForma() {
        if (centroInsercao == null || raioAtual <= 0) return null;
        return switch (tipoForma) {
            case CIRCULO   -> new Circulo(centroInsercao.clone(), raioAtual);
            case QUADRADO  -> new Quadrado(centroInsercao.clone(), raioAtual);
            case HEXAGONO  -> new Hexagono(centroInsercao.clone(), raioAtual);
        };
    }

    private void desenharPreview(EditorContext contexto) {
        if (centroInsercao == null || raioAtual <= 0) return;
        GraphicsContext gc = contexto.getCanvas().getGraphicsContext2D();

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1.5);
        gc.setLineDashes(6);
        gc.setFill(Color.rgb(150, 150, 150, 0.2));

        double cx = centroInsercao.getX();
        double cy = centroInsercao.getY();
        double r  = raioAtual;

        switch (tipoForma) {
            case CIRCULO -> {
                gc.fillOval(cx - r, cy - r, r * 2, r * 2);
                gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
            }
            case QUADRADO -> {
                gc.fillRect(cx - r, cy - r, r * 2, r * 2);
                gc.strokeRect(cx - r, cy - r, r * 2, r * 2);
            }
            case HEXAGONO -> {
                double[] xs = new double[6];
                double[] ys = new double[6];
                for (int i = 0; i < 6; i++) {
                    double ang = Math.toRadians(60 * i - 30);
                    xs[i] = cx + r * Math.cos(ang);
                    ys[i] = cy + r * Math.sin(ang);
                }
                gc.fillPolygon(xs, ys, 6);
                gc.strokePolygon(xs, ys, 6);
            }
        }

        gc.setLineDashes(0);
    }
}