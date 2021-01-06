package se.mdh.student.dva232.projectgroup7

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class WaitingRoom : AppCompatActivity(), ActivityInterface {
    private lateinit var gameCode: GameType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("WaitingRoom", "onCreate started\n")
        setContentView(R.layout.waiting_room)
        gameCode = GameType.valueOf(intent.getStringExtra("GAME_CODE")!!)
        lateinit var out: String
        val gameClass = if (gameCode == GameType.ROCK_PAPER_SCISSORS) {
            out = "Rock Paper Scissors"
            RockPaperScissors::class.java
        } else if (gameCode == GameType.TIC_TAC_TOE) {
            out = "Tic Tac Toe"
            TicTacToe::class.java
        } else if (gameCode == GameType.DICES) {
            out = "Dices"
            DicesActivity::class.java
        } else if (gameCode == GameType.BLOW) {
            out = "Blow"
            BlowActivity::class.java
        } else {  //TODO: if game not present add "else if" here
            out = "Unrecognized Game"
            null
        }
        if (out == "Unrecognized Game") {
            Toast.makeText(
                baseContext,
                getString(R.string.waiting_room_game_error),
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        val label = findViewById<TextView>(R.id.waiting_room_label)
        label.text = getString(R.string.waiting_room, out)

        val data = if (gameCode == GameType.ROCK_PAPER_SCISSORS) {
            RockPaperScissorsData("")
        } else if (gameCode == GameType.TIC_TAC_TOE) {
            object : Data {
                override val game: GameType
                    get() = GameType.TIC_TAC_TOE

                override fun moveToCsv(): String {
                    return ""
                }
            }
        } else if (gameCode == GameType.DICES) {
            object : Data {
                override val game: GameType
                    get() = GameType.DICES

                override fun moveToCsv(): String {
                    return ""
                }
            }    // TODO: when created add here the correct data Implementation
        }  else if (gameCode == GameType.BLOW) {
            object : Data {
                override val game: GameType
                    get() = GameType.BLOW

                override fun moveToCsv(): String {
                    return ""
                }
            }     // TODO: when created add here the correct data Implementation

        } else {
            object : Data {
                override val game: GameType
                    get() = GameType.FLIP_A_COIN //??????

                override fun moveToCsv(): String {
                    return ""
                }
            }     // TODO: when created add here the correct data Implementation
        }
        GlobalScope.launch(Dispatchers.IO) {
            var ret: JSONObject = CommunicationLayer.addPlayerToMultiplayerQueue(data)
            Pinger.isPlayerAdded = true
            Log.e("WaitingRoom Coroutine", "Player added to queue")
            val uuid: String = CommunicationLayer.uuid

            if (ret["response"] == "in_queue") {
                do {
                    delay(10)
                    ret = CommunicationLayer.checkMultiplayerQueue(data)
                } while (ret["response"] == "in_queue")
            }
            ret = JSONObject(ret["response"] as String)

            val intent = Intent(this@WaitingRoom, gameClass)
            intent.putExtra("isStarting", ret["starting_player"] == uuid)
            intent.putExtra("field", ret["field"].toString())
            startActivity(intent)
        }
        findViewById<Button>(R.id.leave_queue_btn).setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                CommunicationLayer.delPlayerFromMultiplayerQueue(data)
                onBackPressed()
            }
        }
    }

    //function needed to instantiate pinger, not really necessary, the only "response" possible is "ok" (in the waiting room)
    override fun quit() {
        throw NotImplementedError("Impossible that quit gets called in waiting room")
    }

    override fun onPause() {
        Log.e("WatingRoom", "onPause started\n")
        Pinger.stop()
        super.onPause()
    }

    override fun onResume() {
        Log.e("WaitingRoom", "onResume started\n")
        Pinger.changeContext(this, gameCode)
        super.onResume()
    }

    override fun onBackPressed() {
        Pinger.stop()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}