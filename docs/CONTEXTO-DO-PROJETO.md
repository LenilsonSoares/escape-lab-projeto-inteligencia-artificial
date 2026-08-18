# Contexto completo do projeto Escape Lab

Este documento consolida as informações dos materiais da disciplina e as decisões tomadas durante a preparação do repositório. Ele serve como referência para a equipe continuar o trabalho sem depender do histórico da conversa.

Atualizado em: 18/08/2026.

## 1. Disciplina e proposta

- Disciplina: Projeto de Inteligência Artificial — SIS939.
- Curso: Sistemas de Informação, 8º semestre, período 2026.2.
- Equipe: Novo grupo 7, composta por cinco integrantes.
- O semestre utiliza um único projeto prático para aplicar os conteúdos estudados.
- O jogo funciona como laboratório de IA: os agentes precisam apresentar comportamentos observáveis, explicáveis, testáveis e mensuráveis.
- A base é IA clássica. Machine Learning é opcional e não faz parte do escopo mínimo.

## 2. Como as três primeiras atividades se relacionam

### Atividade 1 — Definição de tema do projeto final

- Entrega de um relatório de 1 a 3 páginas em PDF.
- A equipe definiu o jogo Escape Lab.
- O relatório foi enviado em 10/08/2026.
- Essa atividade é o planejamento narrativo e temático do projeto.

### Atividade 2 — Prática de Game Loop

- Prática guiada da Aula 2 usando o exemplo Operação Sentinela.
- A tarefa pediu um projeto JavaFX executável e o envio do link do GitHub.
- Foram praticados arquitetura modular, Canvas, `AnimationTimer`, delta time, entrada, atualização, renderização, movimentação e painel de debug.
- O exemplo Operação Sentinela não é o tema final da equipe.
- Apesar de ser uma prática separada, seu código é genérico e foi reaproveitado como fundação técnica do Escape Lab.
- O repositório enviado nessa atividade deve permanecer disponível e sem alterações até a avaliação do professor.

### Atividade 3 — Criação de tilemap

- É uma evolução prática ligada diretamente ao projeto do jogo.
- O material diz: “Crie ou evolua o mapa do seu projeto de jogo”.
- Portanto, o mapa deve representar o laboratório do Escape Lab.
- Essa é a atividade técnica atual.

## 3. Identidade do jogo

- Nome provisório: Escape Lab.
- Gênero: ação e aventura em perspectiva top-down.
- Tema: fuga de um laboratório de pesquisa.
- Estilo visual obrigatório: pixel art.
- Construção do cenário: mapas em tiles.

## 4. Temática e ambientação

Uma falha no sistema de segurança de um laboratório faz com que os robôs responsáveis pela proteção do local passem a atacar qualquer pessoa encontrada. O ambiente será composto por corredores, salas de pesquisa, depósitos, portas de segurança e áreas restritas.

A ambientação deverá comunicar um laboratório em estado de emergência, com iluminação de alerta, equipamentos danificados, obstáculos, áreas contaminadas e rotas alternativas. A referência geral registrada no relatório é a de jogos de exploração e sobrevivência com visão superior.

## 5. História principal

O protagonista é um funcionário preso dentro do laboratório depois da ativação do sistema de segurança. Para escapar, ele precisa explorar o ambiente, encontrar cartões de acesso, desbloquear novas áreas e evitar os robôs.

O conflito central é sobreviver ao sistema de segurança fora de controle e alcançar a saída. No final inicialmente planejado, o funcionário desliga o sistema e consegue fugir do laboratório.

## 6. Arquétipos de inimigos planejados

### Robô Patrulha

- Vigia os corredores seguindo rotas predefinidas.
- Ao ver ou ouvir o jogador, inicia uma perseguição.
- Futuramente poderá procurar a última posição conhecida após perder o jogador.

### Drone de Vigilância

- Monitora áreas abertas e procura movimentações.
- Ao detectar um som, aproxima-se para investigar.
- Ao enxergar o jogador, inicia a perseguição.

### Robô Guarda

