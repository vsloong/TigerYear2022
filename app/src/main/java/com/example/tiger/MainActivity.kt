package com.example.tiger

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiger.ui.theme.Purple700
import com.example.tiger.ui.theme.TigerTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pathList = arrayListOf<Path>()

        tigerSvgPathList.forEach {
            val path = Path()
            processPath(string = it, path = path)
            path.close()
            pathList.add(path)
        }

        val svgList = mutableStateListOf(Path())

        setContent {


            LaunchedEffect(key1 = true, block = {

                delay(1000)
                val size = pathList.size

                for (index in 0..size) {
                    delay(100)

                    svgList.clear()
                    svgList.addAll(pathList.subList(0, index))
                }
            })

            TigerTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = Purple700) {
                    Greeting(svgList)
                }
            }
        }
    }
}

@Composable
fun Greeting(svgList: List<Path>) {

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.tiger_bg),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        val color = Color(0xFF4b0500)
        Canvas(
            modifier = Modifier
                .padding(start = 50.dp)
                .size(400.dp)
                .scale(0.7f),
            onDraw = {
                svgList.forEach {
                    drawPath(
                        path = it,
                        color = color,
                    )
                }

            })
    }


}


fun processPath(string: String, path: Path) {

    val cmdM = "Move"
    val cmdm = "move"
    val cmdL = "Line"
    val cmdl = "line"
    val cmdV = "Vertical"
    val cmdv = "vertical"
    val cmdH = "Horizontal"
    val cmdh = "horizontal"
    val cmdz = "close"

    var nextMethod = ""

    //上一次的坐标点
    var lastX = 0f
    var lastY = 0f

    //存储x，y相关字符串
    var lastXstr = ""
    var lastYstr = ""

    //是否是X坐标
    var isX = true


    val s = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '.')


    val pathMeasure = android.graphics.PathMeasure()

    /**
     * 处理方法
     */
    fun processMethod() {
        when (nextMethod) {

            cmdM -> {
                path.moveTo(lastX, lastY)
            }

            cmdm -> {
                path.relativeMoveTo(lastX, lastY)
            }

            cmdL -> {
                path.lineTo(lastX, lastY)
            }

            cmdl -> {
                path.relativeLineTo(lastX, lastY)
            }

            cmdH -> {
                //注意此时y坐标应该是当前位置的y坐标
                pathMeasure.setPath(path.asAndroidPath(), false)

                val position = FloatArray(2)
                val tan = FloatArray(2)

                pathMeasure.getPosTan(pathMeasure.length, position, tan)
                path.lineTo(lastX, position[1])
            }

            cmdh -> {
                path.relativeLineTo(lastX, 0f)
            }

            cmdV -> {
                //注意此时x坐标应该是当前位置的x坐标
                pathMeasure.setPath(path.asAndroidPath(), false)

                val position = FloatArray(2)
                val tan = FloatArray(2)

                pathMeasure.getPosTan(pathMeasure.length, position, tan)
                path.lineTo(position[0], lastX)
            }

            cmdv -> {
                path.relativeLineTo(0f, lastX)
            }

            cmdz -> {
                path.close()
            }
        }

    }


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

        processMethod()
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
                nextMethod = cmdM
            }
            'm' -> {
                isX = true

                processXY()
                nextMethod = cmdm
            }

            'L' -> {
                isX = true

                processXY()
                nextMethod = cmdL
            }
            'l' -> {
                isX = true

                processXY()
                nextMethod = cmdl
            }

            'V' -> {
                isX = true
                processXY()
                nextMethod = cmdV
            }
            'v' -> {
                isX = true
                processXY()
                nextMethod = cmdv
            }


            'H' -> {
                isX = true
                processXY()
                nextMethod = cmdH
            }
            'h' -> {
                isX = true
                processXY()
                nextMethod = cmdh
            }

            'z' -> {
                isX = true
                processXY()
                nextMethod = cmdz
            }

            ' ' -> {
                isX = true

                processXY()
            }

            //表示是x y坐标分割的逗号
            ',' -> {
                isX = false
            }
        }
    }


}

