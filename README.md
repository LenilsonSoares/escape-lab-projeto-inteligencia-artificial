# Escape Lab

Projeto final da disciplina Projeto de Inteligência Artificial. O jogo é uma aventura 2D top-down em Java e JavaFX, ambientada em um laboratório cujo sistema de segurança saiu de controle.

## Executar

Pré-requisito: JDK 25 LTS configurado no `JAVA_HOME`.

No Windows (PowerShell):

```powershell
.\mvnw.cmd clean javafx:run
```

O Maven Wrapper baixa automaticamente o Maven e as dependências do JavaFX. Pressione `ENTER` no briefing inicial.

| Tecla | Ação |
| --- | --- |
| WASD ou setas | mover o jogador |
| `SHIFT` | andar agachado: metade da velocidade e passos mais silenciosos |
| `F2` | alternar entre a IA completa e a demonstração do A* das entregas anteriores |
| `TAB` | mostrar ou ocultar os painéis de depuração |
| `ESC` | pausar |
| `1`, `2`, `3` | abrir um laboratório específico |
| `ENTER` | começar e, depois de capturado, recomeçar |
| `←` `→` | no briefing ou depois da captura, escolher a dificuldade |
| `F11` | tela cheia |

## Objetivo

Em cada laboratório a saída fica atrás de uma porta de segurança trancada. O jogador precisa pegar o cartão de acesso, que fica perto do robô guarda, e depois chegar à saída. Pegar o cartão faz barulho e abre as portas do setor. Na saída do terceiro laboratório o sistema de segurança é desligado e a fuga termina.

A partida começa com 1.000 pontos e cada novo contato com um robô desconta 100. Com zero pontos o jogador é capturado e pode recomeçar com `ENTER`.

## Inteligência dos robôs

Os robôs não sabem onde o jogador está. Cada um percebe o ambiente pelos próprios sensores, guarda o que percebeu na memória e decide o que fazer. O A* continua responsável apenas por calcular o caminho até o destino escolhido.

```text
percepção (visão e audição) -> memória e suspeita -> decisão (FSM ou árvore) -> A* -> movimento
```

### Arquétipos

| Robô | Papel | Rotina | Decisão | Visão | Audição |
| --- | --- | --- | --- | --- | --- |
| Perseguidor (amarelo) | Guarda | parado no posto, vigiando o cartão | FSM | 8 tiles, 70° | 1,3 |
| Patrulheiro (roxo) | Patrulha | percorre quatro pontos | FSM | 7 tiles, 90° | 1,0 |
| Interceptador (vermelho) | Batedor | ronda uma área e segue ruídos | Árvore de comportamento | 6 tiles, 110° | 1,5 |
| Saltador (azul) | Saltador | ronda perto das bancadas e salta um equipamento | FSM | 7 tiles, 80° | 1,0 |

A diferença entre eles está no comportamento: o guarda quase não sai do posto, a patrulha cobre os corredores, o batedor investiga qualquer ruído e, na perseguição, mira dois tiles à frente do jogador, e o saltador corta caminho por cima das bancadas.

### Percepção

- **Visão:** o robô vê o jogador se ele estiver dentro do alcance e do ângulo do cone e se nenhuma parede ou porta fechada estiver no caminho. A linha de visão é verificada percorrendo o segmento entre os dois pontos. Equipamentos são baixos e não bloqueiam a visão.
- **Audição:** andando, o jogador faz um ruído a cada 0,45 s com alcance de 3,5 tiles; agachado, o alcance cai para 1,5 tile. Pegar o cartão faz um ruído de 7 tiles e as portas, ao abrir, de 6 tiles. Cada robô ouve até `alcance do ruído × sensibilidade`.

### Memória e suspeita

Cada percepção vira uma evidência com posição, instante, origem (visão, audição ou rádio) e confiança. A confiança diminui com o tempo: `c = c0 × e^(-0,20 × idade)`. Quando o jogador some de vista, a última posição vista fica congelada; o robô não continua seguindo a posição real atrás da parede.

A suspeita vai de 0 a 100. Ver o jogador de longe soma 25 por segundo, cada ruído novo soma 12 e cada relato de rádio soma 25. Sem estímulo, ela cai 10 por segundo. Os níveis usam histerese para não ficarem trocando:

| Nível | Entra | Sai |
| --- | --- | --- |
| Atenção | suspeita ≥ 25 | suspeita ≤ 15 |
| Investigação | suspeita ≥ 60 | suspeita ≤ 40 |
| Combate | jogador visto a menos de 6 tiles | jogador a mais de 8 tiles ou fora de vista |

### Máquina de estados (Aula 07)

