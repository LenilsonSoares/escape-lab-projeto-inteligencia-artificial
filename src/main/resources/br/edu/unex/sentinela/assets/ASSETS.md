# Assets visuais

As imagens desta pasta foram geradas especificamente para o Escape Lab e ajustadas para uso no tilemap do projeto. Nenhum asset de jogo comercial foi copiado.

Os arquivos possuem 80 × 80 pixels e são desenhados pelo JavaFX em tiles de 40 × 40 pixels, com suavização desativada para preservar o estilo pixel art.

- `lab-floor.png`: piso metálico transitável;
- `lab-floor-alt.png`: variação de piso metálico transitável;
- `lab-wall.png`: parede bloqueada;
- `lab-wall-alt.png`: variação de parede bloqueada;
- `lab-terminal.png`: equipamento bloqueado;
- `lab-bio-pod.png`: equipamento bloqueado;
- `lab-console.png`: console científico bloqueado;
- `lab-reactor.png`: reator selado bloqueado;
- `player.png`: jogador;
- `pathfinder-robot.png`: agente do A*.

A aparência não altera as regras do mapa. O código continua usando `TileType` para decidir quais posições são transitáveis.
