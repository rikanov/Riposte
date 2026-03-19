Riposte: The Fencing-Inspired Strategy Engine

Riposte is a minimalist, abstract strategy game played on a 5 x 7 grid. It simulates the tactical "push and pull" of a fencing bout through high-speed movement and calculated exchanges.

1. The Core ObjectiveThe goal is to capture on the Dynamic Target Area (DTA) twice. Unlike traditional games where the goal is a fixed location, the DTA in Riposte is dynamic.
   It moves every time a capture occurs, forcing players to constantly recalibrate their positioning.
2. Movement: The "Ricochet" MechanicPieces move in straight lines (Horizontal, Vertical, or Diagonal).The Slide: A piece must slide until it hits an obstacle (the board edge or another piece).
   Fencing Analogy: This represents the explosive Lunge or Fleche—once the momentum starts, the fencer commits to the line of attack.
4. Capturing & Shifting Focus: When a player captures an opponent's piece (by landing an own piece on DTA), that piece is removed from the board. 
   However, the combat doesn't end there: the player who made the capture must then relocate the DTA to the square where the captured piece once stood.
   Player wins when he took two pieces of the oppent.
   Fencing Analogy: This represents a successful Touché. The moment you strike your opponent, the "line of engagement" shifts. The battle resets around a new tactical opening, forcing both fencers to adjust their footwork and distance instantly.
6. The Riposte (Classic Rule): If Player A captures Player B's piece, Player B is often granted an immediate opportunity to counter-attack.
7. Clean Cut (Tournament Rule): A variant where the "Riposte" is disabled. Once a piece is captured, it is removed without an immediate chance for a counter-strike.
   Fencing Analogy: In real fencing, a Parry-Riposte is a defensive action followed by an immediate offensive action. The game’s logic mirrors this "right of way" struggle.

 Mathematical Symmetry & AI Behavior
 
 Our research using the KotlinDemo statistical suite revealed a fascinating "Odd-Even Effect" in the Minimax search tree:Odd-Depth Search (5, 7, 9): Leads to "Decisive/Aggressive" play. 
 The AI sees its own final move but not the opponent's reaction, encouraging bold attacks.Even-Depth Search (8, 10): Leads to "Defensive/Cautious" play. 
 The AI anticipates the opponent's counter, often resulting in intricate "fencing dances" where players maneuver for dozens of turns without committing to a strike.
 
 Technical Specifications
 
 Engine: High-performance C++ IDA* (Iterative Deepening A*) search.Architecture: Multi-threaded Root Parallelism (optimized for upto 24-core desktop and multi-core mobile environments).
 UI: Modern Android Jetpack Compose with a non-blocking Coroutine bridge to the JNI layer.
