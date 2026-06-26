package org.example.Models;

import java.util.ArrayList;
import java.util.List;

public abstract class FormaBase implements FormaGeometrica {

    protected List<Ponto> pontos;
    protected String corPreenchimentoHex;
    protected String corBordaHex;
    protected double espessuraBorda;
    protected String groupId;

    public FormaBase() {
        this.pontos = new ArrayList<>();
        this.corPreenchimentoHex = "#AAAAAA";
        this.corBordaHex = "#000000";
        this.espessuraBorda = 1.0;
        this.groupId = null;
    }

    // -------------------------------------------------------------------------
    // Gerenciamento de pontos
    // -------------------------------------------------------------------------

    @Override
    public void adicionarPonto(Ponto p) {
        pontos.add(p);
    }

    @Override
    public List<Ponto> getPontos() {
        return pontos;
    }

    @Override
    public int getQuantidadePontos() {
        return pontos.size();
    }

    // -------------------------------------------------------------------------
    // Getters e Setters de estilo
    // -------------------------------------------------------------------------

    @Override
    public String getCorPreenchimentoHex() {
        return corPreenchimentoHex;
    }

    @Override
    public void setCorPreenchimentoHex(String hex) {
        this.corPreenchimentoHex = hex;
    }

    @Override
    public String getCorBordaHex() {
        return corBordaHex;
    }

    @Override
    public void setCorBordaHex(String hex) {
        this.corBordaHex = hex;
    }

    @Override
    public double getEspessuraBorda() {
        return espessuraBorda;
    }

    @Override
    public void setEspessuraBorda(double espessura) {
        this.espessuraBorda = espessura;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    // -------------------------------------------------------------------------
    // Cálculo do centroide (média aritmética dos pontos)
    // -------------------------------------------------------------------------

    @Override
    public Ponto getCentroide() {
        if (pontos.isEmpty()) return new Ponto(0, 0);
        double somaX = 0, somaY = 0;
        for (Ponto p : pontos) {
            somaX += p.getX();
            somaY += p.getY();
        }
        return new Ponto(somaX / pontos.size(), somaY / pontos.size());
    }

    // -------------------------------------------------------------------------
    // Transformações geométricas
    // -------------------------------------------------------------------------

    @Override
    public void transladar(double dx, double dy) {
        for (Ponto p : pontos) {
            p.mover(dx, dy);
        }
    }

    @Override
    public void escalar(double fator) {
        Ponto centro = getCentroide();
        for (Ponto p : pontos) {
            double novoX = centro.getX() + (p.getX() - centro.getX()) * fator;
            double novoY = centro.getY() + (p.getY() - centro.getY()) * fator;
            p.setX(novoX);
            p.setY(novoY);
        }
    }

    /**
     * Rotação em torno do centroide (ângulo em radianos).
     */
    @Override
    public void rotacionar(double anguloRadianos) {
        Ponto centro = getCentroide();
        double cos = Math.cos(anguloRadianos);
        double sin = Math.sin(anguloRadianos);
        for (Ponto p : pontos) {
            double dx = p.getX() - centro.getX();
            double dy = p.getY() - centro.getY();
            p.setX(centro.getX() + dx * cos - dy * sin);
            p.setY(centro.getY() + dx * sin + dy * cos);
        }
    }

    @Override
    public void cisalhar(double shx, double shy) {
        for (Ponto p : pontos) {
            double novoX = p.getX() + shx * p.getY();
            double novoY = p.getY() + shy * p.getX();
            p.setX(novoX);
            p.setY(novoY);
        }
    }

    // -------------------------------------------------------------------------
    // Utilitário: copia atributos de estilo para outra forma (usado em clonar)
    // -------------------------------------------------------------------------

    protected void copiarEstiloPara(FormaBase destino) {
        destino.corPreenchimentoHex = this.corPreenchimentoHex;
        destino.corBordaHex = this.corBordaHex;
        destino.espessuraBorda = this.espessuraBorda;
        destino.groupId = this.groupId;
    }
}