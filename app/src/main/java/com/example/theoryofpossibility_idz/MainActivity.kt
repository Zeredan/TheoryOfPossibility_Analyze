package com.example.theoryofpossibility_idz

import android.graphics.Outline
import android.graphics.Typeface
import android.media.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.theoryofpossibility_idz.ui.theme.TheoryOfPossibility_idzTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.StringFormat
import org.apache.commons.math3.analysis.function.Gaussian
import org.apache.commons.math3.distribution.ChiSquaredDistribution
import org.apache.commons.math3.distribution.FDistribution
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.inference.ChiSquareTest
import org.apache.commons.math3.stat.inference.TTest
import org.apache.commons.math3.stat.inference.TestUtils
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.reflect.KProperty

infix fun<T : Number> T.roundTo(decimals: Int) : Double{
    return pow(10.0, decimals.toDouble()).run{
        round(this@roundTo.toDouble() * this) / this
    }
}

class MyViewModel : ViewModel() {
    data class DistributionData(var M: Double, var D: Double, var MO: Double, var ME: Double, var AS: Double, var EXC: Double, var VARIATION: Double)
    inner class Content(var text: String, var image: Int? = null, var content: @Composable () -> Unit)

    val IQDataset = iqDataSet_1.toMutableStateList()
    val SATDataset = correspondingSATDataSet_1.toMutableStateList()
    val intervalLengthIQ: Double = 2.0
    val intervalLengthSAT: Double = 40.0

    var currentContent by mutableStateOf("Графики")

    lateinit var mainNavController: NavHostController
    var navElements: SnapshotStateList<Content> = mutableStateListOf(
        Content("Данные", R.drawable.data){
            DataSet(IQDataset, getIntervalIqData(), SATDataset, getIntervalSATData(), 55.0, 145.0, 400.0, 2000.0)
        },
        Content("Графики", R.drawable.mainicon){
            Graphics(IQDataset, getIntervalIqData(), SATDataset, getIntervalSATData())
        },
        Content("К.Р.А.", R.drawable.correlation){
            //Text("пока неизвестно")
            CorrelationRegressionAnalyze(IQDataset.map(Int::toDouble), getIntervalIqData(), SATDataset.map(Int::toDouble), getIntervalSATData())
        },
        Content("Отчёт", R.drawable.analyze){
            MainData(IQDataset.map(Int::toDouble), getIntervalIqData(), SATDataset.map(Int::toDouble), getIntervalSATData())
        }
    )

    fun getIntervalIqData() : Map<Pair<Double, Double>, List<Int>>{
        infix fun Double.between(interv: Pair<Double, Double>) : Boolean{
            return (this >= interv.first && this <= interv.second)
        }

        return List((90 / intervalLengthIQ).roundToInt()) {
            55 + it * intervalLengthIQ to 55 + (it + 1) * intervalLengthIQ
        }.map{
            it to IQDataset.filter{it1 -> it1.toDouble() between it}
        }.toMap().toSortedMap{a, b -> (a.first - b.first).toInt()}
    }
    fun getIntervalSATData() : Map<Pair<Double, Double>, List<Int>>{
        infix fun Double.between(interv: Pair<Double, Double>) : Boolean{
            return (this >= interv.first && this <= interv.second)
        }

        return List((217 * 6 / intervalLengthSAT).roundToInt()) {
            (1000 - 3 * 217) + it * intervalLengthSAT to (1000 - 3 * 217) + (it + 1) * intervalLengthSAT
        }.map{
            it to SATDataset.filter{it1 -> it1.toDouble() between it}
        }.toMap().toSortedMap{a, b -> (a.first - b.first).toInt()}
    }

