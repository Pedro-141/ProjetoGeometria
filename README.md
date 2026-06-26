# Editor de Desenho Vetorial Interativo

Projeto prático da disciplina **Programação Orientada a Objetos 1** — Avaliação N3, Semestre 2026/1.

**Universidade de Rio Verde — UniRV**  
**Docente:** Prof. Me. Sergio Souza Novak

---

## Descrição

Software desktop desenvolvido em Java com JavaFX que permite criar, editar, transformar e persistir formas geométricas 2D em uma tela interativa. O projeto aplica padrões clássicos de projeto da GoF (State, Prototype, Memento e Composite) e segue a arquitetura MVC com pacotes separados para Models, Controller e Views.

---

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 LTS |
| JavaFX | 17.0.10 |
| Apache Maven | 3.9+ |
| Gson (persistência JSON) | 2.10.1 |
| JUnit Jupiter (testes) | 5.10.0 |

---

## Como compilar e executar

### Pré-requisitos

- JDK 17 ou superior instalado
- Maven 3.9+ instalado **ou** IntelliJ IDEA (que já inclui Maven embutido)

### Pelo IntelliJ IDEA (recomendado)

1. Abra o IntelliJ e selecione **Open** → escolha a pasta `Trabalho_N3`
2. O IntelliJ reconhece o `pom.xml` automaticamente e baixa as dependências
3. Aguarde a indexação concluir
4. Clique com o botão direito em `src/main/java/org/example/Main.java` → **Run 'Main'**

### Pelo terminal (com Maven instalado no PATH)

```bash
# Entrar na pasta do projeto
cd Trabalho_N3

# Compilar e baixar dependências
mvn compile

# Executar a aplicação
mvn javafx:run

# Gerar JAR executável
mvn package
```

---

## Estrutura de pacotes

```
src/main/java/org/example/
│
├── Main.java                          ← Ponto de entrada JavaFX
│
├── Models/                            ← Camada de domínio (formas geométricas)
│   ├── FormaGeometrica.java           (interface — contrato completo)
│   ├── FormaBase.java                 (classe abstrata — comportamentos comuns)
│   ├── Ponto.java                     (coordenada 2D com clone())
│   ├── Poligono.java                  (polígono genérico — fórmula de Gauss)
│   ├── Circulo.java                   (círculo com raio > 0)
│   ├── Quadrado.java                  (vértices calculados automaticamente)
│   └── Hexagono.java                  (6 vértices calculados automaticamente)
│
├── Controller/                        ← Camada de controle (lógica e padrões)
│   ├── Ferramenta.java                (interface State)
│   ├── ModoFerramenta.java            (enum: TRANSLADAR, ESCALAR, ROTACIONAR, CISALHAR)
│   ├── EditorContext.java             (contexto State + renderização + Memento)
│   ├── HistoricoController.java       (pilhas de desfazer/refazer — Memento)
│   ├── FerramentaDesenhar.java        (polígonos por cliques)
│   ├── FerramentaSelecionar.java      (clique + caixa de seleção)
│   ├── FerramentaTransformar.java     (escala, rotação, cisalhamento por arrasto)
│   ├── FerramentaInserirForma.java    (círculo, quadrado, hexágono por arrasto)
│   └── FerramentaEditarVertice.java   (arrastar vértices individuais)
│
└── Views/                             ← Camada de apresentação (JavaFX)
    ├── ContainerApp.java              (raiz BorderPane + persistência JSON)
    ├── BarraLateralView.java          (painel lateral com todos os controles)
    └── PainelStatusView.java          (barra de status inferior em tempo real)
```

---

## Padrões de Projeto implementados

### State Pattern
O comportamento do canvas muda conforme a ferramenta ativa, sem condicionais complexos.

- **Contexto:** `EditorContext` mantém referência à ferramenta atual e delega todos os eventos do mouse para ela
- **Interface:** `Ferramenta` define `aoPressionarMouse`, `aoArrastarMouse`, `aoSoltarMouse` e `aoClicar`
- **Estados concretos:** `FerramentaDesenhar`, `FerramentaSelecionar`, `FerramentaTransformar`, `FerramentaInserirForma`, `FerramentaEditarVertice`

```java
// Troca de estado em tempo de execução
contexto.setFerramenta(new FerramentaTransformar(ModoFerramenta.ROTACIONAR));
```

### Prototype Pattern
Permite duplicar formas sem compartilhar referências em memória, garantindo independência entre original e cópia.

- A interface `FormaGeometrica` exige o método `clonar()`
- Cada classe de forma implementa `clonar()` criando uma nova instância com cópia profunda de todos os pontos via `Ponto.clone()`
- Usado no `HistoricoController` (para salvar estados) e em `duplicarSelecionadas()`

