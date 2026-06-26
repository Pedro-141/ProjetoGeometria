package org.example.Views;

import com.google.gson.*;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Controller.*;
import org.example.Models.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

public class ContainerApp extends BorderPane {

    private final Canvas           canvas;
    private final EditorContext    contexto;
    private final BarraLateralView barraLateral;
    private final PainelStatusView painelStatus;

    private boolean alteracoesNaoSalvas = false;

    // -------------------------------------------------------------------------
    // Construtor
    // -------------------------------------------------------------------------

    public ContainerApp() {
        // Canvas redimensionável
        canvas = new Canvas(1050, 740);

        // Contexto central (State Pattern + Memento + renderização)
        contexto = new EditorContext(canvas);

        // Views auxiliares
        barraLateral = new BarraLateralView(this);
        painelStatus = new PainelStatusView();

        configurarLayout();
        configurarEventosMouse();
    }

    // -------------------------------------------------------------------------
    // Layout
    // -------------------------------------------------------------------------

    private void configurarLayout() {
        setStyle("-fx-background-color: #1e1e1e;");

        // Área central: fundo escuro + canvas
        StackPane areaCanvas = new StackPane(canvas);
        areaCanvas.setStyle("-fx-background-color: #252526;");
        areaCanvas.setPadding(new Insets(8));

        // Permite que o canvas cresça com a janela
        canvas.widthProperty().bind(
                areaCanvas.widthProperty().subtract(16));
        canvas.heightProperty().bind(
                areaCanvas.heightProperty().subtract(16));

        // Redesenha quando o canvas for redimensionado
        canvas.widthProperty().addListener(obs -> contexto.redesenhar());
        canvas.heightProperty().addListener(obs -> contexto.redesenhar());

        setLeft(barraLateral);
        setCenter(areaCanvas);
        setBottom(painelStatus);
    }

    // -------------------------------------------------------------------------
    // Eventos do mouse no canvas → atualiza painel de status
    // -------------------------------------------------------------------------

    private void configurarEventosMouse() {
        canvas.setOnMouseMoved(e -> {
            painelStatus.atualizarCoordenadas(
                    contexto.aplicarSnap(e.getX()),
                    contexto.aplicarSnap(e.getY()));
        });

        canvas.setOnMouseClicked(e -> {
            // Repassa ao contexto (que repassa à ferramenta ativa)
            contexto.getFerramenta().aoClicar(e, contexto);
            atualizarStatus();
        });

        canvas.setOnMouseReleased(e -> {
            contexto.getFerramenta().aoSoltarMouse(e, contexto);
            atualizarStatus();
            marcarAlterado();
        });
    }

    private void atualizarStatus() {
        painelStatus.atualizarTotalFormas(contexto.getFormas().size());
        painelStatus.atualizarMetricas(contexto.getFormasSelecionadas());
    }

    // -------------------------------------------------------------------------
    // Ações públicas (chamadas pela BarraLateralView)
    // -------------------------------------------------------------------------

    public void setFerramenta(Ferramenta ferramenta, String nome) {
        contexto.setFerramenta(ferramenta);
        painelStatus.atualizarFerramenta(nome);
    }

    public void setCorPreenchimento(String hex) {
        contexto.setCorPreenchimentoAtual(hex);
        // Aplica às formas já selecionadas
        for (FormaGeometrica f : contexto.getFormasSelecionadas()) {
            f.setCorPreenchimentoHex(hex);
        }
        contexto.redesenhar();
        marcarAlterado();
    }

    public void setCorBorda(String hex) {
        contexto.setCorBordaAtual(hex);
        for (FormaGeometrica f : contexto.getFormasSelecionadas()) {
            f.setCorBordaHex(hex);
        }
        contexto.redesenhar();
        marcarAlterado();
    }

