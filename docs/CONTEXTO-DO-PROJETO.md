# Contexto do projeto Escape Lab

Este documento reúne as principais informações do projeto, o estado atual da implementação e os limites de escopo definidos para cada atividade da disciplina.

Atualizado em: 18/08/2026.

## 1. Identificação

- Disciplina: Projeto de Inteligência Artificial — SIS939.
- Curso: Sistemas de Informação, 8º semestre, período 2026.2.
- Equipe: Novo grupo 7, composto por cinco integrantes.
- Nome do jogo: Escape Lab.
- Gênero: ação e aventura em perspectiva top-down.
- Tema: fuga de um laboratório de pesquisa.
- Estilo visual planejado para o projeto final: pixel art.
- Tecnologias: Java 21, JavaFX 21, Maven, JUnit 5, Git e GitHub.

## 2. Proposta do projeto

Escape Lab é um jogo 2D em que o protagonista fica preso em um laboratório após uma falha no sistema de segurança. Para escapar, ele deverá explorar corredores, salas de pesquisa e depósitos enquanto evita os robôs responsáveis pela proteção do local.

O projeto será desenvolvido progressivamente durante o semestre. Cada atividade acrescentará um conceito estudado em aula, como Game Loop, mapas em tiles, colisões, navegação, percepção e tomada de decisão.

O projeto utiliza IA clássica. Machine Learning é opcional e não faz parte do escopo mínimo.

## 3. Relação entre as atividades iniciais

### Atividade 1 — Temática e história

- Definiu o nome, o gênero e a ambientação do Escape Lab.
- Estabeleceu o conflito principal e os arquétipos de inimigos planejados.
- O relatório foi entregue em 10/08/2026.

### Atividade 2 — Game Loop

- Utilizou o exemplo Operação Sentinela como prática técnica.
- Implementou janela JavaFX, Canvas, `AnimationTimer`, delta time, entrada, atualização, renderização, movimentação e painel de depuração.
- O código genérico dessa prática foi reaproveitado como base técnica do Escape Lab.
- O repositório `operacao-sentinela` permanece separado e não deve receber alterações relacionadas ao projeto final.

### Atividade 3 — Criação de tilemap

- Evoluiu o projeto com uma representação lógica do laboratório.
- Implementou matriz de tiles, tipos transitáveis e bloqueados, posição inicial, representação visual e colisões.
- Os requisitos obrigatórios da Aula 03 estão implementados e testados.

## 4. Temática e elementos planejados

O laboratório possui corredores, salas de pesquisa, depósitos, equipamentos, portas de segurança e áreas restritas. A ambientação representa um sistema de segurança fora de controle.

Os inimigos planejados para etapas futuras são:

- **Robô Patrulha:** percorre corredores e futuramente poderá perseguir o jogador.
- **Drone de Vigilância:** monitora áreas abertas e poderá investigar sons.
- **Robô Guarda:** protege áreas importantes e poderá reagir a movimentações próximas.

Esses inimigos ainda não foram implementados. Eles pertencem às próximas atividades da disciplina.

## 5. Estado atual da aplicação

A aplicação possui:

- projeto Maven com Maven Wrapper;
- compatibilidade com JDK 21;
- JavaFX 21 e Canvas redimensionável;
- Game Loop com `AnimationTimer`;
- fluxo `input -> update -> render`;
- movimento baseado em delta time;
- controles por WASD e setas direcionais;
- movimento diagonal normalizado;
- painel de depuração com posição, FPS e delta time;
- testes automatizados com JUnit 5.

O pacote Java continua sendo `br.edu.unex.sentinela`. Essa identificação foi mantida para evitar uma alteração estrutural sem relação com a atividade atual.

## 6. Tilemap implementado na Aula 03

O laboratório é representado por uma matriz lógica de 15 linhas e 15 colunas. Cada tile possui 40 pixels, de modo que o mapa ocupa uma área de 600 × 600 pixels.

### Tipos de tile

- `LAB_FLOOR`: piso do laboratório, transitável.
- `WALL`: parede, não transitável.
- `EQUIPMENT`: equipamento ou obstáculo, não transitável.

As paredes externas, divisões internas, corredores e equipamentos são definidos diretamente na matriz de `TileMap`.

Cada posição lógica é acessada por `tileAt(linha, coluna)`. O `TileType` mantém a propriedade `walkable`, usada pela colisão para decidir se uma área pode ser ocupada. A aparência de cada tipo fica sob responsabilidade do `GameRenderer`, mantendo separadas a camada lógica e a camada visual.

### Coordenadas da grade e do mapa

O projeto utiliza dois sistemas de coordenadas:

- grade: linha e coluna identificam uma célula da matriz;
- mapa: X e Y representam posições em pixels dentro do tilemap.

Com tiles de 40 pixels, o canto superior esquerdo de uma célula é calculado por `x = coluna × 40` e `y = linha × 40`. No sentido inverso, a divisão pelo tamanho do tile com arredondamento para baixo indica a célula correspondente a uma posição em pixels.

A verificação de colisão aplica essa conversão aos quatro limites do corpo do jogador e consulta todos os tiles ocupados. Na renderização, um deslocamento é acrescentado apenas para centralizar o mapa na janela; as regras de movimento continuam usando as coordenadas locais do tilemap.

### Posição inicial

A posição inicial corresponde a um tile de piso configurado no mapa. O jogador é centralizado nesse tile, e sua área completa é validada ao calcular as coordenadas iniciais, antes de criar o jogador.

### Representação visual

Nesta atividade, o mapa utiliza apenas formas e cores simples no Canvas:

