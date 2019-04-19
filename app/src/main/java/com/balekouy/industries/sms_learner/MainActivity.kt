package com.balekouy.industries.sms_learner

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import com.balekouy.industries.sms_learner.AppDatabase.Companion.databaseExist
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mDB: AppDatabase? = null
    private val mDbWorkerThread = DbWorkerThread("dbWorkerThread")
    private var mUIHandler = Handler()
    private val hashmap = HashMap<String, Float>()
    private var hints = listOf("","","")
        set(value) {
            field = value
            setButtons()
        }

    private var correctWords = ArrayList<String>()
    private var context: String = PROPOSITION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runOnUiThread { setContentView(R.layout.activity_main) }
        initProgram()
    }

    private fun initUI() {
        user_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                val task = Runnable { handleNewLetter(text.toString()) }
                mDbWorkerThread.postTask(task)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        word_1.setOnClickListener { clickedOnWord1() }

        word_2.setOnClickListener { clickedOnWord2() }

        word_3.setOnClickListener { clickedOnWord3() }
    }

    private fun handleNewLetter(text: String) {
        setEmotion(text)

        val lenghtText = text.length
        if (lenghtText > 0) {
            if (text.last().isWhitespace()) {
                val words = text.substring(0, text.lastIndex).split(" ")
                val lastWord = words.last()
                val beforeLastWord: String? = words.getOrNull(words.size - 2)
                context = PROPOSITION
                hints = getTopHint(beforeLastWord?.toLowerCase(), lastWord.toLowerCase())
                if (hints[0] == "")
                    hints = getTopHint(lastWord.toLowerCase())
                if (hints[0] == "")
                    hints = getTopHint()
            } else {
                val word = text.substring(0, text.lastIndex + 1).split(" ").last()
                val task = Runnable {
                    val worddb = mDB?.dataDao()?.getWord("$word%")
                    hints =
                        if (!worddb.isNullOrEmpty()) {
                            context = COMPLETION
                            worddb.map { v -> v.word1 }
                        } else {
                            context = CORRECTION
                            getNearestWord(word)
                        }
                }
                mDbWorkerThread.postTask(task)
            }
        } else {
            context = PROPOSITION
            hints = getTopHint(lastWord = ".")
        }
    }

    private fun setEmotion(text: String) {
        val emotion = calculateEmotion(text)
        val format = String.format("%.2f", emotion)
        when {
            emotion < -0.5 -> emotion_tv.text = "🤬 $format"
            emotion > 0.5 -> emotion_tv.text = "😊 $format"
            emotion < -0.2 -> emotion_tv.text = "😡 $format"
            emotion > 0.2 -> emotion_tv.text = "🙂 $format"
            else -> emotion_tv.text = "😐 $format"
        }
    }

    private fun calculateEmotion(text: String): Float {
        var emotionCounter = 0F
        val list = text.split(" ").filter { it != "" }
        list.forEach {
            val text = it.replace(".", "")
            emotionCounter += getEmotion(text)
        }
        return emotionCounter / list.size
    }

    private fun getEmotion(word: String): Float {
        return hashmap[word.toLowerCase()] ?: 0F
    }

    private fun getNearestWord(word: String): List<String> {
        var arr = listOf(
            mutableMapOf("value" to "", "level" to 100),
            mutableMapOf("value" to "", "level" to 100),
            mutableMapOf("value" to "", "level" to 100)
        )

        fun rearangeCollection(word: String, level: Int) {
            var temp = mutableMapOf("value" to "", "level" to 100)

            arr.forEach {
                if (temp["level"] == 100) {
                    if (it["level"] as Int >= level) {
                        temp["level"] = it["level"] as Int
                        temp["value"] = it["value"] as String
                        it["level"] = level
                        it["value"] = word
                    }
                } else {
                    var temptemp = mapOf("value" to temp["value"], "level" to temp["level"])
                    it["level"] = temp["level"] as Int
                    it["value"] = temp["value"] as String
                    temp["level"] = temptemp["level"] as Int
                    temp["value"] = temptemp["value"] as String
                }
            }
        }
        correctWords.forEach {
            rearangeCollection(it, levenshtein(word, it))
        }

        return arr.map { it["value"].toString() }
    }


    private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        var cost = Array(lhsLength) { it }
        var newCost = Array(lhsLength) { 0 }

        for (i in 1 until rhsLength) {
            newCost[0] = i

            for (j in 1 until lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = Math.min(Math.min(costInsert, costDelete), costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength - 1]
    }

    private fun getTopHint(beforeLastWord: String? = null, lastWord: String? = null): List<String> {
        return when {
            beforeLastWord != null && lastWord != null -> {
                Log.d("BEFORE HINT", " word1 : $beforeLastWord, word2 : $lastWord")
                mDB?.dataDao()?.getHint(beforeLastWord, lastWord)
                    ?.map { x -> x.word3 }
                    .orNothing()
            }
            lastWord != null -> {
                Log.d("BEFORE HINT", "word1 : $lastWord")
                val test = mDB?.dataDao()?.getHint(lastWord)

                Log.d("DURING HINT", "test : $test")
                test?.map { x -> x.word2 }.orNothing()
            }
            else -> {
                Log.d("BEFORE HINT", "NO ARGS")
                mDB?.dataDao()?.getHint()
                    ?.map { x -> x.word1 }
                    .orNothing()
            }
        }
    }

    private fun setButtons() {
        word_1.text = hints[0]
        word_2.text = if (hints.size > 1) hints[1] else ""
        word_3.text = if (hints.size > 2) hints[2] else ""
    }

    private fun clickedOnWord1() {
        updateText(word_1)
    }

    private fun clickedOnWord2() {
        updateText(word_2)
    }

    private fun clickedOnWord3() {
        updateText(word_3)
    }

    private fun updateText(but: Button) {
        val text :String = user_input.text.toString()

        if (but.text.isNotEmpty()) {
            val newText: String
            Log.i("TABC", context)
            newText =
                if (context == PROPOSITION)
                    "${user_input.text}${but.text} "
                else {
                    var sent = user_input.text.split(" ")
                    Log.i("TAB1", sent.toString())
                    sent = sent.dropLast(1)
                    Log.i("TAB2", sent.toString())
                    "${sent.joinToString(separator = " ")}${if (sent.isNotEmpty()) " " else ""}${but.text} "
                }
            Log.i("TAB", newText)

            user_input.setText(newText)

            user_input.setSelection(user_input.text.length)
        }
    }


    private fun initProgram() {
        mDbWorkerThread.start()
        mDB = AppDatabase.getDatabase(this)
        if (!databaseExist(applicationContext)) readCSVFirstTime()
        initEmotionSet()
        initCorrectionSet()
        initUI()
    }

    private fun initEmotionSet() {
        var fileReader: BufferedReader? = null
        try {
            var line: String?

            fileReader = BufferedReader(InputStreamReader(resources.openRawResource(R.raw.emotions)))

            // Read CSV header
            fileReader.readLine()

            // Read the file line by line starting from the second line
            line = fileReader.readLine()
            while (line != null) {
                val tokens = line.split(",")
                if (tokens.isNotEmpty()) {
                    hashmap[tokens[0]] = tokens[1].toFloat()
                }
                line = fileReader.readLine()
            }

        } catch (e: Exception) {
            Log.e("File error", "Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader?.close()
            } catch (e: IOException) {
                Log.e("File error", "Closing fileReader Error!")
                e.printStackTrace()
            }
        }
    }

    private fun initCorrectionSet() {
        val task = Runnable { correctWords?.addAll(mDB?.dataDao()?.getMVP()?.map { v -> v.word1 }.orEmpty()) }
        mDbWorkerThread.postTask(task)
    }

    private fun readCSVFirstTime() {
        read0(resources.openRawResource(R.raw.gram0))
        read1(resources.openRawResource(R.raw.gram1))
        read2(resources.openRawResource(R.raw.gram2))
    }

    private fun read0(resourceFile: InputStream) {
        generateDaraFromCsv(resourceFile, 0)
    }

    private fun read1(resourceFile: InputStream) {
        generateDaraFromCsv(resourceFile, 1)
    }

    private fun read2(resourceFile: InputStream) {
        generateDaraFromCsv(resourceFile, 2)
    }

    private fun generateDaraFromCsv(resourceFile: InputStream, gram:Int){
        var fileReader: BufferedReader? = null
        try {
            var line: String?

            fileReader = BufferedReader(InputStreamReader(resourceFile))

            // Read CSV header
            fileReader.readLine()

            // Read the file line by line starting from the second line
            line = fileReader.readLine()
            while (line != null) {
                val tokens = line.split(";")
                if (tokens.isNotEmpty()) {
                    when (gram){
                        0 -> mDbWorkerThread.postTask(Runnable { mDB?.dataDao()?.insert(DataZero(null, tokens[0], Integer.parseInt(tokens[1])))})
                        1 -> mDbWorkerThread.postTask(Runnable { mDB?.dataDao()?.insert(DataUn(null, tokens[0], tokens[1], Integer.parseInt(tokens[2])))})
                        else -> mDbWorkerThread.postTask(Runnable { mDB?.dataDao()?.insert(Data(null, tokens[0], tokens[1], tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4])))})
                    }
                }
                line = fileReader.readLine()
            }

        } catch (e: Exception) {
            Log.e("File error", "Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader?.close()
            } catch (e: IOException) {
                Log.e("File error", "Closing fileReader Error!")
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val CORRECTION = "ERASE"
        const val COMPLETION = "ERASE"
        const val PROPOSITION = "CONCAT"
    }
}

private fun List<kotlin.String>?.orNothing(): List<kotlin.String> {
    Log.d("NEW HINT", this.toString())
    return if (this != null) {
        when (size) {
            1 -> listOf(get(0), "", "")
            2 -> listOf(get(0), get(1), "")
            3 -> listOf(get(0), get(1), get(2))
            else -> listOf("", "", "")
        }
    } else listOf("", "", "")
}
