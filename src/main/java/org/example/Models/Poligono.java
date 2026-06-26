package org.example.Models;

import java.util.List;

public class Poligono extends FormaBase {

    public Poligono() {
        super();
    }

    // -------------------------------------------------------------------------
    // Validação
    // -------------------------------------------------------------------------

    public boolean isValido() {
        return pontos.size() >= 3;
    }

    // -------------------------------------------------------------------------
    // Área — Fórmula de Gauss (Shoelace)
    // -------------------------------------------------------------------------

    @Override
    public double calcularArea() {
        int n = pontos.size();
        if (n < 3) return 0;

        double soma = 0;
        for (int i = 0; i < n - 1; i++) {
            soma += pontos.get(i).getX() * pontos.get(i + 1).getY();
            soma -= pontos.get(i + 1).getX() * pontos.get(i).getY();
        }
        // Fechamento: último ponto → primeiro ponto
        soma += pontos.get(n - 1).getX() * pontos.get(0).getY();
        soma -= pontos.get(0).getX() * pontos.get(n - 1).getY();

        return Math.abs(soma) / 2.0;
    }

    // -------------------------------------------------------------------------
    // Perímetro — soma das distâncias euclidianas
    // -------------------------------------------------------------------------

    @Override
    public double calcularPerimetro() {
        int n = pontos.size();
        if (n < 2) return 0;

        double perimetro = 0;
        for (int i = 0; i < n - 1; i++) {
            perimetro += pontos.get(i).distancia(pontos.get(i + 1));
        }
        // Fecha o polígono: último → primeiro
        perimetro += pontos.get(n - 1).distancia(pontos.get(0));
        return perimetro;
    }

    // -------------------------------------------------------------------------
    // Hit-test: verifica se um ponto está dentro do polígono (Ray Casting)
    // -------------------------------------------------------------------------

    @Override
    public boolean contemPonto(double px, double py) {
        int n = pontos.size();
        if (n < 3) return false;

        boolean dentro = false;
        int j = n - 1;
        for (int i = 0; i < n; i++) {
            double xi = pontos.get(i).getX(), yi = pontos.get(i).getY();
            double xj = pontos.get(j).getX(), yj = pontos.get(j).getY();

            boolean cruza = ((yi > py) != (yj > py))
                    && (px < (xj - xi) * (py - yi) / (yj - yi) + xi);
            if (cruza) dentro = !dentro;
            j = i;
        }
        return dentro;
    }

    // -------------------------------------------------------------------------
    // Prototype
    // -------------------------------------------------------------------------

    @Override
    public FormaGeometrica clonar() {
        Poligono copia = new Poligono();
        for (Ponto p : this.pontos) {
            copia.adicionarPonto(p.clone());
        }
        copiarEstiloPara(copia);
        return copia;
    }

    @Override
    public String toString() {
        return String.format("Poligono[pontos=%d, area=%.2f, perimetro=%.2f]",
                pontos.size(), calcularArea(), calcularPerimetro());
    }
}