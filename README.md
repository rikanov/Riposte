## La Riposte: The Fencing-Inspired Strategic Board Game 

La Riposte is a minimalist, abstract strategy game played on a 5x7 grid. It simulates the tactical "push and pull" of a fencing bout through high-speed movement, calculated exchanges, and strategic positioning.

#### 🎯 The Core Objective 

The goal is to successfully perform a capture on the Touché Point (marked with a ★) twice.

Unlike traditional board games with a fixed goal (like a King in Chess), the Touché Point in Riposte is dynamic. It relocates every time a capture occurs, forcing players to constantly recalibrate their positioning and find a new "line of attack."

#### ⚡ Movement: The "Ricochet" Mechanic 

Pieces move across the board with explosive speed, governed by a sliding mechanic:

Trajectory: Pieces can move in any straight line (Horizontal, Vertical, or Diagonal).

The Slide: Once a piece moves, it must slide continuously until it hits an obstacle (either the edge of the board or another piece).

**Fencing Analogy:** This represents the explosive Lunge or Fleche. Once the physical momentum starts, the fencer commits entirely to the line of attack.

#### 🤺 Capturing & Shifting Focus 

When a player’s piece lands exactly on the Touché Point, the opponent's piece on that square is captured and removed from the board.

However, the combat doesn't end there:

The player who made the capture must immediately relocate the Touché Point to the square where their captured piece originally stood.

A player wins the bout by capturing two of the opponent's pieces.

**Fencing Analogy:** This represents a successful Touché. The moment you strike, the "line of engagement" shifts instantly. Both fencers must immediately adjust their footwork and distance to adapt to the new reality of the piste.

#### 📜 Rules & Variations 

The Riposte (Classic Rule): If Player A captures Player B's piece, Player B is granted an immediate, single-turn opportunity to counter-attack.

Clean Cut (Tournament Rule): A strict variant where the "Riposte" is disabled. Once a piece is captured, it is removed without an immediate chance for a counter-strike.

**Fencing Analogy:** In real fencing, a Parry-Riposte is a defensive action followed by an immediate offensive strike. The game’s logic perfectly mirrors this fierce struggle for the "right of way."

#### 🏆 The Hall of Legends: Personalities & Playstyles 

La Riposte does not rely on simple "Easy, Medium, or Hard" difficulty sliders. Instead, the single-player Tournament Mode introduces the Hall of Legends—a curated roster of AI opponents spanning historical fencing masters, literary duelists, and modern champions (including Paralympic legends).

Facing a new opponent means facing a completely different heuristic mindset. The AI engine dynamically adjusts its tactical weights (Aggression, Defense, and Mobility) to reflect the real-world or fictional persona of the fencer.

## 🛠️ Development Methodology & AI Collaboration 

The development of Riposte follows a Human-Centric AI-Assisted model.

Original Core: The game concept, ruleset, and the high-performance C++ Game Engine (including the bitboard logic and JNI bridge) are 100% human-designed and hand-coded by the author.

AI Co-pilot: Google Gemini (including 3.1 Pro and Ultra models) serves as a high-level project collaborator. Its role includes:

**Refactoring and optimizing** complex Kotlin UI components.

**Assisting in the implementation** of the "Hall of Legends" personality-based heuristics.

**Generating edge-case test scenarios** and performing deep code audits for memory safety.

Every line of AI-suggested code is reviewed, tested, and manually integrated. The AI acts as an advanced pair-programmer, while the overall architecture and creative direction remain firmly in human hands.

## Artistic Direction & AI Collaboration

The visual and auditory landscape of *Riposte* is the result of a deliberate collaboration between human curation and Generative AI.

- **Assets:** All background art, UI elements, and musical tracks were initially generated using **Google Gemini** and specialized AI models (Lyria for music, Nano Banana for imagery).

- **Human Touch:** Every asset has been individually reviewed, manually edited, or digitally post-processed to ensure it meets the game’s aesthetic standards and technical requirements.

- **Attribution & Watermarks:** In the spirit of transparency and "Origin Marking," certain AI-generated signatures—including subtle sonic watermarks and visual cues—have been **intentionally preserved**. These marks serve as a digital "hallmark," acknowledging the role of AI in the creative process rather than obscuring it.

This project stands as a testament to how AI can act as a "force multiplier" for solo developers, providing high-fidelity sensory experiences while maintaining a clear, human-led creative vision.