- Protege portas importantes e áreas restritas.
- Normalmente permanece em seu posto.
- Investiga barulhos e persegue o jogador quando o encontra.
- Para se diferenciar melhor, poderá bloquear portas, acionar alarmes ou chamar reforços.

Os três arquétipos precisam diferir pelo comportamento e função, não somente por aparência, vida ou dano.

## 7. Requisitos gerais do projeto semestral

- Jogo 2D em perspectiva top-down.
- Cenários construídos com tiles.
- Estilo visual em pixel art.
- Jogador controlável.
- Obstáculos, colisões e áreas navegáveis.
- No mínimo três arquétipos de inimigos.
- Agentes autônomos com percepção visual e auditiva.
- Patrulha, investigação, busca e perseguição.
- Navegação com implementação própria do algoritmo A*.
- Memória, estados internos e níveis de suspeita.
- Tomada de decisão com FSM, Behavior Tree ou Utility AI, conforme as etapas da disciplina.
- Cooperação entre agentes e compartilhamento de informações por Blackboard.
- Dificuldade dinâmica.
- Ferramentas de depuração e métricas.

## 8. Tecnologias e restrições

- Linguagem: Java.
- Interface e renderização: JavaFX.
- Controle de versão e colaboração: Git e GitHub.
- Não utilizar Unity, Godot, Unreal ou outra game engine.
- A equipe deverá compreender e implementar game loop, mapa, colisões, navegação, percepção, memória e decisão.

## 9. Requisitos da atividade atual — tilemap

### Obrigatórios

- Mapa com no mínimo 15 linhas e 15 colunas.
- Pelo menos três tipos de terreno.
- Áreas transitáveis e não transitáveis.
- Obstáculos definidos em uma matriz.
- Posição inicial válida para o jogador.
- O jogador não pode atravessar tiles bloqueados.
- O jogador deve permanecer dentro dos limites do mapa.
- A camada visual deve corresponder à representação lógica do mapa.

### Desafio adicional

- Criar um terreno transitável com custo de movimento diferente.
- Exemplo: piso normal com custo 1 e área com destroços com custo 2.
- Ainda não é necessário implementar o A*. A propriedade `movementCost` será utilizada futuramente.

### Tipos de tile sugeridos para Escape Lab

- `LAB_FLOOR`: transitável, custo 1.
- `DEBRIS`: transitável, custo 2.
- `WALL`: não transitável.
- Um quarto tipo opcional poderá representar líquido químico, porta ou área perigosa.

## 10. Decisão sobre os repositórios

### Repositório da atividade 2

- Nome: `operacao-sentinela`.
- Endereço: <https://github.com/LenilsonSoares/operacao-sentinela>.
- Foi enviado ao professor para avaliação da Prática de Game Loop.
- Não deve ser apagado, renomeado, tornado privado ou modificado antes da avaliação.

### Repositório do projeto final

- Nome: `escape-lab-projeto-inteligencia-artificial`.
- Endereço: <https://github.com/LenilsonSoares/escape-lab-projeto-inteligencia-artificial>.
- Este é o repositório que deverá receber as próximas implementações do Escape Lab.
- A base técnica foi copiada do projeto Operação Sentinela sem copiar seu diretório `.git`.
- Os históricos Git permanecem independentes.

## 11. Estado atual do código

O novo repositório já possui a base funcional da atividade de Game Loop:

- projeto Maven com Maven Wrapper;
- Java configurado para compatibilidade com JDK 21;
- JavaFX 21;
- janela e Canvas redimensionáveis;
- `AnimationTimer` com delta time;
- fluxo `input -> update -> render`;
- jogador controlado por WASD ou setas;
- movimento diagonal normalizado;
- limite do jogador dentro da janela;
- painel de debug com posição, FPS e delta time;
- testes automatizados com JUnit 5.

A identificação visível e os metadados Maven já foram alterados de Operação Sentinela para Escape Lab. O pacote Java ainda utiliza `br.edu.unex.sentinela`; isso não impede o funcionamento e poderá ser renomeado posteriormente de forma controlada.

Validação realizada em 18/08/2026:

