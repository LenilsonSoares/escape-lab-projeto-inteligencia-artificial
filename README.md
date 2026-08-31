# Escape Lab

Projeto final da disciplina Projeto de Inteligência Artificial. O jogo é uma aventura 2D top-down em Java e JavaFX, ambientada em um laboratório cujo sistema de segurança saiu de controle.

## Executar

Pré-requisito: JDK 21 ou superior configurado no `JAVA_HOME`.

No Windows (PowerShell):

```powershell
.\mvnw.cmd clean javafx:run
```

O Maven Wrapper baixa automaticamente o Maven e as dependências do JavaFX. O jogador verde continua controlável por WASD ou pelas setas direcionais. O robô amarelo é autônomo e inicia sua rota assim que a aplicação é executada.

## Testes

```powershell
.\mvnw.cmd clean verify
```

A suíte atual possui 34 testes automatizados para o Game Loop, movimento do jogador, tilemap, colisões, algoritmo A* e deslocamento do agente.

## Situação atual

O projeto possui:

- janela e Canvas em JavaFX;
- Game Loop com delta time;
- separação entre entrada, atualização e renderização;
- jogador controlável por WASD ou setas;
- mapa lógico de laboratório com 15 linhas e 15 colunas;
- tiles de 40 pixels para piso, parede e equipamento;
- corredores e obstáculos definidos na matriz;
- colisão com paredes, equipamentos e limites do mapa;
- implementação própria do algoritmo A*;
- robô autônomo com início e destino fixos;
- caminho destacado durante a execução;
- testes automatizados com JUnit 5.

O piso azul-escuro é transitável. As paredes cinzas e os equipamentos vermelhos são bloqueados.

## Navegação com A*

### Tilemap como estrutura navegável

O `TileMap` armazena o laboratório em uma matriz. Cada tile transitável é tratado como um nó do espaço de busca. Um nó pode se conectar aos seus vizinhos de cima, direita, baixo e esquerda quando eles também são transitáveis.

Paredes, equipamentos e posições fora da matriz não são adicionados ao caminho. Todos os movimentos possuem custo 1.

### Início e destino

As coordenadas da matriz começam em zero. Nesta atividade, o robô utiliza:

- início: linha 13, coluna 1;
- destino: linha 1, coluna 13.

O caminho calculado para o laboratório possui 24 movimentos e inclui 25 posições quando o início e o destino são contados.

### Heurística

Como o robô se move somente em quatro direções, foi utilizada a distância Manhattan:

```text
h = |linha atual - linha destino| + |coluna atual - coluna destino|
```

A implementação utiliza `PriorityQueue`, mapas e listas da biblioteca padrão do Java. Nenhuma biblioteca externa de busca de caminho foi adicionada.

### Execução da rota

O A* calcula a rota uma única vez quando o mundo é criado. O agente percorre os pontos na ordem retornada, sempre mirando o centro do próximo tile. O movimento usa delta time, verifica se o corpo do robô permanece em uma área válida e limita o último passo para não ultrapassar o ponto desejado.

Ao chegar ao destino, o agente para e permanece nessa posição.

### Representação visual

- a linha azul-clara mostra o caminho calculado;
- o círculo amarelo identifica o destino;
- o robô amarelo percorre a rota;
- o painel informa a quantidade de passos e o estado `EM ROTA` ou `DESTINO ALCANÇADO`.

## Testar manualmente

1. Execute o jogo com `.\mvnw.cmd clean javafx:run`.
2. Observe o caminho azul-claro entre o início e o destino.
3. Confirme que o robô amarelo segue os corredores sem atravessar paredes ou equipamentos.
4. Aguarde até o painel exibir `DESTINO ALCANÇADO`.
5. Verifique que o robô permanece parado no destino.
6. Use WASD ou as setas para confirmar que o jogador continua com o controle manual.

## Limites desta etapa

O destino e os obstáculos permanecem fixos. Ainda não foram implementados obstáculos dinâmicos, recálculo de rota, perseguição do jogador ou custos diferentes por tipo de terreno.

## Participantes da implementação

- Alex Oliveira Santos;
- Alice Gomes Aragao;
- Ana Clara Ribeiro da Silva;
- Kayky Ribeiro Souza;
- Lenilson Dias Soares.

## Documentação

- [Contexto completo do projeto](docs/CONTEXTO-DO-PROJETO.md)
- [Visão geral da disciplina](docs/Projeto-de-Inteligencia-Artificial.pdf)
- [Arquitetura do jogo e Game Loop](docs/Arquitetura-do-Jogo-e-Game-Loop.pdf)
- [Mapas em tiles, colisões e representação do ambiente](docs/Mapas-em-Tiles-Colisoes-e-Representacao-do-Ambiente.pdf)
- [Temática e história do Escape Lab](docs/Projeto+Final+—+Temática+e+História+do+Jogo.pdf)
