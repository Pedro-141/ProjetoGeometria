package org.example.Models;

public class Hexagono extends FormaBase {

    private double raio;

    public Hexagono(Ponto centro, double raio) {
        super();
        if (raio <= 0) {
            throw new IllegalArgumentException("O raio do hexágono deve ser positivo.");
        }
        this.raio = raio;
        calcularVertices(centro, raio);
    }

    // -------------------------------------------------------------------------
    // Cálculo automático dos 6 vértices (hexágono "flat-top", 0° no topo)
    // -------------------------------------------------------------------------

    private void calcularVertices(Ponto centro, double r) {
        pontos.clear();
        double cx = centro.getX();
        double cy = centro.getY();
        for (int i = 0; i < 6; i++) {
            double angulo = Math.toRadians(60 * i - 30); // -30° = vértice no topo
            pontos.add(new Ponto(
                    cx + r * Math.cos(angulo),
                    cy + r * Math.sin(angulo)
            ));
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public double getRaio() {
        return raio;
    }

    // -------------------------------------------------------------------------
    // Área e Perímetro do hexágono regular
    // -------------------------------------------------------------------------

    @Override
    public double calcularArea() {
        // A = (3 * sqrt(3) / 2) * r²
        return (3.0 * Math.sqrt(3.0) / 2.0) * raio * raio;
    }

    @Override
    public double calcularPerimetro() {
        // 6 lados de comprimento = r (para hexágono regular)
        return 6 * raio;
    }

    // -------------------------------------------------------------------------
    // Hit-test — usa Ray Casting herdado de Poligono via FormaBase nos pontos
    // -------------------------------------------------------------------------

    @Override
    public boolean contemPonto(double px, double py) {
        // Aproximação rápida via distância ao centro
        Ponto c = getCentroide();
        double dist = Math.sqrt(Math.pow(px - c.getX(), 2) + Math.pow(py - c.getY(), 2));
        return dist <= raio;
    }

    // -------------------------------------------------------------------------
    // Escala também atualiza o raio interno
    // -------------------------------------------------------------------------

    @Override
    public void escalar(double fator) {
        super.escalar(fator);
        this.raio *= fator;
    }

    // -------------------------------------------------------------------------
    // Prototype
    // -------------------------------------------------------------------------

    @Override
    public FormaGeometrica clonar() {
        Hexagono copia = new Hexagono(getCentroide().clone(), this.raio);
        copiarEstiloPara(copia);
        // Preserva vértices transformados
        copia.pontos.clear();
        for (Ponto p : this.pontos) {
            copia.pontos.add(p.clone());
        }
        return copia;
    }

    @Override
    public String toString() {
        return String.format("Hexagono[raio=%.2f, area=%.2f, perimetro=%.2f]",
                raio, calcularArea(), calcularPerimetro());
    }
}