# Escape Lab

Projeto final da disciplina Projeto de Inteligência Artificial. O jogo é uma aventura 2D top-down em Java e JavaFX, ambientada em um laboratório cujo sistema de segurança saiu de controle.

## Executar

Pré-requisito: JDK 21 ou superior configurado no `JAVA_HOME`.

No Windows (PowerShell):

```powershell
.\mvnw.cmd clean javafx:run
```

O Maven Wrapper baixa automaticamente o Maven e as dependências do JavaFX. Pressione `ENTER` no briefing inicial. O jogador azul-esverdeado é controlado por WASD ou pelas setas direcionais. O robô amarelo usa o A* para navegar até o tile do jogador, utilizado nesta etapa como destino dinâmico. A rota é atualizada quando esse destino muda. Use `ESC` para pausar, `1`, `2` e `3` para trocar o laboratório, `TAB` para mostrar ou ocultar os painéis de diagnóstico e `F11` para alternar a tela cheia.

## Testes

```powershell
.\mvnw.cmd clean verify
```

A suíte atual possui 69 testes automatizados para o Game Loop, entrada, início e pausa, movimento do jogador, animação e temas visuais, três tilemaps, saídas, progressão, colisões, algoritmo A*, troca de rota, deslocamento do agente, layout da interface e integridade dos assets visuais.

## Situação atual

O projeto possui:

- janela e Canvas em JavaFX;
- Game Loop com delta time;
- separação entre entrada, atualização e renderização;
- jogador controlável por WASD ou setas;
- três mapas lógicos de laboratório, cada um com 15 linhas e 20 colunas, acima do mínimo de 15 × 15;
- tiles de 40 pixels para piso, parede e equipamento;
- corredores e obstáculos definidos na matriz;
- colisão com paredes, equipamentos e limites do mapa;
- implementação própria do algoritmo A*;
- listas aberta e fechada durante a busca;
- robô autônomo com destino atualizado pela posição do jogador;
- recálculo da rota quando o jogador muda de tile;
- intervalo mínimo entre buscas para evitar o uso do A* em todos os frames;
- caminho atual destacado durante a execução;
- minimapa construído a partir da mesma matriz usada pelo A*;
- texturas e sprites originais organizados em `src/main/resources`;
- variações visuais de pisos, paredes, consoles e equipamentos científicos;
- iluminação decorativa por setores, sem alterar o mapa lógico;
- interface responsiva para 720p, Full HD e 4K;
- painel de diagnóstico que pode ser ocultado com `TAB`;
- pausa que interrompe a atualização do mundo e pode ser alternada com `ESC`;
- briefing inicial que aguarda `ENTER` antes de começar a simulação;
- indicador no cabeçalho com o progresso real pelos três setores;
- identidade de cor própria para rota de fuga, núcleo de dados e contenção;
- placas e equipamentos coerentes com a finalidade visual de cada setor;
- troca de mapa pelas teclas `1`, `2` e `3`;
- saída verde que carrega o próximo laboratório automaticamente;
- sinal luminoso na saída e identificação visual breve ao entrar em cada mapa;
- iluminação dinâmica, varredura tecnológica e sinais animados nos equipamentos;
- personagens orientados pela direção e animados somente durante o deslocamento;
- tela de conclusão da fuga ao alcançar a saída do terceiro mapa;
- estado final sem rota antiga, mostrando a saída e o caminho como concluídos;
- testes automatizados com JUnit 5.

O piso metálico azul-escuro é transitável. As paredes modulares, os terminais e as cápsulas científicas são bloqueados. As imagens alteram somente a aparência; a propriedade lógica de cada tile continua definida pelo `TileType`.

## Mapas disponíveis

Os três mapas utilizam a mesma estrutura lógica e o mesmo algoritmo A*. A sequência serve para demonstrar o funcionamento da navegação em disposições diferentes de corredores e obstáculos:

1. `Rota de Fuga`: mapa original do laboratório;
2. `Núcleo de Dados`: corredores divididos em alas e uma passagem central;
3. `Contenção`: salas simétricas ligadas por um corredor principal.

Cada mapa possui uma posição inicial válida para o jogador, outra para o robô e uma saída verde. Quando o jogador alcança a saída, o próximo laboratório é carregado, as entidades voltam aos novos pontos iniciais e uma nova rota é calculada. A saída do terceiro mapa conclui a fuga. Todos os pisos transitáveis permanecem conectados.

As teclas `1`, `2` e `3` continuam disponíveis somente como atalhos para demonstrar um mapa específico.

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
- o marcador amarelo identifica o destino atual e fica vermelho quando não existe uma rota até ele;
- o robô amarelo percorre a rota;
- o jogador azul-esverdeado controla a mudança do destino;
- o minimapa representa os mesmos pisos, bloqueios, posições e caminho do mapa principal;
- antes do início, o painel mostra `AGUARDANDO INÍCIO`;
- durante a execução, o painel informa a quantidade de passos e o estado `EM ROTA`, `DESTINO ALCANÇADO` ou `SEM ROTA`;
- a rota possui uma animação visual leve, e o destino pulsa para facilitar a identificação;
- tiles e sprites são posicionados em pixels inteiros, com suavização desativada;
- o mapa estático é armazenado em cache já na escala final para evitar redimensionamentos sucessivos;
- o layout aumenta proporcionalmente em Full HD e 4K, preservando a organização do mapa e dos painéis.

Quando uma nova busca é realizada, o caminho exibido também é atualizado.

## Testar manualmente

1. Execute o jogo com `.\mvnw.cmd clean javafx:run`.
2. Confira o briefing e pressione `ENTER` para iniciar a simulação.
3. Observe a rota inicial entre o robô amarelo e o jogador azul-esverdeado.
4. Mova o jogador para outro tile usando WASD ou as setas.
5. Aguarde até 200 milissegundos e confirme que a linha azul passa a terminar no novo destino.
6. Caminhe por corredores diferentes e verifique que o robô não atravessa paredes ou equipamentos.
7. Pare o jogador e aguarde o painel exibir `DESTINO ALCANÇADO`.
8. Confirme que o robô fica parado e volta a andar quando o jogador muda novamente de tile.
9. Pressione `TAB` para ocultar e exibir os dados de diagnóstico.
10. Pressione `ESC` e confirme que jogador e robô permanecem parados até continuar.
11. Pressione `F11` para testar o redimensionamento em tela cheia.
12. Alcance a saída verde e confirme que o próximo laboratório é carregado automaticamente.
13. No terceiro mapa, alcance a última saída e confirme a mensagem `FUGA CONCLUÍDA`.
14. Use `1`, `2` e `3` somente se quiser abrir diretamente um mapa para demonstração.

## Limites desta etapa

Os obstáculos continuam fixos durante a execução. Ainda não foram implementados obstáculos dinâmicos, custos diferentes por terreno, previsão do movimento do jogador ou deslocamento diagonal do robô.

A rota é recalculada quando o destino muda de tile, respeitando o intervalo mínimo entre buscas. Pequenos movimentos dentro do mesmo tile não executam novamente o A*.

## Participantes da implementação

- Alex Oliveira Santos;
- Alice Gomes Aragao;
- Ana Clara Ribeiro da Silva;
- Kayky Ribeiro Souza;
- Lenilson Dias Soares.
