package com.example.individualassignment_31

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.individualassignment_31.ui.theme.IndividualAssignment_31Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FullScreenImage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //For a smoother appearance, I make the activity transition a fade in/out
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //extract all the intent information
        val imageID = intent.getIntExtra("imageID", 0)
        val positionX = intent.getFloatExtra("positionX", 0f)
        val positionY = intent.getFloatExtra("positionY", 0f)
        var description = intent.getStringExtra("description")
        var width = intent.getIntExtra("width", 0)
        var height = intent.getIntExtra("height", 0)
        if(description == null){ description = "wait, this shouldn't be here" }
        setContent {
            IndividualAssignment_31Theme {
                GrowImage(imageID, positionX, positionY, description, width, height)
            }
        }
    }
}

//I found this code on stack overflow, I admit it. It's a function to
//identify the current activity. I need this to be able to end the activity.
fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

//The main function to display the image and its animations.
@Composable
fun GrowImage(
    imageID: Int,
    positionX: Float,
    positionY: Float,
    description: String,
    width: Int,
    height: Int
){
    Scaffold() {innerPadding ->
        val context = LocalContext.current
        val density = LocalDensity.current
        val coroutineScope = rememberCoroutineScope()
        //obtain the screen size for use in animating the growing image.
        val screenWidth = with(density) {
            context.resources.displayMetrics.widthPixels.toDp().value.toInt()
        }
        val screenHeight = with(density) {
            context.resources.displayMetrics.heightPixels.toDp().value.toInt()
        }
        //A set of width/height and x/y mutable states used by the position and size animations.
        var animWidth by remember { mutableStateOf(width) }
        var animHeight by remember { mutableStateOf(height) }
        var animCoordX by remember { mutableStateOf(positionX) }
        var animCoordY by remember { mutableStateOf(positionY) }

        //The animations of the image growing/shrinking based on the value of the relevant mutable states.
        val growWidthAnimation by animateIntAsState(
            targetValue = animWidth,
            animationSpec = tween(durationMillis = 250), label = "grow width"
        )
        val growHeightAnimation by animateIntAsState(
            targetValue = animHeight,
            animationSpec = tween(durationMillis = 250), label = "grow height"
        )
        //The animations for the image shifting location based on the value of the relevant
        //mutable states. These are needed since images start hard-coded to their previous
        //position and need to shift to (0,0) to not be off-center.
        val shiftXAnimation by animateIntAsState(
            targetValue = animCoordX.toInt(),
            animationSpec = tween(durationMillis = 250), label = "shift x"
        )
        val shiftYAnimation by animateIntAsState(
            targetValue = animCoordY.toInt(),
            animationSpec = tween(durationMillis = 250), label = "shift y"
        )

        val painter = painterResource(imageID)

        //on launch, change the image size and position states to the grown iterations.
        LaunchedEffect(Unit) {
            //delay(250)
            animWidth = screenWidth
            animHeight = screenHeight
            animCoordX = 0f
            animCoordY = 0f
        }

        Image(
            painter = painter,
            contentDescription = description,
            modifier = Modifier
                .padding(innerPadding)
                .offset(shiftXAnimation.dp, shiftYAnimation.dp)
                .size(width = growWidthAnimation.dp, height = growHeightAnimation.dp)
                .clickable {
                    //if the image is clicked again, animate the image shrinking and returning
                    //to its original size and position.
                    coroutineScope.launch(Dispatchers.Main) {
                        //delay(500)
                        animWidth = width
                        animHeight = height
                        animCoordX = positionX
                        animCoordY = positionY
                        //delay(250)
                        //After this, end the activity.
                        context.findActivity()!!.finish()
                    }
                }
        )

    }
}
