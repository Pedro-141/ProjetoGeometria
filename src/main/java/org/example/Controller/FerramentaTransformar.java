package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;

import java.util.List;

public class FerramentaTransformar implements Ferramenta {

    private ModoFerramenta modo;

    private double mouseInicioX;
    private double mouseInicioY;
    private boolean arrastando = false;

    public FerramentaTransformar(ModoFerramenta modo) {
        this.modo = modo;
    }

    public ModoFerramenta getModo() { return modo; }
    public void setModo(ModoFerramenta modo) { this.modo = modo; }

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        if (evento.getButton() != MouseButton.PRIMARY) return;
        mouseInicioX = evento.getX();
        mouseInicioY = evento.getY();
        arrastando = true;

        // Registra estado ANTES de começar a transformar (Memento)
        if (!contexto.getFormasSelecionadas().isEmpty()) {
            contexto.registrarEstado();
        }
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        if (!arrastando) return;

        double dx = evento.getX() - mouseInicioX;
        double dy = evento.getY() - mouseInicioY;

        List<FormaGeometrica> selecionadas = contexto.getFormasSelecionadas();
        if (selecionadas.isEmpty()) return;

        switch (modo) {
            case TRANSLADAR -> aplicarTranslacao(selecionadas, dx, dy);
            case ESCALAR    -> aplicarEscala(selecionadas, dx);
            case ROTACIONAR -> aplicarRotacao(selecionadas, dx);
            case CISALHAR   -> aplicarCisalhamento(selecionadas, dx, dy);
        }

        mouseInicioX = evento.getX();
        mouseInicioY = evento.getY();
        contexto.redesenhar();
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        arrastando = false;
    }

    @Override
    public void aoClicar(MouseEvent evento, EditorContext contexto) {
        // Clique não faz transformação, apenas garante deselecionar se área vazia
        double x = evento.getX();
        double y = evento.getY();
        FormaGeometrica alvo = contexto.encontrarFormaNoPonto(x, y);
        if (alvo == null) {
            contexto.desselecionarTudo();
        }
    }

    // -------------------------------------------------------------------------
    // Transformações
    // -------------------------------------------------------------------------

    private void aplicarTranslacao(List<FormaGeometrica> formas, double dx, double dy) {
        for (FormaGeometrica f : formas) {
            f.transladar(dx, dy);
        }
    }

    private void aplicarEscala(List<FormaGeometrica> formas, double dx) {
        // dx positivo = aumentar, negativo = diminuir
        double fator = 1.0 + dx * 0.01;
        if (fator <= 0) return;
        for (FormaGeometrica f : formas) {
            f.escalar(fator);
        }
    }

    private void aplicarRotacao(List<FormaGeometrica> formas, double dx) {
        // Cada pixel horizontal = 0.5 grau de rotação
        double angulo = Math.toRadians(dx * 0.5);
        for (FormaGeometrica f : formas) {
            f.rotacionar(angulo);
        }
    }

    private void aplicarCisalhamento(List<FormaGeometrica> formas, double dx, double dy) {
        double shx = dx * 0.002;
        double shy = dy * 0.002;
        for (FormaGeometrica f : formas) {
            f.cisalhar(shx, shy);
        }
    }
}