- piso: azul-escuro;
- paredes: cinza;
- equipamentos: vermelho;
- jogador: desenhado depois do mapa.

A renderização percorre a mesma matriz utilizada pela lógica, garantindo correspondência entre o tile exibido e sua propriedade de colisão.

### Colisão

Antes de confirmar um movimento, o jogo verifica todos os tiles ocupados pelo corpo do jogador. O movimento é aceito somente quando a área completa permanece sobre tiles transitáveis e dentro da matriz.

Os eixos X e Y são verificados separadamente para permitir movimento junto às paredes. O deslocamento também é dividido em pequenos passos para evitar que o jogador atravesse um obstáculo durante uma atualização maior.

Os limites físicos são definidos pelo tilemap, e não pelo tamanho da janela.

## 7. Requisitos obrigatórios da Aula 03

- [x] Mapa com no mínimo 15 linhas e 15 colunas.
- [x] Pelo menos três tipos de tile ou terreno.
- [x] Áreas transitáveis e não transitáveis.
- [x] Obstáculos definidos na matriz.
- [x] Posição inicial válida para o jogador.
- [x] Representação visual correspondente à matriz lógica.
- [x] Mapa desenhado antes do jogador.
- [x] Jogador impedido de atravessar paredes e equipamentos.
- [x] Jogador mantido dentro dos limites do mapa.
- [x] Controles por WASD e setas preservados.
- [x] Movimento baseado em delta time preservado.
- [x] Separação entre entrada, atualização e renderização preservada.

O desafio adicional de custo de movimento não foi implementado. A propriedade `movementCost` poderá ser adicionada quando for necessária para os conteúdos de navegação.

## 8. Arquitetura atual

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
│   ├── GameWorld
│   ├── TileMap
│   └── TileType
├── input
│   ├── InputManager
│   └── MovementInput
├── rendering
│   └── GameRenderer
└── telemetry
    └── FrameMetrics
```

- `GameApplication`: cria a janela, o Canvas e conecta os componentes.
- `GameEngine`: executa o ciclo de entrada, atualização e renderização.
- `InputManager`: captura o estado das teclas.
- `MovementInput`: representa os eixos de movimento de um quadro.
- `GameWorld`: mantém o mapa e o jogador.
- `TileMap`: armazena a matriz, dimensões, posição inicial e regras de ocupação.
- `TileType`: informa o tipo do tile e sua transitabilidade.
- `Player`: mantém posição, tamanho e velocidade e aplica o movimento com colisão.
- `GameRenderer`: desenha o tilemap, o jogador e o painel de depuração.
- `FrameMetrics`: calcula FPS e registra o delta time.

## 9. Testes automatizados

Validação realizada em 18/08/2026:

- 20 testes executados;
- 0 falhas;
- 0 erros;
- 0 testes ignorados;
- compilação e empacotamento concluídos com sucesso.

Distribuição atual:

- `PlayerTest`: 8 testes de movimento, delta time, diagonal, limites e colisões.
- `GameWorldTest`: 3 testes de posição inicial e dimensões da área visível.
- `TileMapTest`: 5 testes de matriz, tipos, transitabilidade, posição inicial e limites.
- `MovementInputTest`: 2 testes dos eixos de movimento.
- `FrameMetricsTest`: 2 testes de delta time e FPS.

## 10. Como executar

Pré-requisito: JDK 21 ou superior configurado no `JAVA_HOME`.

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

## 11. Escopo das próximas aulas

Ainda não estão implementados:

- custo de movimento por terreno;
- algoritmos de busca, incluindo BFS e A*;
- grafos e navegação autônoma;
- inimigos funcionais;
- visão, audição e investigação;
- patrulha, perseguição e busca;
- memória e níveis de suspeita;
- máquinas de estado, Behavior Tree e Utility AI;
- Blackboard e comunicação entre agentes;
- cartões de acesso, portas com lógica e inventário;
- combate, câmera e múltiplas fases;
- sprites, animações e iluminação avançada.

Esses recursos devem ser adicionados somente quando forem trabalhados nas respectivas aulas.

## 12. Repositórios

### Prática de Game Loop

- Repositório: `operacao-sentinela`.
- Endereço: <https://github.com/LenilsonSoares/operacao-sentinela>.
- Deve permanecer separado do projeto final.

### Projeto final

- Repositório: `escape-lab-projeto-inteligencia-artificial`.
- Endereço: <https://github.com/LenilsonSoares/escape-lab-projeto-inteligencia-artificial>.
- Recebe as implementações do Escape Lab durante o semestre.

## 13. Materiais de referência

- `Projeto-de-Inteligencia-Artificial.pdf`: visão geral e requisitos do semestre.
- `Arquitetura-do-Jogo-e-Game-Loop.pdf`: conteúdo da Aula 02.
- `Mapas-em-Tiles-Colisoes-e-Representacao-do-Ambiente.pdf`: grade, matriz, coordenadas, propriedades dos tiles, colisão e atividade da Aula 03.
- `Projeto+Final+—+Temática+e+História+do+Jogo.pdf`: relatório de temática elaborado pela equipe.

## 14. Orientações de continuidade

- Implementar apenas o conteúdo correspondente à atividade atual.
- Manter o Game Loop e a separação entre entrada, atualização e renderização.
- Relacionar cada alteração a um requisito da disciplina.
- Preservar os testes existentes e acrescentar testes para novas regras.
- Executar `clean verify` antes de cada entrega.
- Não modificar o repositório da prática Operação Sentinela ao trabalhar no Escape Lab.
