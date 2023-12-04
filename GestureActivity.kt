package com.example.project_10

import android.content.res.Configuration
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.project_10.ui.theme.Project_10Theme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

val motions = mutableStateOf(List(5) { "" }) //STORE ALL RECORDED ACTIONS HERE

class GestureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Project_10Theme {
                MainContainer()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setContent {
            Project_10Theme {
                MainContainer()
            }
        }
    }
}

@Composable
fun MainContainer() {

    val configuration = LocalConfiguration.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) { //if landscape, landscape layout. else, portrait
            LandscapeLayout()
        } else {
            PortraitLayout()
        }
    }
}

@Composable
fun LandscapeLayout() {
    Row(modifier = Modifier.fillMaxSize()) {//use weight to make boxes equal
        TopSection(modifier = Modifier.weight(1f))
        BottomSection(modifier = Modifier.weight(1f))
    }
}

@Composable
fun PortraitLayout() {
    Column(modifier = Modifier.fillMaxSize()) {//use weight to make boxes equal
        TopSection(modifier = Modifier.weight(1f))
        BottomSection(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TopSection(modifier: Modifier = Modifier) {
    var containerSize by remember { mutableStateOf(Size.Zero) }
    var ballSize by remember { mutableStateOf(Size.Zero) }

    var ballMatrix by remember { mutableStateOf(Matrix()) }
    var startPoint by remember { mutableStateOf(PointF()) }
    var startMovement by remember { mutableStateOf(PointF())}
    var lastTapped by remember {mutableStateOf(false)}

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                // Update container size
                containerSize = coordinates.size.toSize()
            }
            .pointerInteropFilter { motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startPoint = PointF(motionEvent.x, motionEvent.y)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = motionEvent.x - startPoint.x
                        val dy = motionEvent.y - startPoint.y

                        val newMatrix = Matrix(ballMatrix).apply {
                            postTranslate(dx, dy)
                        }
                        ballMatrix = checkAndAdjustPosition(newMatrix, containerSize, ballSize)
                        startPoint = PointF(motionEvent.x, motionEvent.y)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> { //on tap moving
                        val dx = motionEvent.x - startPoint.x
                        val dy = motionEvent.y - startPoint.y

                        startPoint = PointF(motionEvent.x, motionEvent.y) //new start point

                        // apply translation to the ball
                        val newMatrix = Matrix(ballMatrix).apply {
                            postTranslate(dx, dy)
                        }
                        //update matrix
                        ballMatrix = newMatrix

                        true//placeholder
                    }

                    MotionEvent.ACTION_UP -> { // on tap release
                        val dx = motionEvent.x - startMovement.x
                        val dy = motionEvent.y - startMovement.y

                        // calculate the distance moved
                        val distance = hypot(dx, dy)

                        // set a threshold to consider it a swipe
                        val threshold = 5f

                        if (distance > threshold) {
                            lastTapped = false
                            // determine the direction of the swipe
                            val angle = atan2(dy, dx)
                            val direction = when {
                                abs(angle) < PI / 4 -> "right"
                                abs(angle) > 3 * PI / 4 -> "left"
                                angle > 0 -> "down"
                                else -> "up"
                            }

                            val openIndex = motions.value.indexOfFirst { it.isEmpty() }

                            if (openIndex != -1) {
                                // update the open index with the direction
                                motions.value = motions.value
                                    .toMutableList()
                                    .apply {
                                        set(openIndex, "You swiped $direction")
                                    }
                            } else {
                                // if all index are filled, reset the entire array
                                motions.value = List(5) { "" }
                                motions.value = motions.value
                                    .toMutableList()
                                    .apply {
                                        set(0, "You swiped $direction")
                                    }
                            }

                        } else {
                            if (lastTapped) { //if last movement was tap, double tap detected.
                                val openIndex = motions.value.indexOfFirst { it.isEmpty() }

                                if (openIndex != -1) {
                                    // update the open index with the direction
                                    motions.value = motions.value
                                        .toMutableList()
                                        .apply {
                                            set(openIndex, "You double tapped")
                                        }
                                } else {
                                    // if all index are filled, reset the entire array
                                    motions.value = List(5) { "" }
                                    motions.value = motions.value
                                        .toMutableList()
                                        .apply {
                                            set(0, "You double tapped")
                                        }
                                }
                                lastTapped = false //set to false after double tap found
                            } else {
                                lastTapped = true //else it was the first double tap, do nothing
                            }
                        }

                        true//placeholder
                    }

                    else -> false//placeholder
                }
            }
    ) {
        val matrixValues = FloatArray(9)
        ballMatrix.getValues(matrixValues)
        val offsetX = matrixValues[Matrix.MTRANS_X]
        val offsetY = matrixValues[Matrix.MTRANS_Y]

        //ball image
        Image(
            painter = painterResource(id = R.drawable.ball),
            contentDescription = "Red Ball",
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    // Update ball size
                    ballSize = coordinates.size.toSize()
                }
                .graphicsLayer {
                    val values = FloatArray(9)
                    ballMatrix.getValues(values)
                    translationX = values[Matrix.MTRANS_X]
                    translationY = values[Matrix.MTRANS_Y]
                }
        )
    }
}

private fun checkAndAdjustPosition(matrix: Matrix, containerSize: Size, ballSize: Size): Matrix {
    val values = FloatArray(9)
    matrix.getValues(values)
    val x = values[Matrix.MTRANS_X]
    val y = values[Matrix.MTRANS_Y]

    // Calculate the boundaries
    val leftBound = 0f
    val rightBound = containerSize.width - ballSize.width
    val topBound = 0f
    val bottomBound = containerSize.height - ballSize.height

    // Adjust the position to stay within the boundaries
    values[Matrix.MTRANS_X] = x.coerceIn(leftBound, rightBound)
    values[Matrix.MTRANS_Y] = y.coerceIn(topBound, bottomBound)

    return Matrix().apply { setValues(values) }
}

@Composable
fun BottomSection(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            motions.value.forEachIndexed { index, action -> //for each index, create box, text.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // equal weight for all
                        .padding(8.dp)
                        .background(Color(0xFFCCCCCC)),
                    contentAlignment = Alignment.CenterStart // align actions to the left, center vertically
                ) {
                    Text (
                        text = action,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        textAlign = TextAlign.Left,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

//preview composable
@Preview(showBackground = true)
@Composable
fun MainContainerPreview() {
    Project_10Theme {
        MainContainer()
    }
}
