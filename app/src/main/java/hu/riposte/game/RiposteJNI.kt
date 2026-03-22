package hu.riposte.game

object RiposteJNI {
    init {
        // A CMakeLists.txt-ben megadott könyvtárnév (pl. "riposte-engine")
        System.loadLibrary("riposte-engine")
    }

    /**
     * @param board A 35 elemű tábla
     * @param player Melyik játékos (1 vagy 2)
     * @param depth Keresési mélység
     * @return [fromIndex, toIndex, newHotSpotIndex]
     */
    external fun getBestMove(board: IntArray, player: Int, depth: Int): IntArray
}