# Escape Lab

Projeto final da disciplina Projeto de Inteligência Artificial. O jogo é uma aventura 2D top-down em Java e JavaFX, ambientada em um laboratório cujo sistema de segurança saiu de controle.

## Executar

Pré-requisito: JDK 21 ou superior configurado no `JAVA_HOME`.

No Windows (PowerShell):

```powershell
.\mvnw.cmd clean javafx:run
```

O Maven Wrapper baixa automaticamente o Maven e as dependências do JavaFX. O jogador verde é controlado por WASD ou pelas setas direcionais. O robô amarelo usa o A* para seguir o tile atual do jogador e atualiza sua rota quando o destino muda.

## Testes

```powershell
.\mvnw.cmd clean verify
```

A suíte atual possui 41 testes automatizados para o Game Loop, movimento do jogador, tilemap, colisões, algoritmo A*, troca de rota e deslocamento do agente.

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
- listas aberta e fechada durante a busca;
- robô autônomo com destino atualizado pela posição do jogador;
- recálculo da rota quando o jogador muda de tile;
- intervalo mínimo entre buscas para evitar o uso do A* em todos os frames;
- caminho atual destacado durante a execução;
- testes automatizados com JUnit 5.

O piso azul-escuro é transitável. As paredes cinzas e os equipamentos vermelhos são bloqueados.

## Navegação com A* dinâmico

### Tilemap como estrutura navegável

O `TileMap` armazena o laboratório em uma matriz. Cada tile transitável é tratado como um nó do espaço de busca. Um nó pode se conectar aos seus vizinhos de cima, direita, baixo e esquerda quando eles também são transitáveis.

Paredes, equipamentos e posições fora da matriz não são adicionados ao caminho. Todos os movimentos possuem custo 1.

### Início e destino

As coordenadas da matriz começam em zero. O robô começa no tile da linha 13 e coluna 1. O destino inicial é o tile ocupado pelo jogador, que começa na linha 1 e coluna 4.

Durante a execução, o centro do jogador em pixels é convertido para linha e coluna da matriz. Quando ele entra em outro tile transitável, esse tile passa a ser o novo destino do robô.

### Heurística

Como o robô se move somente em quatro direções, foi utilizada a distância Manhattan:

```text
h = |linha atual - linha destino| + |coluna atual - coluna destino|
```

A implementação utiliza `PriorityQueue` como lista aberta, um conjunto como lista fechada e mapas para guardar o menor custo e a posição anterior. Nenhuma biblioteca externa de busca de caminho foi adicionada.

### Atualização da rota

A primeira rota é calculada quando o mundo é criado. Depois disso, o jogo compara o tile do jogador com o último destino usado pelo agente. Uma nova busca é solicitada somente quando esses tiles são diferentes.

Foi adotado um intervalo mínimo de 200 milissegundos entre duas buscas. Se o jogador mudar de tile durante esse período, o A* usa a posição mais recente assim que o intervalo termina. Dessa forma, o caminho continua sendo reutilizado enquanto for válido e o algoritmo não é executado em todos os frames.

A nova busca parte do tile atual do robô. Ao receber a rota atualizada, ele mantém sua posição na tela e segue os novos pontos sem teletransporte.

### Execução da rota

O agente percorre os pontos na ordem retornada pelo A*, sempre mirando o centro do próximo tile. O movimento usa delta time, verifica se o corpo do robô permanece em uma área válida e limita o último passo para não ultrapassar o ponto desejado.

Ao chegar ao destino, o agente para. Se o jogador mudar novamente de tile, uma nova rota é calculada e o robô volta a se movimentar. Caso não exista caminho para um destino, o agente para até que o jogador entre em outro tile.

### Representação visual

- a linha azul-clara mostra o caminho atual;
- o círculo amarelo identifica o destino atual e fica vermelho quando não existe uma rota até ele;
- o robô amarelo percorre a rota;
- o jogador verde controla a mudança do destino;
- o painel informa a quantidade de passos e o estado `EM ROTA`, `ALVO ALCANÇADO` ou `SEM ROTA`.

Quando uma nova busca é realizada, o caminho exibido também é atualizado.

## Testar manualmente

1. Execute o jogo com `.\mvnw.cmd clean javafx:run`.
2. Observe a rota inicial entre o robô amarelo e o jogador verde.
3. Mova o jogador para outro tile usando WASD ou as setas.
4. Aguarde até 200 milissegundos e confirme que a linha azul passa a terminar no novo destino.
5. Caminhe por corredores diferentes e verifique que o robô não atravessa paredes ou equipamentos.
6. Pare o jogador e aguarde o painel exibir `ALVO ALCANÇADO`.
7. Confirme que o robô fica parado e volta a andar quando o jogador muda novamente de tile.

## Limites desta etapa

Os obstáculos continuam fixos durante a execução. Ainda não foram implementados obstáculos dinâmicos, custos diferentes por terreno, previsão do movimento do jogador ou deslocamento diagonal do robô.

A rota é recalculada quando o destino muda de tile, respeitando o intervalo mínimo entre buscas. Pequenos movimentos dentro do mesmo tile não executam novamente o A*.

## Participantes da implementação

- Alex Oliveira Santos;
- Alice Gomes Aragao;
- Ana Clara Ribeiro da Silva;
- Kayky Ribeiro Souza;
- Lenilson Dias Soares.
