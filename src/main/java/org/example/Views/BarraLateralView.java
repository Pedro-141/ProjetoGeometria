package org.example.Views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.Controller.*;

public class BarraLateralView extends ScrollPane {

    // Referência ao container para acionar ações
    private final ContainerApp app;

    // Controles de estilo
    private final ColorPicker colorPreenchimento = new ColorPicker(Color.web("#AAAAAA"));
    private final ColorPicker colorBorda         = new ColorPicker(Color.BLACK);
    private final Slider      sliderEspessura    = new Slider(0.5, 10, 1);
    private final CheckBox    chkGrid            = new CheckBox("Grade Magnética (Snapping)");

    // Botões de ferramentas
    private final ToggleGroup grupoFerramentas = new ToggleGroup();

    public BarraLateralView(ContainerApp app) {
        this.app = app;
        configurarLayout();
    }

    // -------------------------------------------------------------------------
    // Layout principal
    // -------------------------------------------------------------------------

    private void configurarLayout() {
        setFitToWidth(true);
        setPrefWidth(220);
        setMinWidth(200);
        setStyle("-fx-background-color: #3c3c3c; -fx-border-color: #555;");

        VBox conteudo = new VBox(8);
        conteudo.setPadding(new Insets(8));
        conteudo.setStyle("-fx-background-color: #3c3c3c;");

        conteudo.getChildren().addAll(
                secaoEstilos(),
                separadorVisual(),
                secaoFerramentas(),
                separadorVisual(),
                secaoFormas(),
                separadorVisual(),
                secaoCamadasGrupo(),
                separadorVisual(),
                secaoSistema()
        );

        setContent(conteudo);
    }

    // -------------------------------------------------------------------------
    // Seção: Estilos
    // -------------------------------------------------------------------------

    private VBox secaoEstilos() {
        VBox box = criarSecao("— ESTILOS —");

        Label lblPreen = labelEstilo("Cor Preenchimento:");
        colorPreenchimento.setMaxWidth(Double.MAX_VALUE);
        colorPreenchimento.setOnAction(e -> app.setCorPreenchimento(toHex(colorPreenchimento.getValue())));

        Label lblBorda = labelEstilo("Cor Borda:");
        colorBorda.setMaxWidth(Double.MAX_VALUE);
        colorBorda.setOnAction(e -> app.setCorBorda(toHex(colorBorda.getValue())));

        Label lblEsp = labelEstilo("Espessura da Borda:");
        sliderEspessura.setShowTickLabels(true);
        sliderEspessura.setMajorTickUnit(3);
        sliderEspessura.setStyle("-fx-control-inner-background: #555;");
        sliderEspessura.valueProperty().addListener(
                (obs, antigo, novo) -> app.setEspessuraBorda(novo.doubleValue()));

        chkGrid.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");
        chkGrid.setOnAction(e -> app.setGridSnapping(chkGrid.isSelected()));

        box.getChildren().addAll(lblPreen, colorPreenchimento,
                lblBorda, colorBorda,
                lblEsp, sliderEspessura,
                chkGrid);
        return box;
    }

    // -------------------------------------------------------------------------
    // Seção: Ferramentas
    // -------------------------------------------------------------------------

    private VBox secaoFerramentas() {
        VBox box = criarSecao("— FERRAMENTAS —");

        ToggleButton btnLinhaLivre  = botaoToggle("✏ Linha Livre");
        ToggleButton btnSelecionar  = botaoToggle("⬚ Selecionar");
        ToggleButton btnEscalar     = botaoToggle("⤢ Escalar");
        ToggleButton btnRotacionar  = botaoToggle("↻ Rotacionar");
        ToggleButton btnCisalhar    = botaoToggle("⌇ Cisalhar");
        ToggleButton btnExcluir     = botaoToggle("✕ Excluir");
        ToggleButton btnEditarVert  = botaoToggle("◈ Editar Vértice");
        Button       btnExcluirSel  = botaoAcao("Excluir Selecionados");

        btnLinhaLivre.setToggleGroup(grupoFerramentas);
        btnSelecionar.setToggleGroup(grupoFerramentas);
        btnEscalar.setToggleGroup(grupoFerramentas);
        btnRotacionar.setToggleGroup(grupoFerramentas);
        btnCisalhar.setToggleGroup(grupoFerramentas);
        btnExcluir.setToggleGroup(grupoFerramentas);
        btnEditarVert.setToggleGroup(grupoFerramentas);

        btnLinhaLivre.setSelected(true); // padrão inicial

        btnLinhaLivre.setOnAction(e -> {
            app.setFerramenta(new FerramentaDesenhar(), "Linha Livre");
        });
        btnSelecionar.setOnAction(e -> {
            app.setFerramenta(new FerramentaSelecionar(), "Selecionar");
        });
        btnEscalar.setOnAction(e -> {
            app.setFerramenta(new FerramentaTransformar(ModoFerramenta.ESCALAR), "Escalar");
        });
        btnRotacionar.setOnAction(e -> {
            app.setFerramenta(new FerramentaTransformar(ModoFerramenta.ROTACIONAR), "Rotacionar");
        });
        btnCisalhar.setOnAction(e -> {
            app.setFerramenta(new FerramentaTransformar(ModoFerramenta.CISALHAR), "Cisalhar");
        });
        btnExcluir.setOnAction(e -> {
            app.setFerramenta(new FerramentaSelecionar(), "Selecionar (Excluir)");
        });
        btnEditarVert.setOnAction(e -> {
            app.setFerramenta(new FerramentaEditarVertice(), "Editar Vértice");
        });
        btnExcluirSel.setOnAction(e -> app.excluirSelecionados());

        box.getChildren().addAll(btnLinhaLivre, btnSelecionar, btnEscalar,
                btnRotacionar, btnCisalhar, btnExcluir,
                btnEditarVert, btnExcluirSel);
        return box;
    }

