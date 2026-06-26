package org.example.Models;

public class Quadrado extends FormaBase {

    private double raio; // metade do lado (distância do centro ao vértice no eixo)

    public Quadrado(Ponto centro, double raio) {
        super();
        if (raio <= 0) {
            throw new IllegalArgumentException("O tamanho do quadrado deve ser positivo.");
        }
        this.raio = raio;
        calcularVertices(centro, raio);
    }

    // -------------------------------------------------------------------------
    // Cálculo automático dos 4 vértices
    // -------------------------------------------------------------------------

    private void calcularVertices(Ponto centro, double r) {
        pontos.clear();
        double cx = centro.getX();
        double cy = centro.getY();
        // Sentido horário a partir do canto superior-esquerdo
        pontos.add(new Ponto(cx - r, cy - r)); // topo-esquerdo
        pontos.add(new Ponto(cx + r, cy - r)); // topo-direito
        pontos.add(new Ponto(cx + r, cy + r)); // baixo-direito
        pontos.add(new Ponto(cx - r, cy + r)); // baixo-esquerdo
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public double getRaio() {
        return raio;
    }

    public double getLado() {
        return raio * 2;
    }

    // -------------------------------------------------------------------------
    // Área e Perímetro
    // -------------------------------------------------------------------------

    @Override
    public double calcularArea() {
        double lado = getLado();
        return lado * lado;
    }

    @Override
    public double calcularPerimetro() {
        return 4 * getLado();
    }

    // -------------------------------------------------------------------------
    // Hit-test (bounding box alinhada ao eixo)
    // -------------------------------------------------------------------------

    @Override
    public boolean contemPonto(double px, double py) {
        Ponto c = getCentroide();
        return Math.abs(px - c.getX()) <= raio && Math.abs(py - c.getY()) <= raio;
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
        Quadrado copia = new Quadrado(getCentroide().clone(), this.raio);
        copiarEstiloPara(copia);
        // Copia os vértices transformados (rotação, cisalhamento etc.)
        copia.pontos.clear();
        for (Ponto p : this.pontos) {
            copia.pontos.add(p.clone());
        }
        return copia;
    }

    @Override
    public String toString() {
        return String.format("Quadrado[lado=%.2f, area=%.2f]", getLado(), calcularArea());
    }
}