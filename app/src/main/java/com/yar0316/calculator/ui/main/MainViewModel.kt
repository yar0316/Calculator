package com.yar0316.calculator.ui.main

import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.jetbrains.anko.toast
import java.lang.RuntimeException

class MainViewModel : ViewModel() {

    // 現在入力されている数値
    val currentInput = MutableLiveData<String>().apply {
        value = getInitialValue()
    }

    // 計算結果
    private var currentResult: Double = 0.0

    private var operatorJustBefore: String = ""

    // 次に数字キーが押されたとき、画面の数値を削除するかどうか
    private var currentClearFlag = false

    // 押されたボタンの種類によって処理を振り分け
    fun onButtonPlessed(button: View) {
        try {
            val text = (button as Button).text.toString()
            when (text) {
                DELETE, in NUMBERS -> editNumText(text)
                in OPERATORS -> calculate(text)
                CLEAR -> clear()
                SIGN -> reverseSign()
                else -> throw RuntimeException("received")
            }
        }catch (e: Exception){
            e.printStackTrace()
            button.context.toast("処理に失敗しました")
            Log.d("exception", "例外が発生しました。実行ログを確認してください")
        }
    }

    /**
     * 符号反転
     */
    private fun reverseSign() {
        currentInput.value = ((currentInput.value?.toDouble() ?: 0.0) * -1.0).toString()
    }

    private fun calculate(operator: String) {
        val currentNum = currentInput.value?.toDouble() ?: 0.0
        // 演算子の判定
        // 計算
        currentResult = when (operatorJustBefore) {
            OPERATORS[0] -> currentResult + currentNum
            OPERATORS[1] -> currentResult - currentNum
            OPERATORS[2] -> currentResult * currentNum
            OPERATORS[3] -> if (currentNum != 0.0) currentResult / currentNum else 0.0
            OPERATORS[4] -> currentResult % currentNum
            else -> currentNum
        }

        // 次のじゅんび
        currentInput.value = currentResult.toString()
        operatorJustBefore = operator
        currentClearFlag = true
    }

    // 画面に表示する文字列の編集
    private fun editNumText(sign: String) {

        if (currentClearFlag || currentInput.value == "0") {
            currentInput.value = ""
            currentClearFlag = false
        }

        val currentStr = currentInput.value

        currentInput.value = when (sign) {
            DELETE -> deleteChar(currentStr ?: getInitialValue())
            else -> currentStr?.plus(sign)
        }
    }

    // 一文字削除
    private fun deleteChar(text: String): String? {
        return when (text.length) {
            1 ->  getInitialValue()
            else ->  text.substring(0, text.length - 1)
        }
    }

    // クリア
    private fun clear() {
        currentInput.value = getInitialValue()
        currentResult = 0.0
        currentClearFlag = false
    }

    private fun getInitialValue(): String {
        return NUMBERS[0]
    }

    companion object {
        private val NUMBERS = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")
        private val OPERATORS = arrayOf("+", "-", "*", "/", "%", "=")
        private const val CLEAR = "C"
        private const val SIGN = "+/-"
        private const val DELETE = "Delete"
    }
}
