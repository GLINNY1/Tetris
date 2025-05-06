# Falling Blocks (Tetris) Multiplayer Game

A multiplayer Tetris game built with Java and JavaFX.

## Features

- Grid-based game board
- Different block shapes
- Block rotation and movement
- Line clearing and scoring
- Game over detection

## Requirements

- Java 21 or later
- Maven

## How to Run

1. Clone the repository:
```bash
git clone https://github.com/GLINNY1/Tetris.git
cd Tetris
```

2. Build and run the game:
```bash
mvn clean compile javafx:run
```

## How to Play Multiplayer

### Hosting a Game
1. Click "Host Game" button
2. Make sure your firewall allows incoming connections on port 5000
3. Share your IP address with your friend

### Joining a Game
1. Click "Join Game" button
2. Enter the host's IP address
3. Click OK to connect

## Controls

- Left/Right Arrow: Move piece horizontally
- Up Arrow: Rotate piece
- Down Arrow: Soft drop
- Space: Hard drop
- C: Save piece
- R: Restart (when game over)

## Multiplayer Features

- Real-time game state synchronization
- Visual feedback of opponent's moves
- Player join/leave notifications
- Automatic reconnection handling

## Game Rules

1. Blocks fall from the top of the screen
2. Use the controls to move and rotate blocks
3. Complete horizontal lines to clear them and earn points
4. Game ends when blocks reach the top of the screen 