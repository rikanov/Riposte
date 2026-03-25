Riposte: The Fencing-Inspired Strategy Engine

Riposte is a minimalist, abstract strategy game played on a 5 x 7 grid. It simulates the tactical "push and pull" of a fencing bout through high-speed movement and calculated exchanges.

The Core Objective

The goal is to perform a capture on the Touché Point (marked with a ★) twice. Unlike traditional games with a fixed goal, the Touché Point is dynamic: it relocates every time a capture occurs, forcing players to constantly recalibrate their positioning and "line of attack."

Movement: The "Ricochet" Mechanic

Pieces move in straight lines (Horizontal, Vertical, or Diagonal).
The Slide: A piece must slide until it hits an obstacle (the board edge or another piece).
Fencing Analogy: This represents the explosive Lunge or Fleche—once the momentum starts, the fencer commits to the line of attack.

Capturing & Shifting Focus

When a player lands on the Touché Point, the opponent's piece is captured and removed. However, the combat doesn't end there: the player who made the capture must then relocate the Touché Point to the square where the captured piece once stood. A player wins by capturing two of the opponent's pieces.
Fencing Analogy: This represents a successful Touché. The moment you strike, the "line of engagement" shifts, forcing both fencers to adjust their footwork and distance instantly.

Rules & Variations

The Riposte (Classic Rule): If Player A captures Player B's piece, Player B is granted an immediate opportunity to counter-attack.

Clean Cut (Tournament Rule): A variant where the "Riposte" is disabled. Once a piece is captured, it is removed without an immediate chance for a counter-strike.

Fencing Analogy: In real fencing, a Parry-Riposte is a defensive action followed by an immediate offensive action. The game’s logic mirrors this "right of way" struggle.

Mathematical Symmetry & AI Behavior
Our research using the KotlinDemo statistical suite revealed a fascinating "Odd-Even Effect" in the Minimax search tree:

Odd-Depth Search (5, 7, 9): Leads to "Decisive/Aggressive" play. The AI sees its own final move but not the opponent's reaction, encouraging bold attacks.

Even-Depth Search (6, 8, 10): Leads to "Defensive/Cautious" play. The AI anticipates the opponent's counter, often resulting in intricate "fencing dances" where players maneuver for dozens of turns without committing to a strike.