    fun getDistributionData(elements: List<Double>, intervalData: Map<Pair<Double, Double>, List<Int>>) : DistributionData{
        val mX = elements.average()
        val moX = intervalData.entries.sortedByDescending { it.value.size }[0].key
        val meX = elements.sorted().let{ it[it.size / 2] }
        val dX = elements.run{ sumOf{ Math.pow(it - mX, 2.0) } / this.size}
        val sigmaX = sqrt(dX)
        val asX = elements.run{ sumOf{ pow(it - mX, 3.0) } / this.size } / pow(sigmaX, 3.0)
        val excX =  elements.run{ sumOf{ pow(it - mX, 4.0) } / this.size } / pow(sigmaX, 4.0) - 3

        return DistributionData(mX roundTo 2, dX roundTo 2, moX.run{ (first + second) / 2 } roundTo 2, meX roundTo 2, asX roundTo 2, excX roundTo 2, (sigmaX / mX) roundTo 2)
    }
    fun getDistributionDataNoInterval(elements: List<Double>) : DistributionData{
        val mX = elements.average()
        val meX = elements.sorted().let{ it[it.size / 2] }
        val dX = elements.run{ sumOf{ Math.pow(it - mX, 2.0) } / this.size}
        val sigmaX = sqrt(dX)
        val asX = elements.run{ sumOf{ pow(it - mX, 3.0) } / this.size } / pow(sigmaX, 3.0)
        val excX =  elements.run{ sumOf{ pow(it - mX, 4.0) } / this.size } / pow(sigmaX, 4.0) - 3

        return DistributionData(mX roundTo 2, dX roundTo 2, 0.0, meX roundTo 2, asX roundTo 2, excX roundTo 2, (sigmaX / mX) roundTo 2)
    }
    fun DistributionNormalChi2Values(elements: List<Double>, intervalData: Map<Pair<Double, Double>, List<Int>>, alpha: Double = 0.01) : Pair<Double, Double>{
        val dData = getDistributionData(elements, intervalData)
        val mItList = intervalData.entries.map{
            //mi t = n * pi
            //pi = Ф(xi - mX   /   sigmaX) - Ф(xi - 1 - mX    /    sigmaX)
            elements.size *
                    NormalDistribution(dData.M, sqrt(dData.D)).run{cumulativeProbability(it.key.second) - cumulativeProbability(it.key.first)}
        }
        val xi2Obs = intervalData.entries.mapIndexed{ ind, it ->
            pow((it.value.size - mItList[ind]), 2.0) / mItList[ind]
        }.sum()
        val xi2Crit = ChiSquaredDistribution(intervalData.size - 3.0).inverseCumulativeProbability(1 - alpha)
        return (xi2Obs roundTo 2) to (xi2Crit roundTo 2)
    }
    fun getIntervalDelta(elements: List<Double>, y: Double) : Double{
        // F(mx + deltaX) - F(mx - deltaX) = y
        // 2Ф(deltaX / sigmaX * sqrt(n)) = y
        // deltaX = Ф^-1(y/2) * sigmaX / sqrt(n)

        return (NormalDistribution().inverseCumulativeProbability(0.5 + y / 2) * sqrt(elements.run{ sumOf{ Math.pow(it - elements.average(), 2.0) } / this.size}) / sqrt(elements.size.toDouble())) roundTo 2
    }
    fun getF_T_Results(elements1: List<Double>, elements2: List<Double>, alpha: Double = 0.05) : List<Double>{
        var S0_1 = elements1.average().run{ elements1.sumOf{ pow(it - this, 2.0) } } / elements1.size * elements1.size.let{(it + 1).toDouble() / it}
        var S0_2 = elements2.average().run{ elements2.sumOf{ pow(it - this, 2.0) } } / elements2.size * elements2.size.let{(it + 1).toDouble() / it}

        var (S0_num, S0_den) = listOf(S0_1, S0_2).sortedDescending()
        var (nFirst, nSecond) = if (S0_1 != S0_num) elements2.size to elements1.size else elements1.size to elements2.size

        val FObs = S0_num / S0_den
        val FCrit = FDistribution(nFirst.toDouble(), nSecond.toDouble()).inverseCumulativeProbability(1 - alpha)

        val TObs = abs(elements1.average() - elements2.average()) / sqrt((elements1.size - 1) * S0_1 + (elements2.size - 1) * S0_2) * sqrt(elements1.size.toDouble() * elements2.size * (elements1.size + elements2.size - 2) / (elements1.size + elements2.size))
        val TCrit = TDistribution(nFirst + nSecond - 2.0).inverseCumulativeProbability(1 - alpha)
        return listOf(FObs roundTo 2, FCrit roundTo 2, TObs roundTo 2, TCrit roundTo 2)
    }
    fun getCorrelationTResults(rxy: Double, n: Int, alpha: Double = 0.01) : List<Double>{
        val TObs = rxy * sqrt(n - 2.0) / sqrt(1 - rxy*rxy)
        val TCrit = TDistribution(n - 2.0).inverseCumulativeProbability(1 - alpha / 2)
        return listOf(TObs roundTo 2, TCrit roundTo 2)
    }
    fun getBiTResults(bi: Double, Sbi: Double, n: Int, alpha: Double = 0.01) : List<Double>{
        val TObs = bi / Sbi
        val TCrit = TDistribution(n - 2.0).inverseCumulativeProbability(1 - alpha / 2)
        return listOf(TObs roundTo 2, TCrit roundTo 2)
    }
    fun getMinYForBi(bi: Double, Sbi: Double, n: Int) : Double{
        val TObs = bi / Sbi
        val TCritValues = List(1000){ (1 - it.toDouble() / 1000) to TDistribution(n - 2.0).inverseCumulativeProbability(1 - it.toDouble() / 1000)}
        val ourValue = TCritValues.filter { it.second <= TObs }.maxOfOrNull{ it.second } ?: 0
        println("OurValue: ${ourValue}; tObs: ${TObs}")
        println(TCritValues)
        return TCritValues.firstOrNull{it.second == ourValue}?.first ?: 0.0
    }
    fun getCorrelationEquationFResults(RSS: Double, ESS: Double, k1: Int, k2: Int, alpha: Double = 0.01) : List<Double>
    {
        val FObs = RSS / k1 / ESS / k2
        val FCrit = FDistribution(k1.toDouble(), k2.toDouble()).inverseCumulativeProbability(1 - alpha)
        return ((FObs roundTo 2) to (FCrit roundTo 2)).toList()
    }
}

