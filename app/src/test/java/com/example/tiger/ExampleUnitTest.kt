package com.example.tiger

import android.util.Log
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {

        val string =
            "M853.33,384v256h-256v298.67h85.33v-213.33h170.67v213.33h85.33V384z"

        val cmdM = 0
        val cmdm = 1
        val cmdL = 2
        val cmdl = 3
        val cmdV = 4
        val cmdv = 5
        val cmdH = 6
        val cmdh = 7

        var nextMethod = -1

        //上一次的坐标点
        var lastX = 0f
        var lastY = 0f

        //存储x，y相关字符串
        var lastXstr = ""
        var lastYstr = ""

        //是否是X坐标
        var isX = true


        val s = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '.')

        /**
         * 处理XY的坐标
         */
        fun processXY() {
            lastX = if (lastXstr.isEmpty()) {
                0f
            } else {
                lastXstr.toFloat()
            }

            lastY = if (lastYstr.isEmpty()) {
                0f
            } else {
                lastYstr.toFloat()
            }

            lastXstr = ""
            lastYstr = ""

            println("上次坐标：$lastX $lastY")
        }

        /**
         * 处理方法
         */
        fun processMethod() {
            println("processMethod:  $nextMethod $lastX $lastY")
        }

        string.forEach {

            /**
             * , 44
             * - 45
             * . 46
             *   32
             */


            //表示如果是数字
            if (it in s) {
                if (isX) {
                    lastXstr += it
                } else {
                    lastYstr += it
                }
                return@forEach
            }

            when (it) {
                'M' -> {
                    isX = true

                    processXY()
                    processMethod()
                    nextMethod = cmdM
                }
                'm' -> {
                    isX = true

                    processXY()
                    processMethod()
                    nextMethod = cmdm
                }

                'L' -> {
                    isX = true

                    processXY()
                    processMethod()
                    nextMethod = cmdL
                }
                'l' -> {
                    processMethod()
                    nextMethod = cmdl
                    isX = true

                    processXY()
                }

                'V' -> {
                    isX = true
                    processXY()
                    processMethod()
                    nextMethod = cmdV
                }
                'v' -> {
                    isX = true
                    processXY()
                    processMethod()
                    nextMethod = cmdv
                }


                'H' -> {
                    isX = true
                    processXY()
                    processMethod()
                    nextMethod = cmdH
                }
                'h' -> {
                    isX = true
                    processXY()
                    processMethod()
                    nextMethod = cmdh
                }

                'z' -> {
                    isX = true
                    processXY()
                    processMethod()
                }

                ' ' -> {
                    isX = true

                    processXY()
                    processMethod()
                }

                //表示是x y坐标分割的逗号
                ',' -> {
                    isX = false
                }
            }
        }


    }
}