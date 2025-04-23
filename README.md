# Tetris

## 🎮 Game Overview
## A simplified falling blocks game inspired by Tetris, developed using Java. This project is built to demonstrate our understanding of core Java programming concepts, including object-oriented design, GUI development, and event handling. 

## 🧩 Features
### - Graphical interface built using [Swing / JavaFX].
### - 10x20 grid-based playfield.
### - Randomized falling shapes.
### - Player input via arrow keys:
### - Left/Right to move block
### - Down to speed up fall
### - Up to rotate block
### - Automatic block falling using a timed game loop.
### - Line-clearing and score tracking.
### - Game over detection.

Java Concepts Used
##- Object-Oriented Programming (classes for Block, Shape, Board, GameManager).
##- Event-driven programming (keyboard input).
##- Timer-based game loop.
##- GUI rendering with Java Swing / JavaFX.
##- 2D array/grid management for game logic.


FallingBlocks/
├── src/                        # Source code lives here
│   ├── Main.java               # Entry point to launch the game
│   ├── GameWindow.java         # Sets up the JFrame and window properties
│   ├── GamePanel.java          # JPanel that handles drawing and game logic
│   ├── Board.java              # Manages the 2D grid and line-clearing logic
│   ├── GameTimer.java          # Manages timed falling using javax.swing.Timer
│   ├── Shape.java              # Abstract base class for all tetrominoes
│   ├── TetrominoL.java         # A subclass of Shape (L-block)
│   ├── TetrominoI.java         # A subclass of Shape (I-block)
│   ├── TetrominoO.java         # A subclass of Shape (O-block)
│   ├── InputHandler.java       # Handles keyboard input from player
│   └── utils/
│       └── ColorUtils.java     # Optional helper class for consistent colors
│
├── resources/                  # Optional: Add images, icons, or config files
│   └── icon.png                # Game window icon or splash art
│
├── README.md                   # Project documentation
└── .gitignore                  # If using Git, ignore compiled files
