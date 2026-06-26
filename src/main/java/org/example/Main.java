package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.Views.ContainerApp;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        ContainerApp container = new ContainerApp();

        Scene scene = new Scene(container, 1280, 780);
        stage.setTitle("Editor de Desenho Vetorial — POO1 UniRV");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        // Avisa sobre alterações não salvas ao fechar
        stage.setOnCloseRequest(evento -> {
            if (container.temAlteracoesNaoSalvas()) {
                evento.consume(); // cancela o fechamento
                container.exibirDialogoSair(stage);
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}