package com.example.individualassignment_31

import android.content.res.XmlResourceParser
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.individualassignment_31.ui.theme.IndividualAssignment_31Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import java.util.Vector

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val photoParser = resources.getXml(R.xml.photos)
        setContent {
            IndividualAssignment_31Theme {
                val photoInfo = readPhotos(photoParser)
                makeScreen(photoInfo)
            }
        }
    }
}

@Composable
fun readPhotos(parser: XmlResourceParser): Vector<Pair<String, String>> {
    val photoInfo = Vector<Pair<String, String>>()
    var title = ""
    var file = ""
    while (parser.eventType != XmlPullParser.END_DOCUMENT) {
        if (parser.eventType == XmlPullParser.START_TAG) {
            when(parser.name) {
                "title" -> title = parser.nextText()
                "file" -> {
                    file = parser.nextText()
                    photoInfo.add(Pair(title, file))
                }
            }
        }
        parser.next()
    }
    return photoInfo
}

@Composable
fun makeImage(title: String, file: String, colNum: Int) {
    val coroutineScope =  rememberCoroutineScope()
    val context = LocalContext.current
    val painter = painterResource(
        context.resources.getIdentifier(file, "drawable", context.packageName)
    )
    var imageSize by remember { mutableStateOf(140) }

    val sz by animateIntAsState(
        targetValue = imageSize,
        animationSpec = tween(durationMillis = 500), label = "grow"
    )

    Image(
        painter = painter,
        contentDescription = title,
        modifier = Modifier
            .aspectRatio(ratio = painter.intrinsicSize.width /
                    painter.intrinsicSize.height)
            //.fillMaxWidth()
            .width(sz.dp)
            .clickable {
                coroutineScope.launch(Dispatchers.Main) {
                    imageSize = if(imageSize == 300) 140 else 300
                    //colNum = if(colNum == 2) 1 else 2
                }
            }
    )
}

@Composable
fun makeScreen(photoInfo: Vector<Pair<String, String>>){
    var bigImages = Array(photoInfo.size){ mutableStateOf(false)}

    Scaffold() {innerPadding ->
        LazyVerticalStaggeredGrid(
            userScrollEnabled = true,
            //columns = StaggeredGridCells.Fixed(2),
            columns = StaggeredGridCells.Adaptive(200.dp),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(innerPadding),
            content = {
                items(photoInfo.size,
                    //span = {StaggeredGridItemSpan.SingleLane},
                    //span = { StaggeredGridItemSpan.FullLine },
                    span = {i ->
                        if(bigImages[i].value) {
                            StaggeredGridItemSpan.FullLine
                        } else {
                            StaggeredGridItemSpan.SingleLane
                        }
                    }
                    ){i ->
                    //makeImage(photoInfo[i].first, photoInfo[i].second, colNum)
                    val title = photoInfo[i].first
                    val file = photoInfo[i].second
                    val coroutineScope =  rememberCoroutineScope()
                    val context = LocalContext.current
                    val painter = painterResource(
                        context.resources.getIdentifier(file, "drawable", context.packageName)
                    )
                    var imageSize by remember { mutableStateOf(200) }

                    val sizeAnimate by animateDpAsState(
                        targetValue = imageSize.dp,
                        animationSpec = tween(durationMillis = 500), label = "grow"
                    )

                    val aspectRatio = painter.intrinsicSize.height / painter.intrinsicSize.width

                    Image(
                        painter = painter,
                        contentDescription = title,
                        modifier = Modifier
//                            .aspectRatio(ratio = painter.intrinsicSize.width /
//                                    painter.intrinsicSize.height)
                            //.fillMaxWidth()
                            .height(sizeAnimate * aspectRatio)
                            .width(sizeAnimate)
                            .clickable {
                                coroutineScope.launch(Dispatchers.Main) {
                                    if(bigImages[i].value) {
                                        imageSize = 200
                                        delay(1000)
                                        bigImages[i].value = false
                                    } else {
                                        bigImages[i].value = true
                                        for(ind in 0..bigImages.size-1) {
                                            if (ind != i) {
                                                bigImages[ind].value = false
                                            }
                                        }
                                        delay(1000)
                                        imageSize = 400
                                    }
                                }
                            }
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IndividualAssignment_31Theme {
        //makeScreen()
    }
}