    public void setEspessuraBorda(double espessura) {
        contexto.setEspessuraBordaAtual(espessura);
        for (FormaGeometrica f : contexto.getFormasSelecionadas()) {
            f.setEspessuraBorda(espessura);
        }
        contexto.redesenhar();
        marcarAlterado();
    }

    public void setGridSnapping(boolean ativo) {
        contexto.setGridSnappingAtivo(ativo);
        contexto.redesenhar();
    }

    public void excluirSelecionados() {
        contexto.removerFormasSelecionadas();
        atualizarStatus();
        marcarAlterado();
    }

    public void trazerParaFrente()  { contexto.trazerParaFrente(); marcarAlterado(); }
    public void enviarParaTras()    { contexto.enviarParaTras();   marcarAlterado(); }
    public void agrupar()           { contexto.agruparSelecionadas(); marcarAlterado(); }
    public void desagrupar()        { contexto.desagruparSelecionadas(); marcarAlterado(); }
    public void duplicar()          { contexto.duplicarSelecionadas(); atualizarStatus(); marcarAlterado(); }

    public void desfazer() {
        contexto.desfazer();
        atualizarStatus();
    }

    public void refazer() {
        contexto.refazer();
        atualizarStatus();
    }

    // -------------------------------------------------------------------------
    // Persistência JSON
    // -------------------------------------------------------------------------

