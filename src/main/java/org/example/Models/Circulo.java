package org.example.Models;

public class Circulo extends FormaBase {

    private double raio;

    public Circulo(Ponto centro, double raio) {
        super();
        if (raio <= 0) {
            throw new IllegalArgumentException("O raio do círculo deve ser positivo (r > 0).");
        }
        this.raio = raio;
        adicionarPonto(centro);
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public double getRaio() {
        return raio;
    }

    public void setRaio(double raio) {
        if (raio <= 0) {
            throw new IllegalArgumentException("O raio do círculo deve ser positivo (r > 0).");
        }
        this.raio = raio;
    }

    public Ponto getCentro() {
        return pontos.get(0);
    }

    // -------------------------------------------------------------------------
    // Área e Perímetro
    // -------------------------------------------------------------------------

    @Override
    public double calcularArea() {
        return Math.PI * raio * raio;
    }

    @Override
    public double calcularPerimetro() {
        return 2 * Math.PI * raio;
    }

    // -------------------------------------------------------------------------
    // Centroide (coincide com o centro)
    // -------------------------------------------------------------------------

    @Override
    public Ponto getCentroide() {
        return getCentro();
    }

    // -------------------------------------------------------------------------
    // Hit-test
    // -------------------------------------------------------------------------

    @Override
    public boolean contemPonto(double px, double py) {
        Ponto c = getCentro();
        double dx = px - c.getX();
        double dy = py - c.getY();
        return (dx * dx + dy * dy) <= (raio * raio);
    }

    // -------------------------------------------------------------------------
    // Transformações — escala também ajusta o raio
    // -------------------------------------------------------------------------

    @Override
    public void escalar(double fator) {
        // Move o centro normalmente (FormaBase) e ajusta o raio
        super.escalar(fator);
        this.raio *= fator;
    }

    // -------------------------------------------------------------------------
    // Prototype
    // -------------------------------------------------------------------------

    @Override
    public FormaGeometrica clonar() {
        Circulo copia = new Circulo(getCentro().clone(), this.raio);
        copiarEstiloPara(copia);
        return copia;
    }

    @Override
    public String toString() {
        return String.format("Circulo[centro=%s, raio=%.2f, area=%.2f]",
                getCentro(), raio, calcularArea());
    }
}