@Composable
fun PlotBox(modifier: Modifier, scaleHeight: Float, elements: List<Double>)
{
    var xData = viewModel<MyViewModel>().getDistributionDataNoInterval(elements)

    var xQ1 = elements.sorted()[(elements.size * 0.25).roundToInt()]
    var xQ3 = elements.sorted()[(elements.size * 0.75).roundToInt()]
    var xTopEdge = xQ3 + 1.5 * (xQ3 - xQ1)
    var xBottomEdge = xQ1 - 1.5 * (xQ3 - xQ1)
    val xThrowValues = elements.filter { (it > xTopEdge) || (it < xBottomEdge) }

    var canvasSize by remember {
        mutableStateOf(IntSize(0, 0))
    }
    Canvas(
        modifier = modifier.onSizeChanged { canvasSize = it },
        onDraw = {
            drawLine(
                Color.Black,
                Offset(canvasSize.width.toFloat() / 2, canvasSize.height - xBottomEdge.toFloat() * scaleHeight),
                Offset(canvasSize.width.toFloat() / 2, canvasSize.height - xQ1.toFloat() * scaleHeight),
                10f
            )
            drawLine(
                Color.Black,
                Offset(canvasSize.width.toFloat() / 2, canvasSize.height - xQ3.toFloat() * scaleHeight),
                Offset(canvasSize.width.toFloat() / 2, canvasSize.height - xTopEdge.toFloat() * scaleHeight),
                10f
            )
            drawLine(
                Color.Black,
                Offset(canvasSize.width.toFloat() / 2 - 80f, canvasSize.height - xBottomEdge.toFloat() * scaleHeight),
                Offset(canvasSize.width.toFloat() / 2 + 80f, canvasSize.height - xBottomEdge.toFloat() * scaleHeight),
                10f
            )
            drawLine(
                Color.Black,
                Offset(canvasSize.width.toFloat() / 2 - 80f, canvasSize.height - xTopEdge.toFloat() * scaleHeight),
                Offset(canvasSize.width.toFloat() / 2 + 80f, canvasSize.height - xTopEdge.toFloat() * scaleHeight),
                10f
            )
            drawRect(
                Color.Red,
                Offset(canvasSize.width / 2 - 80f, canvasSize.height - xQ3.toFloat() * scaleHeight),
                Size(160f, (xQ3 - xData.ME).toFloat() * scaleHeight),
            )
            drawRect(
                Color.Yellow,
                Offset(canvasSize.width / 2 - 80f, canvasSize.height - xData.ME.toFloat() * scaleHeight),
                Size(160f, (xData.ME - xQ1).toFloat() * scaleHeight),
            )
            drawRect(
                Color.Black,
                Offset(canvasSize.width / 2 - 80f, canvasSize.height - xQ3.toFloat() * scaleHeight),
                Size(160f, (xQ3 - xQ1).toFloat() * scaleHeight),
                style = Stroke(10f)
            )
            drawLine(
                Color.Black,
                Offset(canvasSize.width.toFloat() / 2 - 80f, canvasSize.height - xData.ME.toFloat() * scaleHeight),
                Offset(canvasSize.width.toFloat() / 2 + 80f, canvasSize.height - xData.ME.toFloat() * scaleHeight),
                10f
            )
            xThrowValues.forEach {
                drawCircle(Red, 15f, Offset(canvasSize.width.toFloat() / 2, canvasSize.height - it.toFloat() * scaleHeight))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun<T : Number> DataSet(elementsX: List<T>, intervalDataX: Map<Pair<Double, Double>, List<Int>>, elementsY: List<T>, intervalDataY: Map<Pair<Double, Double>, List<Int>>, lowerBoundX: Double, upperBoundX: Double, lowerBoundY: Double, upperBoundY: Double)
{
    var screenSize by remember{ mutableStateOf(IntSize(0, 0)) }
    var vm = viewModel<MyViewModel>()

    @Composable
    fun<T: Number> DistributionInfo(modifier: Modifier, elements: List<T>, intervalData: Map<Pair<Double, Double>, List<Int>>, lowerBound: Double, upperBound: Double){
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Spacer(Modifier.height(20.dp))
            LazyVerticalGrid(
                modifier = Modifier
                    .border(2.dp, color = Color.White, shape = RoundedCornerShape(10.dp))
                    .fillMaxWidth(0.85f)
                    .weight(1f),
                columns = GridCells.Fixed(3)
            )
            {
                items(elements) {
                    Box(
                        contentAlignment = Alignment.Center
                    )
                    {
                        BadgedBox(
                            modifier = Modifier
                                .padding(10.dp)
                                .background(Color.Black)
                                .size(60.dp),
                            badge = {
                                Image(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(
                                        id =
                                        when {
                                            (it.toDouble() > upperBound) -> R.drawable.fire
                                            (it.toDouble() < lowerBound) -> R.drawable.bad
                                            else -> R.drawable.ok
                                        }
                                    ),
                                    contentDescription = null
                                )
                            },
                            content = {
                                Text(it.toString(), color = Color.White)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Column(
                modifier = Modifier
                    .border(2.dp, color = Color.White, shape = RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .weight(1f)
                    .fillMaxWidth(0.85f)
                    .verticalScroll(rememberScrollState())
            )
            {
                intervalData.let { data ->
                    data.forEach { (k, v) ->
                        Row() {
                            Text(
                                modifier = Modifier.width(150.dp),
                                text = String.format("%-25s", "[${k.first}; ${k.second}]: "),
                                color = Color.White
                            )
                            Text("${v.size}", color = Color.Green)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    Column(
        modifier = Modifier
            .onSizeChanged { screenSize = it }
            .background(Color.DarkGray)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    )
    {
        Text(text = "X (IQ)", textAlign = TextAlign.Center, modifier = Modifier
            .background(Color.Magenta)
            .fillMaxWidth()
            .height(30.dp))
        DistributionInfo(
            modifier = Modifier
                .padding(6.dp)
                .border(3.dp, color = Color.LightGray, shape = RoundedCornerShape(10.dp))
                .padding(3.dp)
                .fillMaxWidth()
                .height(screenSize.height.dp / LocalDensity.current.density),
            elements = elementsX,
            intervalData = intervalDataX,
            lowerBound = lowerBoundX,
            upperBound = upperBoundX
        )

        Spacer(Modifier.height(40.dp))

        Text(text = "Y (SAT results)", textAlign = TextAlign.Center, modifier = Modifier
            .background(Color.Magenta)
            .fillMaxWidth()
            .height(30.dp))
        DistributionInfo(
            modifier = Modifier
                .border(3.dp, color = Color.LightGray, shape = RoundedCornerShape(10.dp))
                .padding(5.dp)
                .fillMaxWidth()
                .height(screenSize.height.dp / LocalDensity.current.density),
            elements = elementsY,
            intervalData = intervalDataY,
            lowerBound = lowerBoundY,
            upperBound = upperBoundY
        )
    }
}

@Composable
fun<T> Graphics(elementsX: List<T>, intervalDataX: Map<Pair<Double, Double>, List<Int>>, elementsY: List<T>, intervalDataY: Map<Pair<Double, Double>, List<Int>>) where T : Number
{
    var screenSize by remember{ mutableStateOf(IntSize(0, 0)) }
    var density = LocalDensity.current.density
    @Composable
    fun Graphic(modifier: Modifier, elements: List<T>, intervalData: Map<Pair<Double, Double>, List<Int>>, scaleCoeff: Int, name: String){
        Row(
            modifier = modifier
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        )
        {
            var canvasSize by remember {
                mutableStateOf(IntSize(0, 0))
            }
            val maxCount = intervalData.maxOf{
                it.value.size
            }
            Spacer(Modifier.width(20.dp))
            Canvas(
                modifier = Modifier
                    .onSizeChanged {
                        canvasSize = IntSize(it.width - 300, it.height)
                    }
                    .fillMaxHeight(0.85f)
                    .width(intervalData.maxOf { it.key.second * scaleCoeff }.dp / density + 500.dp),
                onDraw = {
                    val textPaintY = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        textSize = 24.sp.toPx()
                        color = android.graphics.Color.BLUE
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

                    }
                    val textPaintX = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        textSize = 12.sp.toPx()
                        color = android.graphics.Color.YELLOW
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    }

                    val intervalLength = intervalData.entries.first().key.let{it.second - it.first}
                    val allOffset = Offset(50f, -50f)
                    val xIntervalOffset = intervalData.entries.first().key.first % intervalLength

                    //oX line
                    drawLine(
                        Color.White,
                        Offset(0f, canvasSize.height.toFloat()) + allOffset,
                        Offset(canvasSize.width.toFloat() + 100f, canvasSize.height.toFloat()) + allOffset,
                        strokeWidth = 6f
                    )
                    //oX triangle arrow
                    drawPath(
                        Path().apply{
                            this.moveTo(canvasSize.width.toFloat() + 100f + allOffset.x, canvasSize.height.toFloat() + 10f + allOffset.y)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, 0f, -20f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, 50f, 10f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, -50f, 10f)
                        },
                        Color.White
                    )
                    //oX value lines
                    repeat((canvasSize.width / intervalLength).roundToInt()){
                        drawLine(
                            Color.Black,
                            Offset((it + 1.5f) * intervalLength.toFloat() * scaleCoeff + xIntervalOffset.toFloat() * scaleCoeff + allOffset.x, canvasSize.height + 20f + allOffset.y),
                            Offset((it + 1.5f) * intervalLength.toFloat() * scaleCoeff + xIntervalOffset.toFloat() * scaleCoeff + allOffset.x, canvasSize.height - 20f + allOffset.y),
                            10f
                        )
                        rotate(30f, Offset((it + 1.5f) * intervalLength.toFloat() * scaleCoeff + xIntervalOffset.toFloat() * scaleCoeff + allOffset.x, canvasSize.height + 50f + allOffset.y))
                        {
                            drawIntoCanvas { cnv ->
                                cnv.nativeCanvas.drawText(
                                    "${it + 1}",
                                    (it + 1.5f) * intervalLength.toFloat() * scaleCoeff + xIntervalOffset.toFloat() * scaleCoeff + allOffset.x,
                                    canvasSize.height + 50f + allOffset.y,
                                    textPaintX
                                )
                            }
                        }
                    }
                    //oX text
                    drawIntoCanvas { cnv ->
                        cnv.nativeCanvas.drawText(
                            "$name",
                            canvasSize.width + 100f + allOffset.x,            // x-coordinates of the origin (top left)
                            canvasSize.height - 50f + allOffset.y, // y-coordinates of the origin (top left)
                            textPaintY
                        )
                    }

                    //oY line
                    drawLine(
                        Color.White,
                        Offset(0f, canvasSize.height.toFloat()) + allOffset,
                        Offset(0f, -20f) + allOffset,
                        strokeWidth = 6f
                    )
                    //oY triangle arrow
                    drawPath(
                        Path().apply{
                            this.moveTo(-10f + allOffset.x, -20f + allOffset.y)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, 20f, 0f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, -10f, -50f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, -10f, 50f)
                        },
                        Color.White
                    )
                    //oY value lines and chained ones
                    repeat(maxCount){
                        drawLine(
                            Color.Green,
                            Offset(0f, canvasSize.height.toFloat() * (maxCount - it - 1) / maxCount) + allOffset,
                            Offset(canvasSize.width.toFloat() + 100f, canvasSize.height.toFloat() * (maxCount - it - 1) / maxCount) + allOffset,
                            4f,
                            pathEffect = PathEffect.dashPathEffect(arrayOf(30f, 40f).toFloatArray(), 10f)
                        )
                        drawLine(
                            Color.Black,
                            Offset(-20f, canvasSize.height.toFloat() * (maxCount - it - 1) / maxCount) + allOffset,
                            Offset(20f, canvasSize.height.toFloat() * (maxCount - it - 1) / maxCount) + allOffset,
                            10f
                        )
                        drawIntoCanvas { cnv ->
                            cnv.nativeCanvas.drawText(
                                "${it + 1}",
                                30f + allOffset.x,            // x-coordinates of the origin (top left)
                                canvasSize.height.toFloat() * (maxCount - it - 1) / maxCount + allOffset.y - 20f, // y-coordinates of the origin (top left)
                                textPaintY
                            )
                        }
                    }
                    //oY text
                    drawIntoCanvas { cnv ->
                        cnv.nativeCanvas.drawText(
                            "N",
                            -30f,            // x-coordinates of the origin (top left)
                            -10f.dp.toPx(), // y-coordinates of the origin (top left)
                            textPaintY
                        )
                    }

                    //data
                    intervalData.forEach { (k, v) ->
                        drawRect(
                            color = Color.Magenta,
                            topLeft = Offset(
                                k.first.toFloat() * scaleCoeff,
                                canvasSize.height * (maxCount - v.size.toFloat()) / maxCount.toFloat()
                            ) + allOffset,
                            size = Size(
                                (k.second - k.first).toFloat() * scaleCoeff,
                                canvasSize.height * (v.size.toFloat()) / maxCount.toFloat()
                            )
                        )
                        drawRect(
                            color = Color.Black ,
                            topLeft = Offset(
                                k.first.toFloat() * scaleCoeff,
                                canvasSize.height * (maxCount - v.size.toFloat()) / maxCount.toFloat()
                            ) + allOffset,
                            size = Size(
                                (k.second - k.first).toFloat() * scaleCoeff,
                                canvasSize.height * (v.size.toFloat()) / maxCount.toFloat()
                            ),
                            style = Stroke(width = 6f)
                        )
                    }
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .onSizeChanged { screenSize = it }
            .background(Color.Gray)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    )
    {

        Spacer(Modifier.height(40.dp))
        Graphic(
            modifier = Modifier
                .background(Color.DarkGray)
                .fillMaxWidth()
                .height(screenSize.height.dp / LocalDensity.current.density),
            elements = elementsX,
            intervalData = intervalDataX,
            scaleCoeff = 25,
            name = "IQ"
        )
        Spacer(Modifier.height(40.dp))
        Graphic(
            modifier = Modifier
                .background(Color.DarkGray)
                .fillMaxWidth()
                .height(screenSize.height.dp / LocalDensity.current.density),
            elements = elementsY,
            intervalData = intervalDataY,
            scaleCoeff = 2,
            name = "SAT"
        )
    }
}

@Composable
fun MainData(elementsX: List<Double>, intervalDataX: Map<Pair<Double, Double>, List<Int>>, elementsY: List<Double>, intervalDataY: Map<Pair<Double, Double>, List<Int>>, alpha: Double = 0.05)
{
    val vm = viewModel<MyViewModel>()

    var xData = vm.getDistributionData(elementsX, vm.getIntervalIqData())
    var yData = vm.getDistributionData(elementsY, vm.getIntervalSATData())

    var yNormalChi2Values = vm.DistributionNormalChi2Values(elementsY, intervalDataY)
    var y95intervalDelta = vm.getIntervalDelta(elementsY, 0.95)

    var (y1elements, y2elements) = elementsY.mapIndexed{ind, it -> it to ind}.partition { it.second % 2 == 0 }.let{it.first.map{it1 -> it1.first} to it.second.map{it1 -> it1.first}}
    var (FObs, FCrit, TObs, TCrit) = vm.getF_T_Results(y1elements, y2elements, 0.01)

    Column(
        modifier = Modifier
            .background(Color.Gray)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Column()
        {
            Text("Задача 1", modifier = Modifier
                .padding(10.dp)
                .scale(1.5f)
                .align(Alignment.CenterHorizontally), color = Color.White)
            Text("Выборка - Эксперименты (IQ, результат SAT) 79 студентов\nX - IQ, Y - результат теста SAT (возможна по предположению положительная корреляция)")
        }
        Column()
        {
            Text("Задача 2-3(x)", modifier = Modifier
                .padding(10.dp)
                .scale(1.5f)
                .align(Alignment.CenterHorizontally), color = Color.White)
            Row()
            {
                Text("MX:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${xData.M}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("MOX:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${xData.MO}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("MEX:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${xData.ME}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("DX:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${xData.D}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("sigmaX:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${sqrt(xData.D) roundTo 2}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("ASX:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${xData.AS}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("EXCX:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${xData.EXC}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("VariationX:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${xData.VARIATION}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
        }
        Column()
        {
            Text("Задача 2-3(y)", modifier = Modifier
                .padding(10.dp)
                .scale(1.5f)
                .align(Alignment.CenterHorizontally), color = Color.White)
            Row()
            {
                Text("MY:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${yData.M}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("MOY:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${yData.MO}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("MEY:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${yData.ME}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("DY:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${yData.D}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("sigmaY:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${sqrt(yData.D) roundTo 2}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("ASY:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${yData.AS}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("EXCY:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${yData.EXC}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
            Row()
            {
                Text("VariationY:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${yData.VARIATION}", color = Color.Black, modifier = Modifier.width(100.dp))
            }
        }
        Column()
        {
            Text("Задача 4", modifier = Modifier
                .padding(10.dp)
                .scale(1.5f)
                .align(Alignment.CenterHorizontally), color = Color.White)
            Row()
            {
                Text("VariationY:", color = Color.Blue, modifier = Modifier.width(150.dp))
                Text("${yData.VARIATION}", color = Color.Black, modifier = Modifier.width(100.dp))
                Text("Вывод: ${when{ yData.VARIATION < 0.1 -> "незначительная"; yData.VARIATION < 0.2 -> "средняя" else -> "значительная" }} степень рассеивания результатов SAT теста")
            }
        }
        Column(

        )
        {
            Text("Задача 5", modifier = Modifier
                .padding(10.dp)
                .scale(1.5f)
                .align(Alignment.CenterHorizontally), color = Color.White
            )
            Divider()
            Spacer(Modifier.height(50.dp))
            PlotBox(modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.4f).height(500.dp), scaleHeight = 10f, elements = elementsX)
            Divider()
            Spacer(Modifier.height(50.dp))
            PlotBox(modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.4f).height(500.dp), scaleHeight = 1f, elements = elementsY)
        }
        Column()
        {
            Text("Задача 6", modifier = Modifier
                .padding(10.dp)
                .scale(1.5f)
                .align(Alignment.CenterHorizontally), color = Color.White)
                Row()
                {
                    Text("Хи набл: ", color = Color.Green, modifier = Modifier.width(150.dp))
                    Text("${yNormalChi2Values.first}", color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Row()
                {
                    Text("Хи крит: ", color = Color.Red, modifier = Modifier.width(150.dp))
                    Text("${yNormalChi2Values.second}", color = Color.Black, modifier = Modifier.width(100.dp))
                }
            Text("Вывод: ${
                if (yNormalChi2Values.run{first <= second}) 
                     "H0 принимается, результаты SAT теста - выборка с норм распределением"
                else "H0 отвергается, результаты SAT теста - выборка с распределением, отличным от нормального"
            }")
        }
        Column()
        {
            Text("Задача 7", modifier = Modifier
                .padding(10.dp)
                .scale(1.5f)
                .align(Alignment.CenterHorizontally), color = Color.White)
            Text("gamma = 95%; Y сред. лежит внутри:", color = Color.Yellow)
            Row(

            )
            {
                Text("[", color = Color.Black)
                Text("${yData.M}", color = Color.Green)
                Text(" - ", color = Color.Black)
                Text("${y95intervalDelta}", color = Color.Blue)
                Text(" ; ", color = Color.Black)
                Text("${yData.M}", color = Color.Green)
                Text(" + ", color = Color.Black)
                Text("${y95intervalDelta}", color = Color.Blue)
                Text("]", color = Color.Black)
            }
        }
        Column()
        {
            Text("Задача 8", modifier = Modifier
                .padding(10.dp)
                .scale(1.5f)
                .align(Alignment.CenterHorizontally), color = Color.White)
            Text("Выборка Y была разделена на 2 части(в 1 - нечетные, в другой - четные номера)")
            Row()
            {
                Text("F набл: ", color = Color.Blue)
                Text("${FObs}", color = Color.Green)
            }
            Row()
            {
                Text("F крит: ", color = Color.Blue)
                Text("${FCrit}", color = Color.Red)
            }
            Row()
            {
                Text("T набл: ", color = Color.Blue)
                Text("${TObs}", color = Color.Green)
            }
            Row()
            {
                Text("T крит: ", color = Color.Blue)
                Text("${TCrit}", color = Color.Red)
            }
            Text("Вывод: ${
                if (FObs <= FCrit) {
                    if (TObs <= TCrit){
                        "H0 принимается, средние  равны"
                    }
                    else{
                        "H0 отвергается, средние не равны"
                    }
                }
                else{
                    "нельзя сравнивать генеральные средние, т.к. генеральные дисперсии не равны"
                }
            }")
        }
    }
}

@Composable
fun CorrelationRegressionAnalyze(xElements: List<Double>, xIntervalData: Map<Pair<Double, Double>, List<Int>>, yElements: List<Double>, yIntervalData: Map<Pair<Double, Double>, List<Int>>)
{
    var vm = viewModel<MyViewModel>()
    var screenSize by remember{ mutableStateOf(IntSize(0, 0)) }
    var density = LocalDensity.current.density

    var xData = vm.getDistributionData(xElements, xIntervalData)
    var yData = vm.getDistributionData(yElements, yIntervalData)

    val MXY = xElements.zip(yElements).sumOf{it.first * it.second} / xElements.size
    val cov = (MXY - xData.M * yData.M) roundTo 2
    val rxy = (cov / sqrt(xData.D) / sqrt(yData.D)) roundTo 2
    val (TObs, TCrit) = vm.getCorrelationTResults(rxy, xElements.size, 0.00000000001)
    val Det = pow(rxy, 2.0) roundTo 2
    val b1 = (cov / xData.D) roundTo 2
    val b0 = (yData.M - b1 * xData.M) roundTo 2

    var TSS = yElements.sumOf{ pow(it - yData.M, 2.0) } roundTo 2
    var RSS = xElements.sumOf{ pow((b0 + b1 * it) - yData.M, 2.0) } roundTo 2
    var ESS = (TSS - RSS) roundTo 2

    println("TSS = ${TSS} RSS = ${RSS} ESS = ${ESS}")
    val S2e = 1.0 / (xElements.size - 2) * ESS
    println("s2e = ${S2e}")
    val S2b1 = S2e / xElements.size / xData.D
    val S2b0 = S2b1 * xData.M * xData.M

    val (TObsB0, TCritB0) = vm.getBiTResults(b0, sqrt(S2b0), xElements.size)
    val (TObsB1, TCritB1) = vm.getBiTResults(b1, sqrt(S2b1), xElements.size)
    val B0Delta = (sqrt(S2b0) * TDistribution(xElements.size.toDouble()).inverseCumulativeProbability(0.9995)) roundTo 2
    val B1Delta = (sqrt(S2b1) * TDistribution(xElements.size.toDouble()).inverseCumulativeProbability(0.9995)) roundTo 2

    val (FObs, FCrit) = vm.getCorrelationEquationFResults(RSS, ESS, 2, xElements.size - 2 - 1)

    val test1Result = TestUtils.homoscedasticT(xElements.toDoubleArray(), yElements.toDoubleArray())
    val test2Result = TestUtils.homoscedasticTTest(xElements.toDoubleArray(), yElements.toDoubleArray())
    val x145 = xData.M * 1.45
    val correspondingYFantazy = b0 + b1 * x145
    val b0MinY = vm.getMinYForBi(b0, sqrt(S2b0), xElements.size)
    val b1MinY = vm.getMinYForBi(b1, sqrt(S2b1), xElements.size)

    Column(
        modifier = Modifier
            .onSizeChanged { screenSize = it }
            .background(Color.DarkGray)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    )
    {
        var canvasSize by remember {
            mutableStateOf(IntSize(0, 0))
        }
        Spacer(Modifier.height(40.dp))
        Row(modifier = Modifier
            .fillMaxWidth()
            .height((screenSize.height / density * 0.7f).dp)
        )
        {
            Spacer(Modifier.width(20.dp))
            Canvas(
                modifier = Modifier
                    .onSizeChanged { canvasSize = it }
                    .fillMaxSize(),
                onDraw = {
                    val textPaintAxis = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        textSize = 24.sp.toPx()
                        color = android.graphics.Color.BLUE
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

                    }
                    val textPaintNumbers = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        textSize = 12.sp.toPx()
                        color = android.graphics.Color.YELLOW
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    }

                    //oX line
                    drawLine(
                        Color.White,
                        Offset(0f, canvasSize.height.toFloat()),
                        Offset(canvasSize.width.toFloat(), canvasSize.height.toFloat()),
                        strokeWidth = 6f
                    )
                    //oX triangle arrow
                    drawPath(
                        Path().apply{
                            this.moveTo(canvasSize.width.toFloat() - 50f, canvasSize.height.toFloat() + 10f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, 0f, -20f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, 50f, 10f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, -50f, 10f)
                        },
                        Color.White
                    )
                    //oX value lines
                    repeat(10){
                        drawLine(
                            Color.Black,
                            Offset((it + 1f) / 11 * canvasSize.width, canvasSize.height + 20f),
                            Offset((it + 1f) / 11 * canvasSize.width, canvasSize.height - 20f),
                            10f
                        )
                        drawLine(
                            Color.Green,
                            Offset((it + 1f) / 11 * canvasSize.width, 0f),
                            Offset((it + 1f) / 11 * canvasSize.width, canvasSize.height - 40f),
                            4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f))
                        )
                        rotate(30f, Offset((it + 1f) / 11 * canvasSize.width, canvasSize.height + 50f))
                        {
                            drawIntoCanvas { cnv ->
                                cnv.nativeCanvas.drawText(
                                    "${((it + 1) * 16)}",
                                    (it + 1f) / 11 * canvasSize.width,
                                    canvasSize.height + 50f,
                                    textPaintNumbers
                                )
                            }
                        }
                    }
                    //oX text
                    drawIntoCanvas { cnv ->
                        cnv.nativeCanvas.drawText(
                            "IQ",
                            canvasSize.width - 80f,            // x-coordinates of the origin (top left)
                            canvasSize.height - 50f, // y-coordinates of the origin (top left)
                            textPaintAxis
                        )
                    }

                    //oY line
                    drawLine(
                        Color.White,
                        Offset(0f, canvasSize.height.toFloat()),
                        Offset(0f, 0f),
                        strokeWidth = 6f
                    )
                    //oY triangle arrow
                    drawPath(
                        Path().apply{
                            this.moveTo(-10f, 50f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, 10f, -50f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, 10f, 50f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, -20f, 0f)
                        },
                        Color.White
                    )
                    //oY value lines
                    repeat(10){
                        drawLine(
                            Color.Black,
                            Offset(-20f, (10f - it) / 11 * canvasSize.height),
                            Offset(20f, (10f - it) / 11 * canvasSize.height),
                            10f
                        )
                        drawLine(
                            Color.Green,
                            Offset(30f, (10f - it) / 11 * canvasSize.height),
                            Offset(canvasSize.width.toFloat(), (10f - it) / 11 * canvasSize.height),
                            4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f))
                        )
                        rotate(0f, Offset(30f, (10f - it) / 11 * canvasSize.height - 10f))
                        {
                            drawIntoCanvas { cnv ->
                                cnv.nativeCanvas.drawText(
                                    "${((it + 1) * 180)}",
                                    30f,
                                    (10f - it) / 11 * canvasSize.height - 10f,
                                    textPaintNumbers
                                )
                            }
                        }
                    }
                    //oY text
                    drawIntoCanvas { cnv ->
                        cnv.nativeCanvas.drawText(
                            "SAT",
                            30f,            // x-coordinates of the origin (top left)
                            70f, // y-coordinates of the origin (top left)
                            textPaintAxis
                        )
                    }
                    drawLine(
                        object : ShaderBrush(){
                            override fun createShader(size: Size): Shader {
                                return LinearGradientShader(Offset(0f, 0f), Offset(size.width, size.height), listOf(Color.Green, Color.Red))
                            }
                        },
                        if (b0 > 0)
                            Offset(0f, (canvasSize.height - b0 / 1800f * 10f / 11f * canvasSize.height).toFloat())
                        else
                            Offset((-b0/b1 / 160f * 10f / 11f * canvasSize.width).toFloat(), canvasSize.height.toFloat()),
                        Offset(canvasSize.width.toFloat() * 10f / 11f, (canvasSize.height - (b0 + b1 * 160) / 1800f * 10f / 11f * canvasSize.height).toFloat()),
                        7f
                    )
                    vm.IQDataset.zip(vm.SATDataset).forEach{ (iq, sat) ->
                        drawCircle(
                            Color.Magenta,
                            10f,
                            Offset(iq / 160f * canvasSize.width * 10f / 11f, canvasSize.height - sat / 1800f * canvasSize.height * 10f / 11f)
                        )
                    }
                    drawRoundRect(
                        Color.Gray,
                        Offset(180f, 20f),
                        Size(canvasSize.width - 180f - 20f, 140f),
                        CornerRadius(10f, 8f)
                    )
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(180f, 20f),
                        size = Size(canvasSize.width - 180f - 20f, 140f),
                        cornerRadius = CornerRadius(10f, 8f),
                        style = Stroke(4f)
                    )
                    drawIntoCanvas { cnv ->
                        cnv.nativeCanvas.drawText(
                            "R^2 = ${Det}",
                            200f,            // x-coordinates of the origin (top left)
                            140f, // y-coordinates of the origin (top left)
                            textPaintAxis.apply{this.color = android.graphics.Color.RED}
                        )
                    }
                    drawIntoCanvas { cnv ->
                        cnv.nativeCanvas.drawText(
                            "SAT = ${b0} + ${b1} * IQ",
                            200f,            // x-coordinates of the origin (top left)
                            70f, // y-coordinates of the origin (top left)
                            textPaintAxis.apply{this.color = android.graphics.Color.RED; this.textSize = 18.sp.toPx()}
                        )
                    }
                }
            )
        }
        Spacer(Modifier.height(100.dp))
        Row(modifier = Modifier
            .fillMaxWidth()
            .height((screenSize.height / density * 0.7f).dp)
        )
        {
            Spacer(Modifier.width(20.dp))
            Canvas(
                modifier = Modifier
                    .onSizeChanged { canvasSize = it }
                    .fillMaxSize(),
                onDraw = {
                    val textPaintAxis = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        textSize = 24.sp.toPx()
                        color = android.graphics.Color.BLUE
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

                    }
                    val textPaintNumbers = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        textSize = 12.sp.toPx()
                        color = android.graphics.Color.YELLOW
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    }

                    //oX line
                    drawLine(
                        Color.White,
                        Offset(0f, canvasSize.height.toFloat()),
                        Offset(canvasSize.width.toFloat(), canvasSize.height.toFloat()),
                        strokeWidth = 6f
                    )
                    //oX triangle arrow
                    drawPath(
                        Path().apply{
                            this.moveTo(canvasSize.width.toFloat() - 50f, canvasSize.height.toFloat() + 10f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, 0f, -20f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, 50f, 10f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, -50f, 10f)
                        },
                        Color.White
                    )
                    //oX value lines
                    repeat(10){
                        drawLine(
                            Color.Black,
                            Offset((it + 1f) / 11 * canvasSize.width, canvasSize.height + 20f),
                            Offset((it + 1f) / 11 * canvasSize.width, canvasSize.height - 20f),
                            10f
                        )
                        drawLine(
                            Color.Green,
                            Offset((it + 1f) / 11 * canvasSize.width, 0f),
                            Offset((it + 1f) / 11 * canvasSize.width, canvasSize.height - 40f),
                            4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f))
                        )
                        rotate(30f, Offset((it + 1f) / 11 * canvasSize.width, canvasSize.height + 50f))
                        {
                            drawIntoCanvas { cnv ->
                                cnv.nativeCanvas.drawText(
                                    "${((it + 1) * 16)}",
                                    (it + 1f) / 11 * canvasSize.width,
                                    canvasSize.height + 50f,
                                    textPaintNumbers
                                )
                            }
                        }
                    }
                    //oX text
                    drawIntoCanvas { cnv ->
                        cnv.nativeCanvas.drawText(
                            "IQ",
                            canvasSize.width - 80f,            // x-coordinates of the origin (top left)
                            canvasSize.height - 50f, // y-coordinates of the origin (top left)
                            textPaintAxis
                        )
                    }

                    //oY line
                    drawLine(
                        Color.White,
                        Offset(0f, canvasSize.height.toFloat()),
                        Offset(0f, 0f),
                        strokeWidth = 6f
                    )
                    //oY triangle arrow
                    drawPath(
                        Path().apply{
                            this.moveTo(-10f, 50f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, 10f, -50f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, 10f, 50f)
                            this.relativeCubicTo(0f, 0f, 0f, 0f, -20f, 0f)
                        },
                        Color.White
                    )
                    //oY value lines
                    repeat(10){
                        drawLine(
                            Color.Black,
                            Offset(-20f, (10f - it) / 11 * canvasSize.height),
                            Offset(20f, (10f - it) / 11 * canvasSize.height),
                            10f
                        )
                        drawLine(
                            Color.Green,
                            Offset(30f, (10f - it) / 11 * canvasSize.height),
                            Offset(canvasSize.width.toFloat(), (10f - it) / 11 * canvasSize.height),
                            4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f))
                        )
                        rotate(0f, Offset(30f, (10f - it) / 11 * canvasSize.height - 10f))
                        {
                            drawIntoCanvas { cnv ->
                                cnv.nativeCanvas.drawText(
                                    "${((it + 1) * 180)}",
                                    30f,
                                    (10f - it) / 11 * canvasSize.height - 10f,
                                    textPaintNumbers
                                )
                            }
                        }
                    }
                    //oY text
                    drawIntoCanvas { cnv ->
                        cnv.nativeCanvas.drawText(
                            "|ERROR|",
                            30f,            // x-coordinates of the origin (top left)
                            70f, // y-coordinates of the origin (top left)
                            textPaintAxis
                        )
                    }
                    vm.IQDataset.zip(vm.SATDataset).forEach{ (iq, sat) ->
                        drawCircle(
                            Color.Red,
                            10f,
                            Offset(iq / 160f * canvasSize.width * 10f / 11f, canvasSize.height - abs(sat - (b0 + b1 * iq)).toFloat() / 1800f * canvasSize.height * 10f / 11f)
                        )
                    }
                }
            )
        }
        Spacer(Modifier.height(100.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(50.dp)
        )
        {
            Column(

            )
            {
                Text("Задача 9", modifier = Modifier
                    .padding(10.dp)
                    .scale(1.5f)
                    .align(Alignment.CenterHorizontally), color = Color.White
                )
                Row(

                )
                {
                    Text("y = ", color = Color.Black)
                    Text(b0.toString(), color = Color.Yellow)
                    Text(" + ", color = Color.Black)
                    Text(b1.toString(), color = Color.Green)
                    Text(" * x")
                }
                Row(

                )
                {
                    Text("R^2: ", color = Color.Blue, modifier = Modifier.width(150.dp))
                    Text(Det.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
            }
            Column(

            )
            {
                Text("Задача 10", modifier = Modifier
                    .padding(10.dp)
                    .scale(1.5f)
                    .align(Alignment.CenterHorizontally), color = Color.White)
                Row(
                )
                {
                    Text("cov: ", color = Color.Blue, modifier = Modifier.width(150.dp))
                    Text(cov.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Row(

                )
                {
                    Text("rxy: ", color = Color.Blue, modifier = Modifier.width(150.dp))
                    Text(rxy.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Row(

                )
                {
                    Text("T набл: ", color = Color.Green, modifier = Modifier.width(150.dp))
                    Text(TObs.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Row(

                )
                {
                    Text("T крит: ", color = Color.Red, modifier = Modifier.width(150.dp))
                    Text(TCrit.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Row(

                )
                {
                    Text("Вывод: ${if (TObs <= TCrit) "оценка корреляции значима" else "нельзя сказать о полученной корреляции"}", color = Color.Blue)
                }
                Text("Если оценка значима, то связь при корреляции\n0-0.3 значит \"очень слабая\"\n0.3-0.5 значит \"слабая\"\n0.5-0.7 значит \"средняя\"\n0.7-0.9 значит \"высокая\"\n0.9-1 значит \"очень высокая\"\n1 - \"функциональная\"")
            }
            Column(

            )
            {
                Text("Задача 11", modifier = Modifier
                    .padding(10.dp)
                    .scale(1.5f)
                    .align(Alignment.CenterHorizontally), color = Color.White)
                Row(

                )
                {
                    Text("T набл (b0): ", color = Color.Green, modifier = Modifier.width(150.dp))
                    Text(TObsB0.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Row(

                )
                {
                    Text("T крит (b0): ", color = Color.Red, modifier = Modifier.width(150.dp))
                    Text(TCritB0.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Text("Вывод: ${if (abs(TObsB0) < TCritB0) "оценка bo не значима" else "оценка b0 значима"}")
                Row(

                )
                {
                    Text("T набл (b1): ", color = Color.Green, modifier = Modifier.width(150.dp))
                    Text(TObsB1.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Row(

                )
                {
                    Text("T крит (b1): ", color = Color.Red, modifier = Modifier.width(150.dp))
                    Text(TCritB1.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Text("b0 не значима, так как зачастую < 0, но если > 0, то при iq = 0(невозможно) человек наберет ${b0} баллов SAT")
                Text("Вывод: ${if (abs(TObsB1) < TCritB1) "оценка b1 не значима" else "оценка b1 значима"}")
                Text("значение b1: при возрастании iq человека на 1, результаты SAT теста в среднем поднимутся на ${b1}")
            }
            Column(

            )
            {
                Text("Задача 12", modifier = Modifier
                    .padding(10.dp)
                    .scale(1.5f)
                    .align(Alignment.CenterHorizontally), color = Color.White)
                Row(

                )
                {
                    Text("F набл (для эконометр. модели): ", color = Color.Green, modifier = Modifier.width(150.dp))
                    Text(FObs.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Row(

                )
                {
                    Text("F крит (для эконометр. модели): ", color = Color.Red, modifier = Modifier.width(150.dp))
                    Text(FCrit.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Text("Вывод: ${if (FObs >= FCrit) "модель значима в целом" else "нельзя сказать о значимости модели"} ")
            }
            Column(

            )
            {
                Text("Задача 13", modifier = Modifier
                    .padding(10.dp)
                    .scale(1.5f)
                    .align(Alignment.CenterHorizontally), color = Color.White)
                Row(

                )
                {
                    Text("TSS: ", color = Color.Red, modifier = Modifier.width(150.dp))
                    Text(TSS.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Row(

                )
                {
                    Text("RSS: ", color = Color.Red, modifier = Modifier.width(150.dp))
                    Text(RSS.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Row(

                )
                {
                    Text("ESS: ", color = Color.Red, modifier = Modifier.width(150.dp))
                    Text(ESS.toString(), color = Color.Black, modifier = Modifier.width(100.dp))
                }
                Text("R^2 отвечает за значимость данной корреляционной модели (Oт -inf до 1), в данной модели SAT зависит от IQ на ${Det}, и от других факторов на ${1 - Det}")
            }
            Column(

            )
            {
                Text("Задача 15", modifier = Modifier
                    .padding(10.dp)
                    .scale(1.5f)
                    .align(Alignment.CenterHorizontally), color = Color.White)
                Text("y = 99%; b0 принадлежит интервалу:", color = Color.White)
                Row(

                )
                {
                    Text("[", color = Color.Black)
                    Text("${b0}", color = Color.Green)
                    Text(" - ", color = Color.Black)
                    Text("${B0Delta}", color = Color.Blue)
                    Text(" ; ", color = Color.Black)
                    Text("${b0}", color = Color.Green)
                    Text(" + ", color = Color.Black)
                    Text("${B0Delta}", color = Color.Blue)
                    Text("]", color = Color.Black)
                }
                Text("y = 99%; b1 принадлежит интервалу:", color = Color.White)
                Row(

                )
                {
                    Text("[", color = Color.Black)
                    Text("${b1}", color = Color.Green)
                    Text(" - ", color = Color.Black)
                    Text("${B1Delta}", color = Color.Blue)
                    Text(" ; ", color = Color.Black)
                    Text("${b1}", color = Color.Green)
                    Text(" + ", color = Color.Black)
                    Text("${B1Delta}", color = Color.Blue)
                    Text("]", color = Color.Black)
                }
            }
            Column(

            )
            {
                Text("Задача 16",
                    modifier = Modifier
                        .padding(10.dp)
                        .scale(1.5f)
                        .align(Alignment.CenterHorizontally),
                    color = Color.White
                )
                Text("Встроенный тест 1: ${test1Result roundTo 2}")
                Text("Встроенный тест 2: ${test2Result roundTo 2}")
                Text("По графику остатков видно, что остатки не расходятся, и не сходятся, следовательно наблюдается")
                Text("Гомоскедастичность", color = Color.Green)
            }
            Column(

            )
            {
                Text("Задача 17",
                    modifier = Modifier
                        .padding(10.dp)
                        .scale(1.5f)
                        .align(Alignment.CenterHorizontally),
                    color = Color.White
                )
                Text("Максимальная Y (b0): ${b0MinY}")
                Text("Максимальная Y (b1): ${b1MinY}")
                Row()
                {
                    Text("Значение X, превыщающее на 45% среднее: ")
                    Text("${x145 roundTo 2}", color = Color.Yellow)
                }
                Row()
                {
                    Text("Соответствующее значение Y, посчитанное: ")
                    Text("${correspondingYFantazy roundTo 2}", color = Color.Yellow)
                }
                Text("Смысл: Если iq = ${x145 roundTo 2}, то, скорее всего, участник наберет ${correspondingYFantazy roundTo 2} баллов SAT")
            }
        }
    }

}

@Composable
fun App(vm: MyViewModel = viewModel()){
    Column(
        modifier = Modifier.fillMaxSize()
    )
    {
        var navController = rememberNavController().also{vm.mainNavController = it}
        NavHost(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            navController = navController,
            startDestination = "Графики"
        )
        {
            vm.navElements.forEach { elem ->
                composable(
                    route = elem.text
                )
                {
                    elem.content()
                }
            }
        }
        Row(
            modifier = Modifier
                .background(
                    object : ShaderBrush() {
                        override fun createShader(size: Size): Shader {
                            return LinearGradientShader(
                                Offset(0f, 0f),
                                Offset(size.width, size.height),
                                listOf(Color.Gray, Color.DarkGray)
                            )
                        }
                    }
                )
                .fillMaxWidth()
                .height(100.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceEvenly
        )
        {
            vm.navElements.forEach { elem ->
                var offs by remember{mutableStateOf(Offset(0f, 0f))}
                var coroutineScope = rememberCoroutineScope()
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            coroutineScope.launch {
                                launch {
                                    detectTapGestures {
                                        if (elem.text != vm.currentContent) navController.navigate(
                                            elem.text
                                        )
                                        vm.currentContent = elem.text
                                    }
                                }
                                launch {
                                    detectDragGestures { change, dragAmount ->
                                        offs += dragAmount
                                    }
                                }
                            }
                        }
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .size(70.dp, 90.dp),
                    contentAlignment = Alignment.Center
                )
                {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(id = elem.image ?: R.drawable.ic_launcher_background),
                        contentDescription = null
                    )
                    Text(elem.text, color = if (elem.text == vm.currentContent) Color.Magenta else Color.Yellow)
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
