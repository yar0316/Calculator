package com.yar0316.calculator.ui.main

import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.jetbrains.anko.toast
import java.lang.RuntimeException

/**
 * 入力されたデータの保持と計算を行う
 * 文字列ほとんど生で書いてる。companion objectに定数定義したほうがいいかも。もしくはutilつくって定数用ファイル
 * HACK: 現在計算結果が整数でも小数点以下の表示が出てたりと表示が不安定なので、そのあたりを修正したい
 *
 * @author yar0316
 */

class MainViewModel : ViewModel() {

    // 現在入力されている数値
    val currentInput = MutableLiveData<String>().apply {
        value = getInitialValue()
    }

    // 計算結果
    private var currentResult: Double = 0.0

    // 直前に入力された演算子
    private var operatorJustBefore: String = ""

    // 次に数字キーが押されたとき、画面の数値を削除するかどうか
    private var currentClearFlag = false

    /**
     * 押されたボタンの種類によって処理を振り分け
     * すべてのボタンが、クリック時にこのメソッドを呼ぶように設定されてる
     * ボタンごと渡すようにしてるので(実際はテキストの渡し方しらないだけ)エラー時にらくちんトースト
     *
     * @param button クリックされたボタン
     */
    fun onButtonPressed(button: View) {
        try {
            // なんか忠告されてるけどそれやると読みづらくならんか？
            val text = (button as Button).text.toString()

            // すべてのボタンが押されたときにこのメソッド呼ぶので、受け取ったテキストで分岐
            when (text) {
                DELETE, in NUMBERS -> editNumText(text)
                in OPERATORS -> calculate(text)
                CLEAR -> clear()
                SIGN -> reverseSign()
                else -> throw RuntimeException("received")
            }
        }catch (e: Exception){
            e.printStackTrace()
            button.context.toast(e.message ?: "処理に失敗しました")
            Log.d("exception", "例外が発生しました。実行ログを確認してください")
        }
    }

    /**
     * 符号反転。+/-キー
     * とりあえず-1かけときゃ何とかなるよね
     */
    private fun reverseSign() {
        currentInput.value = ((currentInput.value?.toDouble() ?: 0.0) * -1.0).toString()
    }

    /**
     * ここで計算。演算子キー
     * @param operator 入力された演算子
     */
    private fun calculate(operator: String) {

        // 表示してるデータを計算用に変換
        val currentNum = currentInput.value?.toDouble() ?: 0.0

        // 演算子の判定と計算
        // 引数は今回クリックされたボタンの演算子なので、判定には直前の演算子を利用
        // ハッシュにしたほうが演算子わかりやすくていいかな？
        currentResult = when (operatorJustBefore) {
            OPERATORS[0] -> currentResult + currentNum
            OPERATORS[1] -> currentResult - currentNum
            OPERATORS[2] -> currentResult * currentNum
            OPERATORS[3] -> if (currentNum != 0.0) currentResult / currentNum else throw ArithmeticException("0で割ることはできません")
            OPERATORS[4] -> currentResult % currentNum
            else -> currentNum
        }

        // 次のじゅんび
        currentInput.value = currentResult.toString()
        operatorJustBefore = operator
        currentClearFlag = true
    }

    /**
     * 表示用の数値文字列を編集する。数値、小数点キー
     * 正直削除は分けようかと思ったけど被るコードも割とあるのであえてまとめた
     *
     * @param sign 入力された数字もしくは記号
     */
    private fun editNumText(sign: String) {

        // 先頭に0が表示されたり足し算したのに末尾に数字が追加されてったりするのを防ぐ
        if (currentClearFlag || currentInput.value == "0") {
            currentInput.value = ""
            currentClearFlag = false
        }

        // 先にやると上の処理の意味なくなる
        val currentStr = currentInput.value

        currentInput.value = when (sign) {
            DELETE -> deleteChar(currentStr)
            else -> currentStr?.plus(sign)
        }
    }

    /**
     * 一文字削除の処理。Delキー
     * @param text 現在表示されている文字列
     */
    private fun deleteChar(text: String?): String? {
        text ?: return getInitialValue() // nullが飛んでくることはまずないはずだけどLiveData<T>.valueがnullableで返ってくるので。
        return when (text.length) {
            1 ->  getInitialValue()
            else ->  text.substring(0, text.length - 1)
        }
    }


    /**
     * リセット処理。画面ではCキー押したときの処理だけど実際はACと同じことしてる
     */
    private fun clear() {
        currentInput.value = getInitialValue()
        currentResult = 0.0
        currentClearFlag = false
    }

    /**
     * 画面に表示する初期値を返す。0。
     */
    private fun getInitialValue(): String {
        return NUMBERS[0]
    }

    // これViewの表記変わるとこっちも変えなきゃいけないからあんまよくない気がするようなきがしないでもないような
    companion object {
        private val NUMBERS = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")
        private val OPERATORS = arrayOf("+", "-", "*", "/", "%", "=")
        private const val CLEAR = "C"
        private const val SIGN = "+/-"
        private const val DELETE = "del"
    }
}