val tigerSvgPathList = arrayListOf(

    //左耳朵
    "M493.18,289.92l-12.44,-9.07 -14.76,-9.39 -15.22,-6.39 -10.07,-2h-9.74l-12.41,1.33 -8.07,4.04 -4.7,4.35 -5.37,10.74 -2.02,7.72 -0.33,8.39v10.07l2.46,12.07 3.7,11.74 5.02,8.7 8.07,8.04 8.39,7.72 11.41,6.72 -1.35,11.07 -5.7,9.74 -10.74,7.37 -13.04,4.7 11.74,2.7 9.39,-2.02 4.7,-2.35 5.04,-4.35 5.72,-5.37 4.02,-7.04 5.02,-16.78 -9.04,-5.7 -11.76,-6.39 -9.39,-7.04 -8.39,-9.04 -4.04,-7.72 -2.35,-8.7 0.67,-29.85 3.02,-6.04 4.7,-3.02 9.74,-1 8.07,1.35 10.39,4.02 10.74,7.37 6.39,9.74 7.72,10.74 5.37,4.35 -4.35,4.72 -4.7,7.04 -7.39,24.15 10.41,-16.78 9.74,-11.07 10.07,-9.74 9.74,-7.04 7.72,-5.37 -11.74,-11.39z",

    //头部皱纹
    "M474.03,340.9l20.83,-16.76 25.18,-16.11 22.15,-8.39 13.76,-1 14.09,1 20.15,6.37 10.41,-2 11.07,-5.04 13.44,-2.67 13.04,-2.02h14.78l13.7,4.02 13.78,9.74 5.02,10.74 12.76,4.7 5.37,4.02 3.35,5.7 1.35,11.74 -11.74,-13.41 -10.33,-4.7 -13.04,-1.35 -0.67,-9.04 -5.09,-7.7 -10.39,-4.7 -13.04,-1.35 -17.39,2.02 -15.46,7.72 -16.54,9.35 -10.74,-2.02 -12.44,-4.02 -16.11,-0.67 -22.15,-0.67 -20.13,7.04 -12.44,5.37 -12.74,5.04 -13.78,9.04z",

    //右耳朵
    "M679.48,280.18l14.78,-6.72 18.46,-8.04 18.81,-3.02 9.04,-4.35 7.74,-1.35 8.39,-0.33 8.7,1.67 4.7,2.35 4.35,5.04 4.02,11.74 1.67,7.04v8.39l0.67,10.39 -2.35,12.41 -3.7,12.09 -4.7,8.39 -8.04,8.04 -8.44,7.7 -11.76,6.37 1,11.41 4.24,10.39 7.39,10.07 15.09,13.44 -16.78,-8.39 -7.72,-4.35 -5.8,-5.76 -7.04,-12.41 -5.04,-16.44 9.07,-5.72 12.09,-6.37 9.07,-6.7 8.39,-9.41 4.7,-9.04 1.67,-7.39 2.35,-14.09L773.16,280.46l-3.7,-8.04 -4.02,-4.35 -4.02,-1.67h-8.39l-9.41,6.04 -7.41,6.39 -11.41,5.37 -5.72,4.02 -5.37,5.04 -14.09,9.07 32.61,5.02 -12.44,6.37 -2.37,8.04 4.02,23.13 -10.39,-16.76 -9.63,-11.04 -10.07,-9.74 -4.72,-7.04 -9.13,-5.37 -22.5,-7.04 -27.89,-5.26 16.46,1 25.85,3.02 10.07,-6.37z",

    //右鬃毛
    "M762.73,420.43l9.41,4.35 8.7,10.07 12.76,14.76 -8.04,-18.78 -5.39,-9.41 -4.02,-5.02 -5.37,-4.02 -7.04,-4.02h-4.35l-2.35,2 -0.33,3.02 2,3.35 4.02,3.7z",
    "M759.05,459.34l7.37,5.37 8.39,7.04 6.04,10.07 8.39,14.09 -8.04,-24.13 -2.7,-8.04 -3.7,-6.04 -3.35,-4.02 -3.35,-2.7 -6.72,-2.35 -3.46,2.67 -1,4.02 2.02,4.02z",
    "M758.71,481.15l2.7,1.35 3.02,3.02 4.35,5.7 2.67,7.39 4.02,22.15 -6.04,-13.04 -6.04,-6.7 -6.7,-6.37 -2.35,-3.7 -1.02,-4.35 1.02,-4.02 4.35,-1.35z",

    //左鬃毛
    "M424.7,405.34l-8.7,3.35 -12.76,7.39 -6.7,5.35 -5.72,6.72 -5.02,9.07 -5.04,11.39 7.39,-10.72 6.37,-6.72 7.04,-5.02 10.74,-4.72 14.44,-3 2.7,-5.37 -0.67,-5.37 -4.02,-2.35z",
    "M408.24,435.19l-5.04,2.35 -7.37,8.15 -4.35,7.04 -3.13,6.96 -2.17,11.07 -2.02,23.48 6.98,-22.46 3.02,-6.37 3.67,-6.72 5.04,-5.35 5.37,-4.72 4.35,-3.67 1.35,-4.35 -1.02,-4.35 -4.7,-1z",
    "M414.61,479.15l-6.02,10.02 -3.37,10.07 -0.33,8.37 1.35,7.07 4.7,12.41 6.37,19.11 -5.04,-31.48 0.35,-10.74 2,-7.04 4.35,-9.39 3.37,-4.7 -1.7,-4.04 -2.67,-0.67 -3.37,1.02z",

    //左脸
    "M528.43,454.65l-8.7,-1.67 -6.39,1.35 -5.41,2.35 -8.39,3.7 -10.41,2h-6.04l-5.37,-2.67 -5.67,-5.3 -2.7,-9.39 -3.02,-14.76 -4.35,-8.07 -5.39,-5.7 -5.02,-4.02 -1.67,-9.07 2.35,-10.41 7.37,-12.41 8.07,-5.02 6.72,-0.67 2.67,4.7 3.02,10.07 4.04,4.35 5.7,2 9.74,1.67 10.07,-0.65h9.74l-15.44,-6.72 -5.72,-3.02 -4.35,-4.35 -1.33,-6.02 -1.02,-5.37 2.7,-6.39 2.35,-6.02 0.33,-3.7 -1,-3.35 -5.02,-2.7h-5.04l-3.02,1 -2.7,2.35 -3.02,7.72 -2.35,7.39 -13.41,4.7 -8.39,8.7 -5.04,9.07 -3,11.37v9.72l8.07,16.78 3.35,9.74 2.02,13.41 1.67,13.76 3.02,9.04 8.07,6.72 9.04,2.02 8.39,-1.02 16.78,-8.04 8.07,-5.04 7.39,-1.33 7.04,0.67 11.09,2.67 -3.02,-8.39 -7.39,-7.72z",
    "M518.79,491.22l-13.54,6.65 -16.11,2.67 -14.09,-1.67 -12.09,-5.02 -12.41,-9.02 -6.72,-12.74 -2.7,-11.09 -6.04,-20.11 -4.35,-25.5 -1.09,42.29 2.02,16.78 6.04,18.44 11.07,10.41 14.11,6.37 12.09,3.35 10.74,1.7 12.76,-0.67 11.74,-6.04L532.8,484.82l-14,6.39z",
    "M484.12,556.98l1,-8.04 5.37,-16.44 -12.76,-1.02 -15.76,-1.33 -17.09,-3.37 -13.44,-6.7 -11.39,-16.46 3.7,15.11 4.7,8.7 5.02,6.72 11.09,4.02 8.39,2.7 17.78,1.67 6.72,4.02 8.7,21.48 -2,-11.09z",
    "M542.86,487.19l-10.74,22.15 -5.72,10.07 -8.7,10.07 -8.07,6.37 -6.72,7.39 -4.35,10.04 1.67,13.44 3.7,14.76 6.37,11.41 9.07,6.7 9.74,0.67 17.39,-7.37 -16.46,12.07H523.14l-13.76,-4.02 -7.39,-3.7 -5.37,-6.02 -4.02,-13.76 -2.02,-12.09 1.67,-13.41 4.7,-9.39 6.72,-7.39 10.41,-9.04 8.39,-6.39 6.04,-8.39 14.44,-24.15z",

    //右脸
    "M638.87,462.71L640.54,454.39l6.72,-6.72 5.37,-1.67h5.7l9.07,1.67 18.13,3.35 7.39,-0.33 6.37,-2.02 7.72,-3.67 4.35,-4.35 2.7,-9.39 -1.35,-9.74 -1.35,-10.72 1.35,-9.41 2.02,-10.39 -1.02,-7.72 -4.35,-2.02 -6.39,-0.67 -14.76,1.67 -13.04,4.72 -17.39,3 19.13,-16.44 6.7,-10.07 1.02,-9.39 -1.02,-6.04 1.35,-5.02 3.7,-3.35 4.7,-0.67 4.04,1.33 7.37,6.04 4.04,5.72 0.67,8.37 -4.72,8.07 -11.07,7.37 22.15,-1.67 6.72,2.7 2.35,7.04 -1.7,10.07 -2.67,13.76 -0.67,9.04 3.02,10.74 2.67,8.39 -0.65,8.39 -1.35,8.39 -8.07,9.39 -9.04,5.7 -8.07,0.67h-8.04l-11.76,-2.7 -12.76,-4.02 -8.04,-1.67 -8.7,2.7 -5.83,4.63 -3.7,6.02 -0.67,-10.72z",
    "M640.54,485.85l22.15,5.04 11.09,1.35 11.74,1 14.11,-2.35 9.07,-3.7 7.04,-4.7 7.37,-8.39 7.39,-10.39 4.35,-13.04 3.35,-13.04 4.04,-33.55 1.33,19.78 0.67,21.74 -1.35,15.09 -3.35,12.09 -4.7,9.72 -6.04,8.04 -5.37,4.35 -5.37,3.35 -7.04,3.7 -10.09,2.02 -10.72,1 -9.74,-0.33 -10.76,-1.7 -12.74,-5.7 -16.44,-11.39z",
    "M678.16,539.18l10.74,2.7 7.37,0.67h9.74l9.74,-2.02 7.72,-3.7 8.7,-8.37 7.37,-10.74 3.7,-9.74 2.7,-10.39 1.67,-14.11 -4.72,12.76 -8.04,13.76 -7.72,8.39 -10.07,6.02 -8.07,2.7 -13.04,1.35 -11.76,-1.7 -13.41,-3.67 -13.04,-9.07 -23.18,-18.13 10.74,18.13 8.07,8.7 9.74,7.39 8.7,7.04 5.72,7.72 2,10.74 1.02,9.04 -3.02,12.81 -5.04,10.07 -5.83,5.7 -7.72,3.02 -12.74,0.67 13.41,4.7 10.74,-1 8.7,-8.7 4.02,-6.37 2.7,-10.07 -1.35,-12.09 -2.02,-12.07 -4.35,-12.09z",

    //鼻子
    "M542.86,530.48l5.7,1h9.74l13.44,-4.02 11.41,-1.67 10.74,0.35 12.41,2.35 10.41,2.35 6.72,-0.35 6.37,-1.35 3.02,-3.35 0.35,-4.02 -4.04,-19.57 9.39,19.57 1.02,7.72 -1.35,5.7 -5.02,3.02 -12.09,4.02 -16.46,2.35 -7.04,2.35 -7.74,5.04 -0.65,13.41v12.41l-4.35,-3.02v-8.7l-1.35,-14.09 -8.41,-6.28 -8.04,-1.7 -24.85,-4.02 -4.35,-3.7 -2,-5.7 -0.35,-6.72 10.41,-38.24 -7.04,38.24 1.33,3.37 2.7,3.35z",

    //虎牙
    "M529.1,600.62l19.46,22.13 4.04,-5.7 0.33,-21.13 11.41,12.41 8.39,-2.7 12.76,-1.67 10.07,0.33 11.74,2.02 6.04,1.35 13.44,-12.41 -1,18.46 8.7,7.04 5.37,-8.07 3.7,-6.37 5.37,-9.74 -7.04,-0.65 -8.07,-2.02 -13.39,-4.72 -15.78,-5.7 -13.04,-2.02 -7.72,0.33 -10.41,0.67 -9.74,3.37 -9.07,3.67 -7.72,3.37 -4.7,2.67 -13.04,5.04z",

    //左胡须
    "M536.82,551.29l-101.73,46.98 103.07,-44.31 -1.35,-2.67z",
    "M537.17,566.05l-82.62,46.31 84.94,-43.96 -2.35,-2.35z",
    "M476.73,618.05l77.55,-45.66 -1.67,-2 -75.88,47.63z",

    //右胡须
    "M624.76,572.77l91.66,58.05 -95.66,-56.37 4.02,-1.67z",
    "M635.52,564.03l98.68,41.96L636.2,560.92l-0.65,3.02z",
    "M633.83,551.61v-3.35l117.4,30.87 -117.4,-27.52z",

    //胸前虎纹
    "M448.2,530.15l3.67,50.98 12.44,31.55 12.74,17.11 14.11,11.41 19.13,10.74L531.84,665.27l22.15,19.81 17.13,19.78 6.72,15.78 7.72,-22.15 12.76,-17.78 13.41,-13.41 16.78,-10.74 13.76,-8.04 21.15,-8.07 24.85,-8.7 17.04,-9.96 13.76,-10.41 11.76,-13.04 6.37,-12.41L760.12,538.2l14.11,-45.66 -25.52,42.96 -18.81,32.2 -11.41,12.09 -15.44,10.74 -28.87,12.74 -22.48,8.04 -20.22,10.48 -28.87,22.48 -11.07,14.09 -7.72,12.76 -7.07,18.44 -9.72,-20.13 -16.46,-15.76 -13.76,-8.39 -11.41,-8.7 -26.09,-16.44 -16.13,-14.09 -12.41,-18.46 -22.48,-57.37z",

    //右侧虎纹
    "M761.4,340.9l30.55,74.51 7.39,28.5 2.35,15.11 1.35,11.74 1,23.92 -1,20.13 -3.04,20.46 -7.26,18.7 -9.74,15.78 -10.85,15.09 8.04,10.07 2.02,4.7 1.35,7.04 -1.35,7.72 -2.02,8.07 -10.07,16.09 -11.41,12.76 -11.74,7.72 -16.11,7.39 -13.76,4.35 -12.44,2.7 -24.15,3.67 -22.57,3.02 -10.07,1.35 -12.02,3.37 -10.41,5.04 -8.7,5.7 -12.41,9.39 -9.76,10.31 -16.78,36.24 27.52,-30.87 17.78,-11.41 10.09,-3.7 11.39,-1.33 34.59,-1.7 30.87,-2.67 22.83,-3.02 17.13,-5.37 13.76,-7.04 9.07,-6.37 16.44,-13.76 6.04,-10.39 3.7,-16.46 1,-10.39 0.67,-18.13 -0.67,-32.76 13.44,-28.85 5.37,-19.81 3.7,-22.48 0.67,-16.11 -2.02,-15.09 -6.04,-22.15 -18.02,-48.33 -29.55,-71.75 -6.04,9.39z",
    "M792.95,675.43l-22.15,23.15 -15.22,13.76 -18.46,14.76 -12.76,6.04 -12.74,4.7 -13.44,3.02 -12.76,2 -38.26,3.02 -16.78,-0.67 -16.46,2.02 -13.76,4.7 -13.96,7.72 -12.09,8.39 -7.04,5.37 -6.04,8.7 -20.48,43.31 26.09,-33.55 8.07,-8.39 9.07,-6.04 8.04,-2.67 7.72,-2.37 14.44,-1.33h24.5l29.22,0.33 30.87,-2.67 24.85,-4.7 15.78,-3.7 7.72,-3.35 6.37,-3.37 7.39,-6.7 5.04,-6.04 7.37,-13.76 9.74,-24.15 3.39,-10.07 6.7,-17.39z",
    "M742.27,774.76l-23.92,14.76 -18.46,9.39 -16.72,3.33 -22.15,-0.67h-20.81l-10.74,0.35 -11.09,3 -20.48,6.39 -16.07,10.09 -32.22,18.78 47.96,-16.2 17.39,-3.67 11.07,-2.37h12.41l16.13,2.37 14.76,-0.67 15.22,-1.7 15.44,-2.67 -69.57,91.94 83.25,-92.29 12.09,-17.11 16.46,-23.13z",

    //右前脚
    "M535.47,833.14l-72.51,105.36 -1,13.76 3.02,8.39 5.02,7.72 8.39,7.37 12.76,8.39 14.09,5.7 16.46,3.7 16.11,0.33 7.72,-2.33 4.04,-4.04 7.37,-16.09 1.02,-10.07 -3.02,-10.09 -4.04,-7.67 -14.72,-11.11 22.15,5.37 7.04,5.04 4.35,7.72 1.35,9.04 -1.02,8.7 -12.13,27.87 -4.35,6.07 -6.37,3.37 -8.7,0.65 -15.78,-0.65 -15.44,-4.04 -14.76,-6.02 -16.13,-8.07 -13.41,-14.76 -5.37,-13.74v-14.78l2.7,-9.39 75.18,-101.66z",
    "M592.54,938.84l14.11,8.04 5.37,4.02 2.35,7.04v7.72l-2.35,10.5 -4.72,12.74 -5.37,9 -3.67,2.67 -6.72,1h-38.94l41.31,5.04h6.72l4.7,-3.02 5.37,-5.02 9.74,-13.04 7.72,-11.41 2.35,-9.74 0.67,-5.02 -2.35,-7.72 -6.39,-5.37 -50,-17.11 20.13,9.74z",
    "M644.24,939.21l5.37,2.35 5.04,2.67 4.35,6.04 2.7,6.37v9.74l-4.35,18.46 -3.02,8.04 -4.02,4.7 -5.04,1.67 -34.92,4.35 36.59,-1 7.07,-1.02 3.7,-2.35 3.02,-4.7 4.35,-11.07 3.02,-11.41 2.02,-12.74 -1.02,-8.7 -2,-4.35 -4.02,-4.35 -5.72,-4.02 -7.04,-2.67 -35.24,-5.72 21.48,5.72 7.7,3.98z",

    //左侧虎纹
    "M474.38,718.04l10.74,3.7 17.13,7.04 18.11,9.07 11.09,11.07 5.37,11.41 12.76,32.87 -24.85,-28.26 -12.76,-8.35 -12.74,-5.33 -11.41,-1.67 3.02,13.76 0.65,15.09 -2.67,28.52 -1,-30.2 -3.7,-17.39 -5.04,-13.44 -8.39,-21.74 -18.13,-26.85 21.74,20.81z",

    //左前脚
    "M454.57,834.85l-9.74,3.7 -6.37,8.04 -4.04,10v12.09l1.7,7.72 3.67,11.07 5.04,7.39 17.81,17.39 -14.78,-18.11 -4.7,-13.41 -0.67,-10.74 1.67,-6.39 2.7,-5.7 5.02,-3.7 8.07,-4.02 10.07,-0.67 4.7,-6.37 -1,-5.37 -4.35,-3.7 -7.04,-1 -7.72,1.67z",
    "M472.03,814.35l-26.52,2.67 -10.41,3.37 -7.72,4.35 -8.04,8.39 -3.02,9.39v10.07l2.35,9.07 3.02,10.07 11.74,19.13 -11.07,-8.7 -10.76,-11.41 -4.35,-8.7 -2.35,-8.7v-9.15l2,-7.72 4.04,-7.72 7.04,-7.72 8.39,-5.37 8.7,-2.35 10.09,-1.67h9.39l17.39,2.7z",

    //背部虎纹
    "M466.99,243.61l5.72,-10.41 3.67,-10.74 6.39,-10.07 7.76,-7.72 13.76,-10.07 15.46,-6.02 15.44,1.33 17.11,3.37 18.41,6.74 26.52,17.78 5.04,-12.41 9.39,-10.74 11.48,-8.41 8.7,-4.35 8.7,-1.35 6.72,1 27.52,12.44 -19.13,-1.35 -9.74,0.67 -9.07,3.02 -7.04,5.02L623.15,209.37l-5.7,10.07 -3.37,12.41 0.67,16.11 7.72,-5.37 10.09,-7.04 7.37,-4.7 7.39,-2.35 9.74,-1.35 11.74,1.35 8.7,4.02 16.78,11.09 14.09,13.04 -29.55,-12.41 -7.72,-1.67h-7.74l-7.04,1.02 -7.09,2.7 -10.41,5.02 -10.07,10.07 -7.72,10.09 -1.35,12.07 -9.41,3.7 -10.74,2.02 -17.7,-2.7 -20.48,-3.02 -12.41,1.02 -12.41,3.35 -24.85,9.07 10.41,-6.72 10.74,-7.37 7.72,-4.35 9.07,-2.02 28.26,-3.02 -17.13,-6.37 -11.07,-1.02 -10.74,-0.65 -14.44,1 -14.11,3.02 -21.74,9.74 -16.44,8.04 23.92,-22.83 17.13,-13.04 14.09,-7.7 13.76,-1.02 11.41,1.67 12.09,4.35 15.44,14.09 -5.65,-17.09 -8.39,-13.76 -10.07,-8.37 -14.44,-5.72h-14.76l-15.78,3.02 -10.41,5.7 -11.07,10.07 -13.04,16.11 -8.37,6.72 -17.13,9.39 10.07,-14.44 6.04,-10.72z",
    "M363.23,243.61l20.48,-25.5 12.74,-19.46 8.07,-11.09 8.39,-8.39 9.07,-8.04 10.41,-5.7 19.13,-7.39 15.11,-2.35 10.07,1 14.44,1.7 12.41,3.35 15.46,7.04 10.39,10.39 11.41,20.48 4.7,4.72 3.37,-0.33 3.7,-9.74 3.35,-15.76 6.37,-11.09 8.39,-7.72 9.07,-4.02 14.78,-2.35 15.78,0.67 -22.5,-9.07 -8.07,-2.35 -10.39,-0.33 -9.07,3.37 -7.39,5.37 -4.7,7.04 -3.37,7.72 -2.67,2.02 -4.35,-0.35 -2.67,-2.35 -11.76,-14.09 -7.74,-5.37 -9.39,-5.7 -7.72,-2.35 -13.04,-3.02 -14.76,-1.35 -19.13,2.7 -14.76,5.7 -14.11,10.07 -9.39,9.07 -9.13,10.98 -6.72,10.07 -21.48,39.26 -12.76,22.48z",
    "M401.49,147.3l11.41,-8.39 18.81,-10.63 17.39,-7.04 12.76,-2.37 11.39,-0.33 18.48,4.02 12.41,4.35L510.91,130.45l6.72,4.7 11.41,9.39 6.04,6.37 4.35,-1.35 2.02,-7.72 4.7,-16.76 5.37,-4.72 5.37,-2 6.72,0.33 10.41,1.67 -19.13,-14.76 -7.74,-2 -6.7,0.33 -6.39,2.7 -7.35,6.44 -4.94,2.37 -7.39,-0.67 -10.07,-4.7 -13.44,-6.37 -13.04,-5.7 -10.07,-0.35 -11.74,0.67 -12.76,4.35L428.3,113.05l-35.2,40.96 8.39,-6.72z",
    "M391.81,156.69L434,124.81l23.15,-18.46 22.83,-18.11 11.76,-8.04 10.83,-4.11 9.7,-2.17 11.41,1.7 5.7,2.35 5.04,4.35 9.74,11.07 7.37,4.04 5.72,-0.67 12.09,-6.72 7.04,-2.35 9.39,0.67 8.39,3.7 19.81,17.39 13.04,9.07 19.57,16.44 14.44,16.11 8.39,14.44 5.04,13.41 4.02,13.76 5.7,10.74 6.07,8.04 15.44,12.09 11.07,8.37 -18.13,-20.46 -6.37,-9.07 -4.7,-13.04 -4.04,-20.83 -2,-7.7 -4,-6.8 -10.87,-11.41 -11.74,-12.41 -11.74,-9.74 -23.5,-16.76L603.58,93.27l-8.39,-13.04 -5.04,-4.7 -7.04,-2.35 -7.04,0.33 -8.7,4.02 -5.7,3.7 -5.72,3.02 -8.39,-0.67 -4.7,-4.35 -4.02,-6.7 -4.35,-7.72 -3.37,-6.72 -4.02,-3.35 -6.13,-3.07 -6.37,-1.02h-8.39l-9.07,3.02 -7.04,4.35 -7.07,6.72 -12.74,13.74 -15.78,18.81 -13.04,13.04 -14.44,14.41 -35.22,31.94z",

    //左侧虎纹
    "M378.7,165.43l-14.44,14.41 -12.74,16.11 -10.76,22.83 -8.39,18.78 -5.7,18.44 -5.04,19.81 -3.67,20.48 -1.35,13.04 -1.67,14.76 0.65,15.44 3.02,17.39 8.7,43.29 2.67,19.81 -2.35,20.13 -7.72,45.29 16.46,-37.92 5.7,-18.46 1.67,-13.04 -1,-14.11 -5.7,-20.46 -5.72,-21.15 -3.35,-14.09 -1.7,-9.04 -1.33,-14.44 0.33,-25.5 1.35,-17.11 3.02,-16.11 5.04,-24.15 8.04,-22.83 10.41,-18.78 11.07,-16.44 14.44,-16.44z",
    "M371.62,196.63l-29.87,25.5 -10.41,12.67 -10.07,14.44 -6.04,11.74 -5.37,15.44 -4.35,21.48 -2.35,24.83 2.35,25.5 4.35,28.26 8.7,35.92 2.7,13.76 -0.67,3.67 -8.07,26.85 -3.35,16.76 -2.02,15.44 0.67,20.13 3.37,20.48 6.04,17.39 10.07,16.78 15.11,14.76 34.57,28.26 -23.5,-28.26 -15.78,-26.85 -8.7,-22.13 -4.02,-17.78 -0.67,-18.13 2.35,-42.26 8.7,-39.59 -0.33,-5.04 -5.37,-30.44 -6.04,-35.92 -0.67,-24.15 3.02,-20.13 5.37,-19.57 10.44,-26.42 14.09,-26.85 15.78,-26.5z",
    "M322.97,495.69l8.7,-14.76 4.7,4.7 -3.02,10.39 -1.67,19.57 2,13.04 2.7,7.37 7.04,14.44 15.22,23.13 15.44,19.57 18.13,18.11 11.41,11.41 13.41,10.74 16.13,10.39 17.78,9.74 18.46,7.37 24.85,8.07 24.85,9.39 16.78,10.74 14.76,13.04 9.74,11.09 8.04,19.11 4.04,19.57 -7.07,-15.78 -16.18,-14.04 -27.18,-9.74 -15.46,-5.76 -19.81,-7.74 -37.26,-16.09 -20.13,-19.46 -24.18,-20.81 -13.04,-14.44 -16.46,-16.11 -13.46,-16.37 -17.39,-23.92 -16.11,-25.83 -5.72,-10.74 -3.7,-12.41 0.67,-15.44 3.02,-11.74z",

    //左后脚
    "M435.09,710.32l-14.44,-1 -10.74,2.35 -7.04,6.37 -5.04,9.07 -0.67,11.74 1.35,7.72 3.02,4.7 6.04,6.72 10.74,5.02 9.74,2.02 55.05,2.43 -84.59,1.67 -18.85,0.5 -16.11,-2.67 -8.07,-4.35 -6.26,-7.33 -3.37,-9.39 -0.67,-10.74 2.02,-9.39 5.37,-11.07 9.39,-8.04 13.04,-5.37 12.76,0.65 15.22,2.37 -21.15,-0.67 -10.39,4.7 -7.17,9.13 -4.7,9.07 -1.35,8.39 0.67,5.7 2.35,7.39 3.02,6.37 6.04,6.04 7.39,2h19.57l-5.35,-8.04 -2.7,-9.07 -1.35,-12.07 1.67,-8.07 4.72,-8.37 6.7,-5.37 10.74,-4.02 10.41,-1.35 7.72,-0.67v-25.26l5.37,30.2z",

    //尾部区域
    "M276.99,394.6l-10.74,10.07 -11.76,10.07 -2.35,15.09 0.35,8.07 2,10.72 4.02,10.41 6.04,9.07 8.41,6.37 8.04,2.67 12.41,1.35 16.46,-11.74 14.76,-9.07 -11.41,-3.7 -7.72,-5.37 -10.07,-8.7 -4.7,-10.74 -2.02,-13.74v-18.13l2.7,-18.46 -14.44,15.78z",
    "M204.46,434.19l-8.37,2 -9.41,0.67 -5.7,13.04 -2.02,8.7 -1,9.74 0.33,9.72 2.7,8.7 5.37,8.7 7.72,7.72 10.39,5.57 17.81,-2.02 17.39,-3.35 16.44,-6.37 -14.44,-7.39 -7.67,-6.76 -7.04,-10.74 -3.02,-8.7 -0.67,-9.72 0.67,-7.39 3.35,-22.13 -10.39,6.04 -12.44,4.02z",
    "M142.68,432.17l-7.37,-4.02 -5.04,-5.02 -12.74,0.65 -10.09,5.37 -5.37,4.7 -4.35,6.39 -3.35,8.37L92.67,458.73l0.67,9.41 1.67,10.04 5.7,8.7 10.41,7.37 18.13,7.39 18.78,4.02 11.44,1.35 -15.11,-10.41 -7.72,-8.7 -3.37,-7.04 -1,-7.72 0.67,-7.72 4.35,-10.07 5.04,-6.37 7.39,-6.04 10.33,-6.41 -8.04,-1 -9.35,-3.37z",
    "M44.01,412.38L42.67,404.38l0.67,-10.07 4.02,-13.04 4.7,-8.7 7.04,-5.7 9.41,-5.04 10.74,-1.67 11.41,0.67 13.44,3.35 12.41,7.39 -0.33,10.07 1.35,8.7 1,5.7 1,5.04 -6.04,-4.35 -7.39,-4.7 -11.07,-2.35 -8.07,1.67 -7.72,3.37L70.93,402.64l-3.7,7.39 -3.02,10.74 -0.67,10.39V441.34l2.35,11.74 -9.72,-14.09 -7.72,-13.76 -4.35,-12.74z",
    "M56.43,309.03l8.39,-14.41 14.44,-11.09 14.44,-4.7 12.41,-2 12.09,1.33 9.07,2.3 9.74,6.72 6.04,9.07 5.7,12.07 -11.41,15.11 -7.04,12.41 -6.37,13.41 -3.02,-17.39 -4.7,-12.09 -4.35,-5.7 -10.74,-4.7 -9.74,-0.35 -15.22,3.02 -12.09,6.04L47.01,331.51l9.39,-22.48z",
    "M133.98,215.41l21.15,-4.02 13.76,0.67 14.09,4.7 7.39,5.37 6.37,7.37 5.37,10.41 2.7,12.41L174.94,284.81l1,-8.7 -2.35,-10.74 -3.02,-9.41 -8.7,-9.39 -13.04,-5.02 -13.76,-2.02 -13.04,0.35 -16.78,3.7 28.87,-28.26z",
    "M203.81,155.01l8.7,-7.72L220.94,144.95h9.39l9.41,1.67L249.21,149.64l6.39,4.35 5.7,7.37 3.7,9.41 1.35,12.74 -6.72,10.07 -8.39,10.07 -11.41,12.76 -0.67,-15.78 -4.35,-11.39 -6.37,-6.39 -10.07,-5.7 -11.41,-2.7 -11.87,-0.3 -18.46,1.96 11.41,-8.7 15.78,-12.41z",
    "M236.7,93.27l0.67,-9.04 11.74,-9.39L258.18,69.57l15.22,-3.37L283.99,67.4l9.39,4.35 7.07,7.37 2,9.39 0.35,13.44 -1.7,11.39 -14.94,-9.65 -11.09,-1.67 -11.74,0.67 -13.76,3 -12.09,5.72 -7.39,4.35 3.37,-6.7 1.67,-7.04 1.67,-8.7z",
    "M233.01,73.16l-0.67,-15.44 0.67,-10.09 3.7,-8.7L241.73,32.61l6.39,-7.72 7.39,-4.02 8.37,-3.48 10.76,0.33 -16.46,-14.02 -8.7,-3.7h-11.41l-10.09,2.02L220.94,6.72l-5.7,7.04 -4.04,9.39 -1,12.07 3.37,13.04 19.57,24.85z",

    //左眼（zm不可分割）
    "M550.23,433.19v-22.83l-4.7,14.44 -3.35,-6.37 -4.35,-4.35 -8.39,-4.35 -9.39,-2 -13.76,-3.02 -9.74,-2.7 -9.39,0.67 -5.72,1 -8.39,-1.67 -5.7,-3.02 -1.67,-5.02 1.67,-6.72 -9.07,8.07 -0.67,6.37 1.35,6.72 5.7,7.37 8.07,11.41 -0.67,-8.7 -3.7,-8.7 15.78,4.35 5.7,12.09 6.37,9.39 4.35,4.02 6.72,3.35 8.07,2.7h8.04l7.72,-1.7 6.72,-3 5.04,-0.67 2.67,5.37 -2,15.41 -7.04,17.78 7.37,-10.07 4.7,-9.72 1.67,-8.7 0.35,-10.41zM542.19,435.86l-9.39,3.02 -8.7,2.02 -8.7,-1.02 -7.72,-2.67 -6.72,-4.7 -7.39,-7.72 -4.02,-8.39 1.35,-4.7 3,-1.67 5.39,-0.35 6.37,1.35 2.67,1.67 -2.35,5.72v6.37l4.35,5.37 5.04,2.67 7.04,-0.67 5.37,-5.37 1.67,-8.04 7.07,1.67 6.37,5.72 0.33,4.02z",

    //右眼（zm不可分割）
    "M706.35,394.6l-1.35,-4.35 -4.7,-1.35 -5.72,5.04 -3.35,-1.02 -13.44,6.04 -8.7,2.02 -15.76,1.35 -9.39,2 -8.39,4.35 -4.04,4.7 -3.7,6.04 -4.67,-14.44 1.67,17.39 -0.65,5.7 -0.67,6.04 -5.37,10.39 4.35,10.07 5.02,9.81 3.02,15.44 2.02,15.76 1.33,-15.11 -0.65,-9.04 -1.35,-11.41 -4.04,-19.78 2.7,-5.37 5.04,0.67 6.7,2.67 7.74,2.02L657.94,440.23l8.07,-2.7 6.72,-3.35 4.35,-4.02 6.37,-9.39 3.37,-14.11 6.37,-6.37 5.37,8.07 1.35,6.02v4.7l-0.67,4.35 -5.7,17.39 11.07,-16.11 1.35,-4.35 0.35,-4.7 -1.02,-10.39zM684.18,411.03l-3.67,8.7 -4.72,7.04 -7.37,5.37 -10.74,2.67 -12.44,-1.67 -11.41,-4.7 3.02,-10.74 6.72,-1.67 1.35,6.37 6.04,4.7 5.02,0.67 6.04,-0.67 5.04,-3.02 3.02,-4.7 0.33,-5.37 -0.67,-6.37 8.41,-3.7 4.35,-1.02 2.35,2.7z",

    //左眼区域
    "M502.58,415.4l-0.33,6.02 2,8.39 4.7,3.37 6.39,2.35 4.7,0.33 1,3.02 -9.07,-1.35 -8.04,-5.02 -4.35,-5.37 -1.67,-6.04 -0.67,-7.72 5.37,2.02z",

    //右眼区域
    "M641.89,421.1l5.02,6.72 6.07,2h7.72l6.02,-2 3.7,-1.67 -0.33,1.67 -5.37,3.35 -4.02,1.35 -8.07,0.67 -6.04,-2.72 -4.02,-3.35 -0.67,-6.04z",

    //王字
    "M606.65,382.84l-6.39,-0.67h-5.02l2,-8.7 13.04,-5.02 11.09,-2.35h6.37l7.39,2 3.02,3.7 1.35,4.35 5.02,5.37 3.7,-2.02 1.02,-6.72 -3.02,-6.37 -2.7,-4.7 -5.04,-3.35 -6.63,-2.02 -7.72,-0.33 -6.39,0.33 -10.07,3.35 -12.41,6.04v-9.72l0.67,-5.37 3.02,-5.72 12.07,-7.7 10.09,-4.04 7.72,-2.35H636.2l12.76,2.7 10.07,4.35 8.39,6.04 6.72,9.74 -1,-10.41 -3.7,-8.37 -5.37,-6.39 -7.04,-3.35 -9.09,-3.37 -13.44,-1.67 -13.41,2.67 -13.44,4.02 -10.07,5.04 -7.39,4.7 -9.74,-5.37 -10.41,-4.35 -16.78,-1.35 -13.04,-0.65 -8.39,0.33 -8.39,2.35 -5.02,2.35 -5.37,4.02 -5.11,5.04 -4.7,7.37 -0.67,9.07 2.67,7.04 8.39,8.39 -2.67,-9.74v-7.72l2.67,-6.04 5.72,-3.67 10.74,-4.7 14.76,-1.67 16.81,2 16.11,6.37 2,5.04v14.09l-7.72,-2.35 -11.74,-2.7 -11.76,0.67 -7.37,2.02 -6.04,3.35 -5.37,5.37 -1.7,6.37 0.35,6.39 2.35,4.35 4.02,-6.04 4.04,-4.7 5.02,-3.35 6.37,-2.35 6.07,-1h6.7l12.09,2.67 5.37,5.72 -8.04,1.67 -5.76,3.35 -5.7,7.39 5.7,-1.35h4.7l8.39,1.35 5.37,7.37 1.7,18.46 3,-21.48 5.39,-5.7 7.04,-1.67 6.72,1 5.7,1.67 -3.7,-5.7 -4.35,-2.7z",

    //小胡子区域
    "M560.65,635.16l3.7,13.44 0.67,-12.09 -1.67,-2.7h-1.35l-1.35,1.35z",
    "M569.39,641.88l2.35,13.04 1.67,-9.74v-3.35l-2.35,-2.02v0.67l-1.67,1.35z",
    "M578.1,646.9l3.7,15.78 -1,-12.09v-3.7l-2.02,-0.67 -0.67,0.67z",

//    "M618.06,645.23l2,-10.39 2.02,3.02 -0.33,4.7 -3.7,2.67z",
//    "M610.32,637.86l1.7,15.09 3.02,-14.09 -1.7,-1 -1,-0.35 -2.02,0.35z",

)

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {

    val pathList = arrayListOf<Path>()

    tigerSvgPathList.forEach {
        val path = Path()
        processPath(string = it, path = path)
        path.close()
        pathList.add(path)
    }

    val color = Color(0xFF4b0500)
    Canvas(
        modifier = Modifier
            .fillMaxSize(),
        onDraw = {
            pathList.forEach {
                drawPath(
                    path = it,
                    color = color,
                )
            }

        })
}