- compilação concluída com sucesso;
- 12 testes executados;
- 0 falhas;
- 0 erros.

O mapa mostrado atualmente pelo renderizador é apenas uma grade visual. Ainda não existe uma matriz lógica de tiles nem colisão com paredes.

## 12. Arquitetura existente

```text
br.edu.unex.sentinela
├── app
│   ├── GameApplication
│   └── Launcher
├── core
│   └── GameEngine
├── entity
│   └── Player
├── game
│   └── GameWorld
├── input
│   ├── InputManager
│   └── MovementInput
├── rendering
│   └── GameRenderer
└── telemetry
    └── FrameMetrics
```

- `GameApplication`: cria a janela, o Canvas e conecta os componentes.
- `GameEngine`: executa o Game Loop.
- `InputManager`: captura o teclado.
- `GameWorld`: mantém e atualiza o estado do jogo.
- `Player`: armazena posição, tamanho, velocidade e movimentação.
- `GameRenderer`: desenha o mundo, o jogador e o painel de debug.
- `FrameMetrics`: calcula FPS e delta time.

## 13. Plano de implementação do tilemap

1. Criar `TileType` com propriedades `walkable` e `movementCost`.
2. Criar `TileMap` com uma matriz de pelo menos 15 por 15.
3. Montar corredores e salas do laboratório, incluindo paredes externas e obstáculos internos.
4. Definir conversões entre coordenadas do grid e coordenadas em pixels.
5. Posicionar o jogador em um tile transitável.
6. Alterar a movimentação para testar colisão antes de confirmar a nova posição.
7. Verificar os eixos X e Y separadamente para permitir movimento suave junto às paredes.
8. Renderizar cada tipo de tile antes de desenhar o jogador.
9. Adaptar o desenho do jogador para representar o funcionário preso no laboratório.
10. Exibir informações úteis de debug, como linha, coluna e tipo do tile atual.
11. Criar testes para dimensões, propriedades dos tiles, limites e colisões.
12. Atualizar o README e registrar imagens da execução, se forem exigidas na entrega.

Não implementar inimigos, visão, som ou A* nesta atividade, salvo se o professor adicionar esses itens aos detalhes da entrega.

## 14. Comandos úteis

Executar os testes:

```powershell
.\mvnw.cmd clean verify
```

Executar o jogo:

```powershell
.\mvnw.cmd clean javafx:run
```

Consultar alterações locais:

```powershell
git status
```

## 15. Fluxo Git recomendado

1. Registrar a base reutilizada em um commit próprio.
2. Enviar esse commit ao GitHub.
3. Criar uma branch para a atividade, por exemplo `feat/tilemap`.
4. Implementar o mapa e os testes em commits pequenos e explicativos.
5. Conferir `git status` e executar `clean verify` antes do envio.
6. Enviar ao Blackboard o link do novo repositório ou da branch indicada pela equipe.

Mensagem sugerida para o commit da base:

```text
chore: inicia projeto Escape Lab com base do game loop
```

## 16. Materiais mantidos neste repositório

- `Projeto-de-Inteligencia-Artificial.pdf`: visão geral, escopo, fases e requisitos do semestre.
- `Arquitetura-do-Jogo-e-Game-Loop.pdf`: conteúdo da Aula 2 e prática de Game Loop.
- `Mapas-em-Tiles-Colisoes-e-Representacao-do-Ambiente.pdf`: conteúdo da Aula 3 e requisitos do tilemap.
- `Projeto+Final+—+Temática+e+História+do+Jogo.pdf`: relatório de temática e história elaborado pela equipe.

## 17. Orientação para continuidade

A pessoa que está retomando o projeto não acompanhou as aulas anteriores. As próximas mudanças devem ser explicadas de forma progressiva, relacionando cada classe aos conceitos dos PDFs e evitando introduzir sistemas futuros antes de concluir o tilemap.

Antes de implementar algo novo, conferir:

- qual requisito da atividade aquela mudança atende;
- em qual classe a responsabilidade deve ficar;
- como observar o comportamento na tela;
- como testar a regra sem depender somente da execução visual.
