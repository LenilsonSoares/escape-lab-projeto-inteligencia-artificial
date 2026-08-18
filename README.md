# Escape Lab

Projeto final da disciplina Projeto de Inteligência Artificial. O jogo será uma aventura 2D top-down em Java e JavaFX, ambientada em um laboratório cujo sistema de segurança saiu de controle.

## Executar

Pré-requisito: JDK 21 ou superior configurado no `JAVA_HOME`.

No Windows (PowerShell):

```powershell
.\mvnw.cmd clean javafx:run
```

O Maven Wrapper baixa automaticamente o Maven e as dependências do JavaFX. Use WASD ou as setas direcionais para movimentar o jogador.

## Testes

```powershell
.\mvnw.cmd clean verify
```

## Situação atual

Além da base do Game Loop, a atividade da Aula 03 — Criação de tilemap — está implementada com:

- janela e Canvas em JavaFX;
- Game Loop com delta time;
- separação entre entrada, atualização e renderização;
- jogador controlável por WASD ou setas;
- mapa lógico de laboratório com 15 linhas e 15 colunas;
- tiles de 40 pixels para piso, parede e equipamento;
- corredores e obstáculos definidos na própria matriz;
- posição inicial do jogador em um piso transitável;
- desenho dos tiles antes do jogador;
- colisão que considera o tamanho inteiro do jogador;
- bloqueio de paredes, equipamentos e limites do mapa;
- painel de depuração com posição, FPS e delta time;
- testes automatizados.

O piso azul-escuro é transitável. As paredes cinzas e os equipamentos vermelhos são bloqueados.

## Testar a colisão manualmente

1. Execute o jogo com `.\mvnw.cmd clean javafx:run`.
2. Use WASD ou as setas para caminhar pelos corredores.
3. Tente avançar contra uma parede cinza e contra um equipamento vermelho: o jogador deve parar antes de sobrepor o tile.
4. Tente alcançar as bordas do laboratório: o jogador deve permanecer dentro da matriz.

Esta atividade não inclui custos de movimento, busca de caminho, inimigos ou outros sistemas de IA das próximas aulas.

## Documentação

- [Contexto completo do projeto](docs/CONTEXTO-DO-PROJETO.md)
- [Visão geral da disciplina](docs/Projeto-de-Inteligencia-Artificial.pdf)
- [Arquitetura do jogo e Game Loop](docs/Arquitetura-do-Jogo-e-Game-Loop.pdf)
- [Mapas em tiles, colisões e representação do ambiente](docs/Mapas-em-Tiles-Colisoes-e-Representacao-do-Ambiente.pdf)
- [Temática e história do Escape Lab](docs/Projeto+Final+—+Temática+e+História+do+Jogo.pdf)
