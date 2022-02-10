# 开篇废话

无奈放假前又出了两个需求，忙活到昨天终于是上线了。 打开掘金发现大家都在创意闹新春，有放烟花的，有写福、写春联的，有年兽作战小游戏的，还有画虎的。该说不说，‘你们掘金花活儿挺多啊’！我也来参与了，真香~

写这篇文章呢是看到了前端同学使用Echarts画的老虎（ [《辞旧迎新 2022 我用Echarts画了个大老虎，祝大家新春快乐，虎虎生威啊。》](https://juejin.cn/post/7052131669288943629)）。这老虎确实可以，我就同样直接拿来了，感谢iconfont作者Eve！

![Snipaste_2022-01-26_17-22-24.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/17a8e408651e417e8310bf2140b0a5a3~tplv-k3u1fbpfcp-watermark.image?)

# 实现目标

这篇文章我们就利用Compose的Canvas来一块一块的绘制出一只猛虎，如上动图，如假包换。

关于Compose Canvas有不太熟悉的同学可以参考我之前写的博客 [《Jetpack Compose - Canvas》](https://blog.csdn.net/u010976213/article/details/114638415)，里面详细介绍了Canvas的各种draw()方法，我们这里主要就是使用drawPath()方法来实现。

# 实现流程

首先我们需要点击素材的 【SVG下载】，下载后导入Android Studio：

![Snipaste_2022-01-26_17-35-13.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/fcd75061120749079376b5edeaea00fa~tplv-k3u1fbpfcp-watermark.image?)

在资源文件夹上右键，选择 **Vector Asset** 来导入刚刚下载的svg文件。

![Snipaste_2022-01-26_18-04-06.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a01d18ef1f6246b99bbee85d7358c822~tplv-k3u1fbpfcp-watermark.image?)

OK，大功告成 ~~~~~~ 了一部分！

来看下生成的XML文件，主要在于其中pathData的值，我们就需要利用这些值来一步步的进行绘制。那么这些值要怎么看呢？

## 1、认识pathData

关于SVG path的详情介绍请看[Paths – SVG 1.1 (Second Edition)](https://www.w3.org/TR/SVG11/paths.html#PathData)。

我呢这里先根据Android写了一个了简单的pathData，渲染图如右侧所示：


![Snipaste_2022-01-26_19-19-43.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d9134251dd2447ac8198f52e7130ebb3~tplv-k3u1fbpfcp-watermark.image?)

接下来一起看下这段神奇的指令：

```
M0,0 l100,0 L100,100 h100 v-100 h100 v200 H0 z
```

- M0,0 ，表示path.moveTo()，后面的0,0就是坐标位置，所以意思就是就是将画笔移到了A(0,0)的位置
- l100,0 ，表示path.relativeLineTo()，也就是说相对于刚才的A点，横向移动100，水平移动0到B点，那么B的坐标点为(100,0)
- L100,100 ，表示path.lineTo()，也就是绝对的移动了，直接将画笔移动到(100,100)的位置，所以C的坐标点为（100,100）
- h100 ，horizontal，表示path.relativeLineTo()，是一种简化版的写法，意思就是相对于C点纵向不变，横向移动100到D点，所以D的坐标为(200,100)。如果用l表示，那么等同于“l100,0”
- v-100，vertical，表示path.relativeLineTo()，是一种简化版的写法，意思就是相对于D点横向不变，纵向向上移动100到E点，所以E的坐标为(200,0)

剩下的h、v移动则不说明了，直接看H：
- H0，同样是横向移动，只是后面的0表示要移动到 x=0 的位置，需要与上文h的相对移动距离区分开。那么从G点直接横向移动到x=0的位置，所以H点坐标(0,100)
- z ，表示path.close()

经过上面描述，可能大家大致就能明白pathData所表示的含义了。并且一个Path可以由M开头z结尾来完成。

其实后面还有更复杂的C（三次贝塞尔曲线）、Q（二次贝塞尔曲线）、A（圆弧）等，但是，这里我们就不过多介绍了。

以上内容可以足够让我们画出来上文提到的猛虎了。

## 2、解析pathData

了解到pathData的构成及含义后，我们就可以进行解析，转成Canvas需要的path，然后就可以进行绘制了。

大致描述下我的临时解法吧，将上述规范的pathData字符串一个字符一个字符的进行解析，如果字符是字母等，则表示移动moveTo()、lineTo()等的命令，后面跟着的则是坐标，连续的坐标是可以用空格隔开的，处理的时候需要注意。

其中处理H命令时就有点特殊了，因为我们只知道要移动到x=0的位置，可是如果当前的位置是相对移动来的，那么我们就不知道当前位置的y坐标，所以我们还需要手动计算出当前的y坐标，那么就需要 **PathMeasure** 类来帮我们处理（感谢【路很长OoO】大佬的指导）。

```kotlin

//由于在compose下PathMeasure并没有获取位置的方法，
//所以我们使用android.graphics.PathMeasure()，
//然后path也需要转换成android.graphics.Path

pathMeasure.setPath(path.asAndroidPath(), false)

val position = FloatArray(2)
val tan = FloatArray(2)

pathMeasure.getPosTan(pathMeasure.length, position, tan)

//至此position就存储了当前的位置坐标
```

获取到移动的命令以及坐标后，整体的处理就变得很简单了，如下所示：

```kotlin
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
```


所以根据上述老虎的SVG图片，从转成的Android pathData我们可以获取到多条path，然后我们按照顺序将其进行绘制，最终就可以得到上述动图了。

说起来很简单，可是在解析pathData以及在绘制老虎的顺序上真真是废了老半天的劲了，一条一条的进行展示、隐藏，看下排序过后的部分数据吧：

![Snipaste_2022-01-26_21-04-08.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3c7cafacc4824c2fae85d525493507a1~tplv-k3u1fbpfcp-watermark.image?)

最后再看下动图，哇，成果还是可以的吧。画虎点睛，王！！！

![GIF 2022-1-26 16-31-18.gif](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e69255f1d4674f299faa36e04d775971~tplv-k3u1fbpfcp-watermark.image?)

最后的最后，祝大家新年快乐，虎虎生威啊！！！
