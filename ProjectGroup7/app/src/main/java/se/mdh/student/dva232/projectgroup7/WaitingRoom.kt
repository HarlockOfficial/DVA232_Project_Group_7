package se.mdh.student.dva232.projectgroup7

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class WaitingRoom : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.waiting_room)
        val gameCode: GameType = GameType.valueOf(intent.getStringExtra("GAME_CODE")!!)
        lateinit var out: String
        val gameClass = if(gameCode == GameType.ROCK_PAPER_SCISSORS){
            out = "Rock Paper Scissors"
            RockPaperScissors::class.java
        }else if(gameCode == GameType.TIC_TAC_TOE){
            out = "Tic Tac Toe"
            TicTacToe::class.java
        }else if(gameCode == GameType.DICES){
            out = "Tic Tac Toe"
            Dices::class.java
        }else if(gameCode == GameType.FLIP_A_COIN){
            out = "Flip A Coin"
            Coin::class.java
        }else{  //TODO: if game not present add "else if" here
            out = "Unrecognized Game"
            null
        }
        if(out == "Unrecognized Game"){
            Toast.makeText(baseContext, getString(R.string.waiting_room_game_error), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        val label = findViewById<TextView>(R.id.waiting_room_label)
        label.text = getString(R.string.waiting_room, out)
        GlobalScope.launch(Dispatchers.IO) {
            val data: Data = if(gameCode == GameType.ROCK_PAPER_SCISSORS){
                RockPaperScissorsData("")
            }else if(gameCode == GameType.TIC_TAC_TOE){
                Data()    // TODO: when created add here the correct data Implementation
            }else if(gameCode == GameType.DICES){
                Data()    // TODO: when created add here the correct data Implementation
            }else if(gameCode == GameType.FLIP_A_COIN){
                Data()    // TODO: when created add here the correct data Implementation
            }else{
                Data()    // TODO: when created add here the correct data Implementation
            }
            var ret: JSONObject = CommunicationLayer.addPlayerToMultiplayerQueue(data)
            val uuid: String = CommunicationLayer.uuid
            if(ret["response"] == "in_queue"){
                do{
                    delay(10)
                    ret = CommunicationLayer.checkMultiplayerQueue(data)
                }while (ret["response"]=="in_queue");
            }
            ret = JSONObject(ret["response"] as String)

            val intent = Intent(this@WaitingRoom, gameClass)
            intent.putExtra("isStarting", ret["starting_player"]==uuid)
            intent.putExtra("field", ret["field"].toString())
            startActivity(intent)
        }
    }
}