    public void salvar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar projeto");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivo JSON (*.json)", "*.json"));
        File arquivo = fc.showSaveDialog(getScene().getWindow());
        if (arquivo == null) return;

        try {
            String json = serializarFormas(contexto.getFormas());
            Files.writeString(arquivo.toPath(), json, StandardCharsets.UTF_8);
            alteracoesNaoSalvas = false;
            exibirAlerta(Alert.AlertType.INFORMATION, "Salvo",
                    "Projeto salvo com sucesso em:\n" + arquivo.getAbsolutePath());
        } catch (IOException ex) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro ao salvar",
                    "Não foi possível salvar o arquivo:\n" + ex.getMessage());
        }
    }

    public void carregar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Carregar projeto");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivo JSON (*.json)", "*.json"));
        File arquivo = fc.showOpenDialog(getScene().getWindow());
        if (arquivo == null) return;

        try {
            String json = Files.readString(arquivo.toPath(), StandardCharsets.UTF_8);
            List<FormaGeometrica> formasCarregadas = desserializarFormas(json);

            contexto.registrarEstado(); // permite desfazer o carregamento
            contexto.getFormas().clear();
            contexto.getFormas().addAll(formasCarregadas);
            contexto.redesenhar();
            atualizarStatus();
            alteracoesNaoSalvas = false;

        } catch (IOException ex) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro ao carregar",
                    "Não foi possível ler o arquivo:\n" + ex.getMessage());
        } catch (JsonParseException | IllegalStateException ex) {
            exibirAlerta(Alert.AlertType.ERROR, "Arquivo corrompido",
                    "O arquivo selecionado está corrompido ou em formato inválido.\n"
                            + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Serialização JSON (Gson)
    // -------------------------------------------------------------------------

    private String serializarFormas(List<FormaGeometrica> formas) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray array = new JsonArray();

        for (FormaGeometrica forma : formas) {
            JsonObject obj = new JsonObject();
            obj.addProperty("tipo", forma.getClass().getSimpleName());
            obj.addProperty("corPreenchimento", forma.getCorPreenchimentoHex());
            obj.addProperty("corBorda", forma.getCorBordaHex());
            obj.addProperty("espessuraBorda", forma.getEspessuraBorda());
            if (forma.getGroupId() != null) {
                obj.addProperty("groupId", forma.getGroupId());
            }

            // Pontos
            JsonArray pontos = new JsonArray();
            for (Ponto p : forma.getPontos()) {
                JsonObject pObj = new JsonObject();
                pObj.addProperty("x", p.getX());
                pObj.addProperty("y", p.getY());
                pontos.add(pObj);
            }
            obj.add("pontos", pontos);

            // Raio (para Circulo, Quadrado e Hexagono)
            if (forma instanceof Circulo c) {
                obj.addProperty("raio", c.getRaio());
            } else if (forma instanceof Quadrado q) {
                obj.addProperty("raio", q.getRaio());
            } else if (forma instanceof Hexagono h) {
                obj.addProperty("raio", h.getRaio());
            }

            array.add(obj);
        }

        return gson.toJson(array);
    }

    private List<FormaGeometrica> desserializarFormas(String json) {
        Gson gson = new Gson();
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();
        java.util.List<FormaGeometrica> lista = new java.util.ArrayList<>();

        for (JsonElement el : array) {
            JsonObject obj = el.getAsJsonObject();
            String tipo = obj.get("tipo").getAsString();

            JsonArray pontosJson = obj.getAsJsonArray("pontos");

            FormaGeometrica forma = switch (tipo) {
                case "Poligono" -> {
                    Poligono pol = new Poligono();
                    for (JsonElement pe : pontosJson) {
                        JsonObject pObj = pe.getAsJsonObject();
                        pol.adicionarPonto(new Ponto(
                                pObj.get("x").getAsDouble(),
                                pObj.get("y").getAsDouble()));
                    }
                    yield pol;
                }
                case "Circulo" -> {
                    JsonObject centro = pontosJson.get(0).getAsJsonObject();
                    double raio = obj.get("raio").getAsDouble();
                    yield new Circulo(
                            new Ponto(centro.get("x").getAsDouble(),
                                    centro.get("y").getAsDouble()), raio);
                }
                case "Quadrado" -> {
                    JsonObject centro = pontosJson.get(0).getAsJsonObject();
                    double raio = obj.get("raio").getAsDouble();
                    yield new Quadrado(
                            new Ponto(centro.get("x").getAsDouble(),
                                    centro.get("y").getAsDouble()), raio);
                }
                case "Hexagono" -> {
                    JsonObject centro = pontosJson.get(0).getAsJsonObject();
                    double raio = obj.get("raio").getAsDouble();
                    yield new Hexagono(
                            new Ponto(centro.get("x").getAsDouble(),
                                    centro.get("y").getAsDouble()), raio);
                }
                default -> throw new IllegalStateException("Tipo desconhecido: " + tipo);
            };

            // Aplica estilos
            forma.setCorPreenchimentoHex(obj.get("corPreenchimento").getAsString());
            forma.setCorBordaHex(obj.get("corBorda").getAsString());
            forma.setEspessuraBorda(obj.get("espessuraBorda").getAsDouble());
            if (obj.has("groupId")) {
                forma.setGroupId(obj.get("groupId").getAsString());
            }

            lista.add(forma);
        }

        return lista;
    }

    // -------------------------------------------------------------------------
    // Diálogo de saída com alterações não salvas
    // -------------------------------------------------------------------------

    public boolean temAlteracoesNaoSalvas() {
        return alteracoesNaoSalvas && !contexto.getFormas().isEmpty();
    }

    public void exibirDialogoSair(Stage stage) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Sair sem salvar?");
        alerta.setHeaderText("Há alterações não salvas.");
        alerta.setContentText("Deseja salvar antes de sair?");

        ButtonType btnSalvarSair = new ButtonType("Salvar e sair");
        ButtonType btnSairSemSalvar = new ButtonType("Sair sem salvar");
        ButtonType btnCancelar = ButtonType.CANCEL;

        alerta.getButtonTypes().setAll(btnSalvarSair, btnSairSemSalvar, btnCancelar);

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isEmpty() || resultado.get() == btnCancelar) return;

        if (resultado.get() == btnSalvarSair) {
            salvar();
        }

        stage.close();
    }

    // -------------------------------------------------------------------------
    // Auxiliares
    // -------------------------------------------------------------------------

    private void marcarAlterado() {
        alteracoesNaoSalvas = true;
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}