Guarda, patrulha e saltador usam uma FSM com uma classe por estado, cada uma com `enter`, `update` e `exit`. As transições acontecem somente dentro do `update` do estado atual e seguem a prioridade: ameaça primeiro, depois pistas.

| Estado | O que faz | Sai quando |
| --- | --- | --- |
| VIGIAR / PATRULHAR | rotina; em atenção, para e olha para a origem da pista | vê o jogador ou a suspeita chega à investigação |
| INVESTIGAR | vai até a pista e olha em volta por 2 s | vê o jogador, não acha nada ou surge outra pista |
| PERSEGUIR | corre até o tile onde viu o jogador | toca o jogador ou perde o contato |
| ATACAR | depois do toque, para por 1,2 s | o tempo acaba |
| PROCURAR | revista os pontos escolhidos pela Utility AI por até 12 s | vê o jogador ou o prazo termina |
| RETORNAR | volta ao posto ou à rota | chega |
| CERCAR SAÍDA | viu o jogador, mas não há vaga de ataque: vigia a porta da saída | consegue vaga ou passa 6 s sem contato |

O A* é chamado no `enter` de cada estado e, na perseguição, no máximo a cada 0,5 s.

### Árvore de comportamento

O interceptador usa uma árvore com seletor reativo, para comparar com a FSM. A cada quadro os ramos são testados em ordem de prioridade:

```text
Seletor
├── Sequência: tocou o jogador? -> RECUAR
├── Sequência: contato confirmado? -> tem vaga de ataque? -> INTERCEPTAR
├── Sequência: pista válida? -> INVESTIGAR PISTA
└── RONDAR
```

Quando um ramo mais prioritário assume, a ação anterior é cancelada e libera o que tinha reservado, como a vaga de ataque.

### Utility AI na busca

Ao perder o jogador, o robô escolhe onde procurar dando uma nota para cada ponto candidato (a última posição vista, cruzamentos e becos a até 6 tiles dela):

```text
P = 0,45 × evidência + 0,35 × não verificado - 0,20 × custo
```

Pontos sem caminho ou reservados por outro robô são descartados antes da nota. O custo vem do próprio A*.

### Cooperação e blackboard

Os robôs compartilham um blackboard:

- **Rádio:** quem vê o jogador publica um relato. Os outros recebem 0,6 s depois e registram a posição relatada, não a posição atual. O mesmo relato nunca é contado duas vezes.
- **Reservas:** na busca, cada ponto é reservado por um robô, para dois robôs não revistarem o mesmo lugar.
- **Vagas de ataque:** só um número limitado de robôs persegue ao mesmo tempo. Quem fica sem vaga vai cercar a saída.

### Diretor de IA e dificuldade dinâmica

O diretor não dá informação aos robôs. Ele mede a pressão sobre o jogador uma vez por segundo:

```text
q = 0,50 × contatos recentes + 0,30 × pontos perdidos + 0,20 × robôs a até 5 tiles
q suavizado = 0,20 × q + 0,80 × q suavizado anterior
```

Com pressão alta por 5 s ele reduz as vagas de ataque; com pressão baixa por 10 s ele aumenta. As vagas ficam entre 1 e 3, e entre dois ajustes passam pelo menos 10 s.

### Depuração

Sem `TAB` a tela fica limpa para jogar: aparecem só o cone de visão de cada robô, as ondas de som e um "?" ou "!" sobre o robô que desconfiou ou viu o jogador. Com `TAB` aparecem também as rotas, o estado e a barra de suspeita acima de cada robô, os nós visitados pelo último A*, os valores g, h e f, a lista de robôs com técnica e estado, o diretor, o blackboard, as notas da Utility AI e as últimas decisões tomadas com o motivo.

### Níveis de dificuldade

No briefing, `←` e `→` escolhem o nível. O nível define a faixa; o diretor de IA continua ajustando dentro dela.

| Parâmetro | Fácil | Normal | Difícil |
| --- | --- | --- | --- |
| Velocidade dos robôs | × 0,85 | × 1,0 | × 1,15 |
| Alcance da visão | × 0,8 | × 1,0 | × 1,2 |
| Proteção depois de um toque | 3,0 s | 2,0 s | 1,5 s |
| Vagas de ataque do diretor | 1 a 2 | 1 a 3 | 2 a 3 |

### Demonstração do A* (F2)

As atividades de A* e A* dinâmico continuam disponíveis com `F2`. Nesse modo os robôs voltam ao comportamento daquelas entregas: sabem onde o jogador está e não usam percepção. Ele serve só para demonstrar a navegação.

## Tipos de inimigos no modo de demonstração do A*

No modo `F2`, cada setor possui quatro robôs simultâneos, identificados pela cor e pelo nome:

- **Perseguidor amarelo:** usa A* para alcançar o tile atual do jogador; velocidade de 100 pixels por segundo.
- **Patrulheiro roxo:** usa A* para percorrer ciclicamente quatro pontos fixos do laboratório, independentemente do jogador; velocidade de 80 pixels por segundo.
- **Interceptador vermelho:** usa A* para alcançar até dois tiles à frente do jogador na direção do movimento real. A previsão para antes de paredes, equipamentos e limites; quando o jogador para, o destino volta ao tile atual dele. Velocidade de 120 pixels por segundo.
- **Saltador azul:** usa A* para alcançar o tile atual do jogador, mas pode saltar por cima de **um** equipamento quando há piso livre do outro lado. Andar um tile custa 1 e o salto, que avança dois tiles, custa 3. Paredes, dois equipamentos seguidos e pousos fora do piso continuam bloqueados. Durante o salto ele não recalcula a rota; a nova busca espera o pouso. Velocidade de 90 pixels por segundo.

Os quatro usam a mesma implementação do A*, com rotas e destinos independentes. O saltador usa o modo `AStarPathfinder.withEquipmentJumps()`, que acrescenta os saltos como vizinhos de custo 3. A heurística Manhattan continua admissível, pois o salto custa 1,5 por tile percorrido, nunca menos que 1. Todos respeitam o intervalo mínimo de 200 milissegundos entre buscas de cada robô. Os mapas mostram as rotas dos quatro, com os saltos desenhados como arcos tracejados; o minimapa identifica suas posições por cor. Os diagnósticos principais de destino e passos continuam acompanhando o perseguidor amarelo.

Permanecer encostado no mesmo inimigo não gera novas penalidades: é preciso se separar e tocar novamente. Cada inimigo contabiliza seu contato independentemente. A pontuação e o total de penalidades são mantidos ao trocar de setor. Os pontos aparecem no cabeçalho, e o painel de diagnóstico mostra também a quantidade de penalidades.

## Testes

```powershell
.\mvnw.cmd clean verify
```

A suíte atual possui 213 testes automatizados. Além de entrada, movimento, mapas, colisões, A*, saltos, animação, layout e penalidades, eles cobrem visão e oclusão, audição, memória e suspeita com histerese, transições da FSM, ramos da árvore de comportamento, notas da Utility AI, rádio e reservas do blackboard, ajustes do diretor de IA, portas e cartão, captura e a demonstração do A*.

Versões usadas: Java 25, JavaFX 25.0.5, JUnit 6.1.3 e Maven 3.10.1 (baixado pelo Maven Wrapper).

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
- identidade de cor própria para rota, iluminação e equipamentos de cada setor;
- equipamentos coerentes com a finalidade visual de cada setor, sem placas decorativas sobre os corredores;
- troca de mapa pelas teclas `1`, `2` e `3`;
- saída verde que carrega o próximo laboratório automaticamente;
- porta de evacuação, placa e sinal luminoso na saída;
- transição visual curta ao entrar em cada mapa;
- iluminação dinâmica, varredura tecnológica e sinais animados nos equipamentos;
- personagens orientados pela direção e com quatro estados visuais de caminhada;
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

- a linha luminosa, na cor do setor, mostra o caminho atual;
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
5. Aguarde até 200 milissegundos e confirme que a rota luminosa passa a terminar no novo destino.
6. Caminhe por corredores diferentes e verifique que o robô não atravessa paredes ou equipamentos.
7. Pare o jogador e aguarde o painel exibir `DESTINO ALCANÇADO`.
8. Confirme que o robô fica parado e volta a andar quando o jogador muda novamente de tile.
9. Pressione `TAB` para ocultar e exibir os dados de diagnóstico.
10. Pressione `ESC` e confirme que jogador e robô permanecem parados até continuar.
11. Pressione `F11` para testar o redimensionamento em tela cheia.
12. Alcance a saída verde e confirme que o próximo laboratório é carregado automaticamente.
13. No terceiro mapa, alcance a última saída e confirme a mensagem `FUGA CONCLUÍDA`.
14. Use `1`, `2` e `3` somente se quiser abrir diretamente um mapa para demonstração.

## Limites conhecidos

- As portas só abrem; nenhuma volta a fechar durante a partida.
- O movimento continua em quatro direções, sem diagonais.
- Os parâmetros de suspeita, alcance e pressão são valores iniciais, a serem calibrados nos playtests.
- Os robôs não colidem entre si; só o contato com o jogador é verificado.

## Participantes da implementação

- Alex Oliveira Santos;
- Alice Gomes Aragao;
- Ana Clara Ribeiro da Silva;
- Kayky Ribeiro Souza;
- Lenilson Dias Soares.