    // -------------------------------------------------------------------------
    // Seção: Formas predefinidas
    // -------------------------------------------------------------------------

    private VBox secaoFormas() {
        VBox box = criarSecao("— FORMAS —");

        Button btnQuadrado  = botaoAcao("Inserir Quadrado");
        Button btnCirculo   = botaoAcao("Inserir Círculo");
        Button btnHexagono  = botaoAcao("○ Inserir Hexágono");

        btnQuadrado.setOnAction(e ->
                app.setFerramenta(
                        new FerramentaInserirForma(FerramentaInserirForma.TipoForma.QUADRADO),
                        "Inserir Quadrado"));

        btnCirculo.setOnAction(e ->
                app.setFerramenta(
                        new FerramentaInserirForma(FerramentaInserirForma.TipoForma.CIRCULO),
                        "Inserir Círculo"));

        btnHexagono.setOnAction(e ->
                app.setFerramenta(
                        new FerramentaInserirForma(FerramentaInserirForma.TipoForma.HEXAGONO),
                        "Inserir Hexágono"));

        box.getChildren().addAll(btnQuadrado, btnCirculo, btnHexagono);
        return box;
    }

    // -------------------------------------------------------------------------
    // Seção: Camadas e Grupo
    // -------------------------------------------------------------------------

    private VBox secaoCamadasGrupo() {
        VBox box = criarSecao("— CAMADAS/GRUPO —");

        Button btnFrente    = botaoAcao("▲ Trazer p/ Frente");
        Button btnTras      = botaoAcao("▼ Enviar p/ Trás");
        Button btnAgrupar   = botaoAcao("Agrupar");
        Button btnDesagrupar = botaoAcao("⚭ Desagrupar");
        Button btnDuplicar  = botaoAcao("❐ Duplicar Selecionados");

        btnFrente.setOnAction(e    -> app.trazerParaFrente());
        btnTras.setOnAction(e      -> app.enviarParaTras());
        btnAgrupar.setOnAction(e   -> app.agrupar());
        btnDesagrupar.setOnAction(e -> app.desagrupar());
        btnDuplicar.setOnAction(e  -> app.duplicar());

        box.getChildren().addAll(btnFrente, btnTras, btnAgrupar, btnDesagrupar, btnDuplicar);
        return box;
    }

    // -------------------------------------------------------------------------
    // Seção: Sistema (Undo/Redo, Salvar, Carregar)
    // -------------------------------------------------------------------------

    private VBox secaoSistema() {
        VBox box = criarSecao("— SISTEMA —");

        Button btnDesfazer = botaoAcao("↩ Desfazer");
        Button btnRefazer  = botaoAcao("↪ Refazer");
        Button btnSalvar   = botaoAcao("💾 Salvar");
        Button btnCarregar = botaoAcao("📂 Carregar");

        btnDesfazer.setOnAction(e -> app.desfazer());
        btnRefazer.setOnAction(e  -> app.refazer());
        btnSalvar.setOnAction(e   -> app.salvar());
        btnCarregar.setOnAction(e -> app.carregar());

        box.getChildren().addAll(btnDesfazer, btnRefazer, btnSalvar, btnCarregar);
        return box;
    }

    // -------------------------------------------------------------------------
    // Fábrica de componentes estilizados
    // -------------------------------------------------------------------------

    private VBox criarSecao(String titulo) {
        VBox box = new VBox(4);
        Label lbl = new Label(titulo);
        lbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px; -fx-font-weight: bold;");
        box.getChildren().add(lbl);
        return box;
    }

    private Label labelEstilo(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");
        return lbl;
    }

    private ToggleButton botaoToggle(String texto) {
        ToggleButton btn = new ToggleButton(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("""
                -fx-background-color: #4a4a4a;
                -fx-text-fill: #dddddd;
                -fx-font-size: 11px;
                -fx-cursor: hand;
                """);
        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                btn.setStyle("""
                        -fx-background-color: #5b8dd9;
                        -fx-text-fill: white;
                        -fx-font-size: 11px;
                        -fx-cursor: hand;
                        """);
            } else {
                btn.setStyle("""
                        -fx-background-color: #4a4a4a;
                        -fx-text-fill: #dddddd;
                        -fx-font-size: 11px;
                        -fx-cursor: hand;
                        """);
            }
        });
        return btn;
    }

    private Button botaoAcao(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("""
                -fx-background-color: #4a4a4a;
                -fx-text-fill: #dddddd;
                -fx-font-size: 11px;
                -fx-cursor: hand;
                """);
        btn.setOnMouseEntered(e -> btn.setStyle("""
                -fx-background-color: #5a5a5a;
                -fx-text-fill: white;
                -fx-font-size: 11px;
                -fx-cursor: hand;
                """));
        btn.setOnMouseExited(e -> btn.setStyle("""
                -fx-background-color: #4a4a4a;
                -fx-text-fill: #dddddd;
                -fx-font-size: 11px;
                -fx-cursor: hand;
                """));
        return btn;
    }

    private Separator separadorVisual() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #555555;");
        return sep;
    }

    // -------------------------------------------------------------------------
    // Utilitário: Color JavaFX → string hex
    // -------------------------------------------------------------------------

    private String toHex(Color cor) {
        return String.format("#%02X%02X%02X",
                (int) (cor.getRed()   * 255),
                (int) (cor.getGreen() * 255),
                (int) (cor.getBlue()  * 255));
    }
}