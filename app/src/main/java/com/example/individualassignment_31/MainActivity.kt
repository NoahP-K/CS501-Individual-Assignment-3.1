package com.example.individualassignment_31

import android.content.Intent
import android.content.res.XmlResourceParser
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
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
        //I need a parser object to read the photo xml file
        val photoParser = resources.getXml(R.xml.photos)
        setContent {
            IndividualAssignment_31Theme {
                val photoInfo = readPhotos(photoParser)
                makeScreen(photoInfo)
            }
        }
    }
}

//a function to read the photo xml file given the parser and return a collection of pairs of
//image data
@Composable
fun readPhotos(parser: XmlResourceParser): Vector<Pair<String, String>> {
    val photoInfo = Vector<Pair<String, String>>()
    var title = ""
    var file = ""
    while (parser.eventType != XmlPullParser.END_DOCUMENT) {
        if (parser.eventType == XmlPullParser.START_TAG) {
            when(parser.name) {
                //the data is stored in pairs of title then file. Once the file is read,
                //the respective pair must be complete so it adds it to the list.
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

//the main function to display the screen
@Composable
fun makeScreen(photoInfo: Vector<Pair<String, String>>){
    val context = LocalContext.current
    val coroutineScope =  rememberCoroutineScope()
    val density = LocalDensity.current
    Scaffold() {innerPadding ->
        val scrollState = LazyStaggeredGridState()
        LazyVerticalStaggeredGrid(
            state = scrollState,
            userScrollEnabled = true,
            columns = StaggeredGridCells.Fixed(2),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(innerPadding),
            content = {
                items(photoInfo.size){i ->
                    val title = photoInfo[i].first
                    val file = photoInfo[i].second
                    //Here I extract the image id based on the string file name
                    //for use in creating a painter resource.
                    val id = context.resources.getIdentifier(file, "drawable", context.packageName)
                    val painter = painterResource(id)
                    var pos by remember { mutableStateOf(Offset.Zero)}  //initialize a position object
                    var size = IntSize(0, 0)    //initialize a size object
                    Image(
                        painter = painter,
                        contentDescription = title,
                        modifier = Modifier
                            //the aspect ratio ensures the image size is always proportional
                            .aspectRatio(ratio = painter.intrinsicSize.width /
                                    painter.intrinsicSize.height)
                            .onGloballyPositioned {coords ->
                                /*
                                Okay there's a lot happening here. So: I need the image's position
                                and size so that I can mimic it in the next activity.
                                I can get the raw values in px units with coords.boundsInWindow() and
                                coords.size, respectively. However, I need these in units of dp. So
                                I capture the local screen density and, with that, convert the values
                                to dp and then do whatever other conversions are needed.
                                 */
                                val windowCoords = coords.boundsInRoot().topLeft
                                val px = with(density) { windowCoords.x.toDp() }
                                val py = with(density) { windowCoords.y.toDp() }
                                pos = Offset(px.value, py.value)
                                size = IntSize(
                                    width = with(density) {
                                        coords.size.width.toDp().value.toInt()
                                                          },
                                    height = with(density) {
                                        coords.size.height.toDp().value.toInt()
                                    }
                                )
                            }
                            .clickable {
                                coroutineScope.launch(Dispatchers.Main) {
                                    scrollState.animateScrollToItem(i)  //start by scrolling to the item to make sure it is fully in the window
                                    //Here I create an intent for the next activity so it has all the info about the
                                    //image. It needs this to recreate the same exact image.
                                    val intent = Intent(context, FullScreenImage::class.java).apply {
                                        putExtra("imageID", id)
                                        putExtra("positionX", pos.x)
                                        putExtra("positionY", pos.y)
                                        putExtra("description", title)
                                        putExtra("width", size.width)
                                        //I noticed that, for some reason, the image position was
                                        //set to be slightly lower down that it was in this screen.
                                        //Reducing it by 30 is a band-aid patch to make it fit better.
                                        putExtra("height", size.height-30)
                                    }
                                    //begin the next activity.
                                    context.startActivity(intent)
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