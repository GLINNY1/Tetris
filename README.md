# 🎮 Falling Blocks Game (Tetris-Inspired)

*A simplified falling blocks game developed in Java to demonstrate core programming concepts like OOP, GUI development, and real-time input handling.*

---

## 🧩 Overview

This project is a minimalist clone of the classic **Tetris** game, built using **Java** and a **graphical user interface**. It focuses on grid-based logic, falling block mechanics, and responsive controls to provide a smooth gameplay experience while showcasing object-oriented software design.

---

## 🚀 Features

- 🖼️ Graphical interface using **Java Swing** (or JavaFX).
- 📐 Standard **10x20 grid** playfield.
- 🎲 Randomly generated tetromino shapes.
- 🎮 Keyboard input support:
  - ⬅️ Move block left
  - ➡️ Move block right
  - 🔽 Speed up block fall
  - 🔼 Rotate block
- ⏱️ Automated falling via a **timed game loop**
- 💥 Line-clearing mechanic and **score tracking**
- ❌ Game over detection when blocks reach the top

---

## 💡 Java Concepts Demonstrated

- **Object-Oriented Programming**  
  (e.g., `Shape`, `Board`, `GamePanel`, `GameManager`)

- **Event-Driven Programming**  
  (keyboard input handling with `KeyListener`)

- **Timer-based Game Loop**  
  (`javax.swing.Timer` or thread-based scheduling)

- **2D Array Management**  
  (grid representation of the playfield and collision detection)

- **GUI Rendering**  
  (`JPanel` and `Graphics` API for drawing shapes and grid)

---

## 📁 Project Structure

FallingBlocks/ ├── src/ # Source code │ ├── Main.java # Launches the game │ ├── GameWindow.java # Sets up the JFrame (main game window) │ ├── GamePanel.java # Handles drawing and game updates │ ├── Board.java # Manages the grid and line-clearing │ ├── GameTimer.java # Timer that drives the falling mechanic │ ├── Shape.java # Abstract class for all tetrominoes │ ├── TetrominoL.java # L-block implementation │ ├── TetrominoI.java # I-block implementation │ ├── TetrominoO.java # O-block implementation │ ├── InputHandler.java # Handles user keyboard input │ └── utils/ │ └── ColorUtils.java # Optional: helper class for colors or utilities │ ├── resources/ # Optional assets (icons, images) │ └── icon.png # Game window icon │ ├── README.md # Project documentation └── .gitignore # Git ignore rules (e.g., compiled files)
