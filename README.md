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

A base da atividade de Game Loop foi reaproveitada. Ela já possui:

- janela e Canvas em JavaFX;
- Game Loop com delta time;
- separação entre entrada, atualização e renderização;
- jogador controlável;
- painel de depuração com posição, FPS e delta time;
- testes automatizados.

O próximo incremento é o mapa em tiles do laboratório, com terrenos, obstáculos e colisões.

## Documentação

- [Contexto completo do projeto](docs/CONTEXTO-DO-PROJETO.md)
- [Visão geral da disciplina](docs/Projeto-de-Inteligencia-Artificial.pdf)
- [Arquitetura do jogo e Game Loop](docs/Arquitetura-do-Jogo-e-Game-Loop.pdf)
- [Mapas em tiles, colisões e representação do ambiente](docs/Mapas-em-Tiles-Colisoes-e-Representacao-do-Ambiente.pdf)
- [Temática e história do Escape Lab](docs/Projeto+Final+—+Temática+e+História+do+Jogo.pdf)
