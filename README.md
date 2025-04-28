# Falling Blocks Game

A simplified Tetris-like game implemented in Java using JavaFX.

## Features

- Grid-based game board
- Different block shapes
- Block rotation and movement
- Line clearing and scoring
- Game over detection

## Requirements

- Java 11 or higher
- Maven

## How to Build and Run

1. Clone the repository
2. Navigate to the project directory
3. Build the project:
   ```bash
   mvn clean package
   ```
4. Run the game:
   ```bash
   mvn javafx:run
   ```

## Controls

- Left Arrow: Move block left
- Right Arrow: Move block right
- Down Arrow: Move block down faster
- Up Arrow: Rotate block

## Game Rules

1. Blocks fall from the top of the screen
2. Use the controls to move and rotate blocks
3. Complete horizontal lines to clear them and earn points
4. Game ends when blocks reach the top of the screen 