```java
// Cada forma sabe se clonar com independência de memória
FormaGeometrica copia = forma.clonar();
copia.transladar(20, 20); // não afeta o original
```

### Memento Pattern
Implementa o histórico de Desfazer/Refazer sem expor o estado interno das formas.

- `HistoricoController` mantém duas pilhas: `pilhaDesfazer` e `pilhaRefazer`
- `registrarEstado()` salva uma cópia profunda da lista de formas **antes** de cada operação destrutiva
- `desfazer()` e `refazer()` trocam os estados entre as pilhas
- Limite de 50 estados para gerenciar memória

```java
historico.registrarEstado(formasAtuais);   // salva snapshot
List<FormaGeometrica> anterior = historico.desfazer(formasAtuais); // restaura
```

### Composite Pattern
Formas com o mesmo `groupId` se comportam como uma única entidade lógica para translação e transformação.

- `agruparSelecionadas()` atribui um `UUID` comum a todas as formas selecionadas
- `desagruparSelecionadas()` limpa o `groupId`
- As formas do grupo se movem e transformam juntas

---

## Funcionalidades

### Ferramentas de desenho
- **Linha Livre:** cliques esquerdos adicionam vértices; clique direito ou duplo clique finaliza o polígono (mínimo 3 pontos)
- **Inserir Quadrado / Círculo / Hexágono:** arrasto define o centro e o tamanho com preview em tempo real

### Ferramentas de edição
- **Selecionar:** clique simples seleciona uma forma; Shift + clique para múltipla seleção; arrasto em área vazia cria caixa de seleção
- **Editar Vértice:** clica e arrasta vértices individuais de formas selecionadas
- **Excluir Selecionados:** remove todas as formas selecionadas

### Transformações geométricas
Todas as transformações são aplicadas por arrasto com as formas selecionadas:

| Ferramenta | Ação | Fórmula |
|---|---|---|
| Transladar | Arrastar | `x += dx`, `y += dy` |
| Escalar | Arrastar horizontalmente | multiplica distância ao centroide pelo fator |
| Rotacionar | Arrastar horizontalmente | rotação em torno do centroide em radianos |
| Cisalhar | Arrastar | `x' = x + shx·y`, `y' = y + shy·x` |

### Camadas e grupos
- Trazer para frente / Enviar para trás
- Agrupar e desagrupar formas selecionadas
- Duplicar formas (com offset de 20px)

### Snap to Grid
Quando ativado, as coordenadas do cursor são arredondadas para a grade (20px por padrão), auxiliando no alinhamento preciso das formas.

### Persistência
- **Salvar:** exporta todas as formas para um arquivo `.json` com coordenadas, cores, espessura, groupId e raio (para formas regulares)
- **Carregar:** lê o arquivo `.json`, reconstrói todas as formas e as renderiza no canvas
- **Arquivo corrompido:** exibe alerta gráfico descritivo sem interromper a aplicação
- **Saída com alterações não salvas:** diálogo de confirmação ao fechar a janela

### Barra de status (tempo real)
- Ferramenta ativa
- Coordenadas X/Y do cursor (com snap aplicado)
- Total de formas no canvas
- Área (px²) e perímetro (px) das formas selecionadas

---

## Validações geométricas

| Regra | Implementação |
|---|---|
| Polígono com mínimo 3 pontos | `FerramentaDesenhar` só finaliza com ≥ 3 cliques |
| Círculo com raio > 0 | `IllegalArgumentException` no construtor de `Circulo` |
| Vértices automáticos do Quadrado | `calcularVertices()` com centro + raio |
| Vértices automáticos do Hexágono | 6 ângulos de 60° calculados trigonometricamente |
| Área do polígono pela fórmula de Gauss | `Poligono.calcularArea()` — Shoelace formula |
| Perímetro por distância euclidiana | Soma de `Ponto.distancia()` entre vértices consecutivos |

---

## Critérios de avaliação atendidos

| Critério | Peso | Atendimento |
|---|---|---|
| Modelagem Geométrica e Validações Matemáticas | 25% | Fórmula de Gauss, distância euclidiana, validações no construtor |
| Implementação de Padrões de Projeto | 35% | State, Prototype, Memento e Composite implementados |
| Usabilidade da GUI em JavaFX | 20% | Canvas interativo, barra lateral, status em tempo real, preview de formas |
| Persistência JSON e Tratamento de Erros | 20% | Gson, try-catch, alertas gráficos, diálogo de saída |

---

## Autor

Pedro Henrique Magalhães de Jesus — Curso de Bacharelado em Engenharia de Software, UniRV — 2026/1
