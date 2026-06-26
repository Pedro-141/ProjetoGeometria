package org.example.Views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.example.Models.FormaGeometrica;

import java.util.List;

public class PainelStatusView extends HBox {

    private final Label lblCoordenadas  = new Label("X: 0   Y: 0");
    private final Label lblTotalFormas  = new Label("Formas: 0");
    private final Label lblArea         = new Label("Área: —");
    private final Label lblPerimetro    = new Label("Perímetro: —");
    private final Label lblFerramenta   = new Label("Ferramenta: Desenhar");

    public PainelStatusView() {
        super(16);
        configurarLayout();
    }

    // -------------------------------------------------------------------------
    // Layout
    // -------------------------------------------------------------------------

    private void configurarLayout() {
        setPadding(new Insets(4, 12, 4, 12));
        setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #444444; "
                + "-fx-border-width: 1 0 0 0;");

        String estiloLabel = "-fx-text-fill: #cccccc; -fx-font-size: 12px;";
        lblCoordenadas.setStyle(estiloLabel);
        lblTotalFormas.setStyle(estiloLabel);
        lblArea.setStyle(estiloLabel);
        lblPerimetro.setStyle(estiloLabel);
        lblFerramenta.setStyle(estiloLabel + " -fx-font-weight: bold;");

        // Separadores visuais
        Label sep1 = separador();
        Label sep2 = separador();
        Label sep3 = separador();
        Label sep4 = separador();

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        getChildren().addAll(
                lblFerramenta, sep1,
                lblCoordenadas, sep2,
                lblTotalFormas, sep3,
                lblArea, sep4,
                lblPerimetro,
                espacador
        );
    }

    private Label separador() {
        Label sep = new Label("|");
        sep.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");
        return sep;
    }

    // -------------------------------------------------------------------------
    // Métodos de atualização (chamados pelo ContainerApp)
    // -------------------------------------------------------------------------

    public void atualizarCoordenadas(double x, double y) {
        lblCoordenadas.setText(String.format("X: %.0f   Y: %.0f", x, y));
    }

    public void atualizarTotalFormas(int total) {
        lblTotalFormas.setText("Formas: " + total);
    }

    public void atualizarMetricas(List<FormaGeometrica> selecionadas) {
        if (selecionadas == null || selecionadas.isEmpty()) {
            lblArea.setText("Área: —");
            lblPerimetro.setText("Perímetro: —");
            return;
        }

        double areaTotal     = 0;
        double perimetroTotal = 0;
        for (FormaGeometrica f : selecionadas) {
            areaTotal     += f.calcularArea();
            perimetroTotal += f.calcularPerimetro();
        }

        lblArea.setText(String.format("Área: %.2f px²", areaTotal));
        lblPerimetro.setText(String.format("Perímetro: %.2f px", perimetroTotal));
    }


    public void atualizarFerramenta(String nomeFerramenta) {
        lblFerramenta.setText("Ferramenta: " + nomeFerramenta);
    }
}