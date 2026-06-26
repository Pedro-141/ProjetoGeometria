package org.example.Controller;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.example.Models.FormaGeometrica;

public class FerramentaSelecionar implements Ferramenta {

    private boolean arrastandoCaixa = false;
    private double selecaoStartX;
    private double selecaoStartY;
    private double selecaoAtualX;
    private double selecaoAtualY;

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        if (evento.getButton() != MouseButton.PRIMARY) return;

        selecaoStartX = evento.getX();
        selecaoStartY = evento.getY();
        selecaoAtualX = selecaoStartX;
        selecaoAtualY = selecaoStartY;

        FormaGeometrica alvo = contexto.encontrarFormaNoPonto(selecaoStartX, selecaoStartY);

        if (alvo == null) {
            // Iniciou em área vazia: começa caixa de seleção
            if (!evento.isShiftDown()) {
                contexto.desselecionarTudo();
            }
            arrastandoCaixa = true;
        }
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        if (!arrastandoCaixa) return;

        selecaoAtualX = evento.getX();
        selecaoAtualY = evento.getY();

        // Redesenha e mostra a caixa de seleção
        contexto.redesenhar();
        desenharCaixaSelecao(contexto);
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        if (!arrastandoCaixa) return;

        // Seleciona todas as formas dentro da caixa
        double x1 = Math.min(selecaoStartX, selecaoAtualX);
        double y1 = Math.min(selecaoStartY, selecaoAtualY);
        double x2 = Math.max(selecaoStartX, selecaoAtualX);
        double y2 = Math.max(selecaoStartY, selecaoAtualY);

        for (FormaGeometrica forma : contexto.getFormas()) {
            if (formaDentroRetangulo(forma, x1, y1, x2, y2)) {
                contexto.selecionarForma(forma);
            }
        }

        arrastandoCaixa = false;
        contexto.redesenhar();
    }

    @Override
    public void aoClicar(MouseEvent evento, EditorContext contexto) {
        if (evento.getButton() != MouseButton.PRIMARY) return;

        double x = evento.getX();
        double y = evento.getY();
        FormaGeometrica alvo = contexto.encontrarFormaNoPonto(x, y);

        if (alvo != null) {
            if (!evento.isShiftDown()) {
                // Seleção simples: deseleciona tudo antes
                contexto.desselecionarTudo();
            }
            contexto.selecionarForma(alvo);
        }
    }

    // -------------------------------------------------------------------------
    // Auxiliares
    // -------------------------------------------------------------------------

    private void desenharCaixaSelecao(EditorContext contexto) {
        GraphicsContext gc = contexto.getCanvas().getGraphicsContext2D();
        double x = Math.min(selecaoStartX, selecaoAtualX);
        double y = Math.min(selecaoStartY, selecaoAtualY);
        double w = Math.abs(selecaoAtualX - selecaoStartX);
        double h = Math.abs(selecaoAtualY - selecaoStartY);

        gc.setFill(Color.rgb(70, 130, 180, 0.15));
        gc.fillRect(x, y, w, h);
        gc.setStroke(Color.DODGERBLUE);
        gc.setLineWidth(1);
        gc.setLineDashes(4);
        gc.strokeRect(x, y, w, h);
        gc.setLineDashes(0);
    }

    private boolean formaDentroRetangulo(FormaGeometrica forma,
                                         double x1, double y1,
                                         double x2, double y2) {
        // Verifica se pelo menos um ponto da forma está dentro do retângulo
        for (org.example.Models.Ponto p : forma.getPontos()) {
            if (p.getX() >= x1 && p.getX() <= x2
                    && p.getY() >= y1 && p.getY() <= y2) {
                return true;
            }
        }
        return false;
    }
}