package org.example.Controller;

import org.example.Models.FormaGeometrica;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Gerencia o histórico de estados para Desfazer/Refazer.
 * Implementa o padrão Memento: cada estado é uma cópia profunda
 * da lista de formas no momento do registro.
 */
public class HistoricoController {

    // Pilha de estados anteriores (para desfazer)
    private final Stack<List<FormaGeometrica>> pilhaDesfazer = new Stack<>();

    // Pilha de estados futuros (para refazer)
    private final Stack<List<FormaGeometrica>> pilhaRefazer = new Stack<>();

    // Limite de estados armazenados para não consumir memória demais
    private static final int LIMITE_HISTORICO = 50;

    // -------------------------------------------------------------------------
    // Registrar estado atual (chame ANTES de qualquer operação destrutiva)
    // -------------------------------------------------------------------------

    public void registrarEstado(List<FormaGeometrica> formasAtuais) {
        // Limpa o histórico de refazer ao registrar uma nova ação
        pilhaRefazer.clear();

        // Mantém o limite máximo de estados
        if (pilhaDesfazer.size() >= LIMITE_HISTORICO) {
            pilhaDesfazer.remove(0);
        }

        pilhaDesfazer.push(clonarLista(formasAtuais));
    }

    // -------------------------------------------------------------------------
    // Desfazer
    // -------------------------------------------------------------------------

    /**
     * Retorna o estado anterior ao atual.
     * O estado atual (antes de desfazer) deve ser passado para ser salvo em refazer.
     *
     * @param formasAtuais lista atual (será salva para refazer)
     * @return lista de formas do estado anterior, ou null se não houver histórico
     */
    public List<FormaGeometrica> desfazer(List<FormaGeometrica> formasAtuais) {
        if (pilhaDesfazer.isEmpty()) return null;

        pilhaRefazer.push(clonarLista(formasAtuais));
        return clonarLista(pilhaDesfazer.pop());
    }

    // -------------------------------------------------------------------------
    // Refazer
    // -------------------------------------------------------------------------

    /**
     * Avança para o estado seguinte (reaplica ação desfeita).
     *
     * @param formasAtuais lista atual (será salva para desfazer)
     * @return lista de formas do estado futuro, ou null se não houver nada para refazer
     */
    public List<FormaGeometrica> refazer(List<FormaGeometrica> formasAtuais) {
        if (pilhaRefazer.isEmpty()) return null;

        pilhaDesfazer.push(clonarLista(formasAtuais));
        return clonarLista(pilhaRefazer.pop());
    }

    // -------------------------------------------------------------------------
    // Consultas de estado
    // -------------------------------------------------------------------------

    public boolean podeDesfazer() {
        return !pilhaDesfazer.isEmpty();
    }

    public boolean podeRefazer() {
        return !pilhaRefazer.isEmpty();
    }

    public void limpar() {
        pilhaDesfazer.clear();
        pilhaRefazer.clear();
    }

    // -------------------------------------------------------------------------
    // Cópia profunda da lista (Prototype de cada forma)
    // -------------------------------------------------------------------------

    private List<FormaGeometrica> clonarLista(List<FormaGeometrica> original) {
        List<FormaGeometrica> copia = new ArrayList<>();
        for (FormaGeometrica forma : original) {
            copia.add(forma.clonar());
        }
        return copia;
    }
}