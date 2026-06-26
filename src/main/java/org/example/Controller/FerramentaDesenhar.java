package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.Poligono;
import org.example.Models.Ponto;

import java.util.ArrayList;
import java.util.List;

public class FerramentaDesenhar implements Ferramenta {

    private final List<Ponto> pontosTemporarios = new ArrayList<>();
    private boolean desenhando = false;

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        // Não usado nesta ferramenta
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        // Não usado nesta ferramenta (sem preview dinâmico de vértice)
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        // Não usado nesta ferramenta
    }

    @Override
    public void aoClicar(MouseEvent evento, EditorContext contexto) {
        double x = contexto.aplicarSnap(evento.getX());
        double y = contexto.aplicarSnap(evento.getY());

        // Clique direito ou duplo clique finaliza o polígono
        if (evento.getButton() == MouseButton.SECONDARY || evento.getClickCount() == 2) {
            finalizarPoligono(contexto);
            return;
        }

        // Clique esquerdo: adiciona vértice
        if (evento.getButton() == MouseButton.PRIMARY) {
            pontosTemporarios.add(new Ponto(x, y));
            desenhando = true;

            // Preview: redesenha com os pontos temporários
            contexto.redesenhar();
            desenharPreview(contexto);
        }
    }

    private void finalizarPoligono(EditorContext contexto) {
        if (pontosTemporarios.size() < 3) {
            pontosTemporarios.clear();
            desenhando = false;
            contexto.redesenhar();
            return;
        }

        Poligono poligono = new Poligono();
        for (Ponto p : pontosTemporarios) {
            poligono.adicionarPonto(p);
        }

        contexto.adicionarForma(poligono);

        pontosTemporarios.clear();
        desenhando = false;
    }

    private void desenharPreview(EditorContext contexto) {
        if (pontosTemporarios.isEmpty()) return;

        javafx.scene.canvas.GraphicsContext gc =
                contexto.getCanvas().getGraphicsContext2D();

        gc.setStroke(javafx.scene.paint.Color.GRAY);
        gc.setLineWidth(1);
        gc.setLineDashes(5);

        // Linha entre os pontos já clicados
        for (int i = 0; i < pontosTemporarios.size() - 1; i++) {
            Ponto a = pontosTemporarios.get(i);
            Ponto b = pontosTemporarios.get(i + 1);
            gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
        }

        // Pontos clicados como pequenos círculos
        gc.setFill(javafx.scene.paint.Color.GRAY);
        for (Ponto p : pontosTemporarios) {
            gc.fillOval(p.getX() - 3, p.getY() - 3, 6, 6);
        }

        gc.setLineDashes(0); // restaura linha sólida
    }
}