package se.mdh.student.dva232.projectgroup7

enum class GameType(val code: String) {
    TIC_TAC_TOE("ttt"),
    ROCK_PAPER_SCISSORS("rps"),
    DICES("dices"),
    FLIP_A_COIN("coin"),
    BLOW("blow")
    // TODO: if game not present, add here
}