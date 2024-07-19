package com.mohit.webview_test

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mohit.webview_test.ui.theme.WebView_TestTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebView_TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ViewFrame(modifier = Modifier
                        .padding(innerPadding)
                    )
                }
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ViewFrame(modifier: Modifier = Modifier) {
    var customURL by remember { mutableStateOf("https://jkrobotics.in/") }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var pagetitle by remember { mutableStateOf("") }
    var progress by remember {mutableFloatStateOf(0.1f)}
    val progressPage = animateFloatAsState(targetValue = progress, animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec)
    val (pageLogo,updateIcon)  = remember { mutableStateOf<Bitmap?>(null) }
    var webview : WebView ?= null
    val console = remember { mutableStateListOf<String>() }
    val scrollstate = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(modifier =modifier) {
        Row(modifier=Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.SpaceAround){
            Image(
                bitmap = pageLogo?.asImageBitmap() ?: ImageBitmap(10,10),
                contentDescription = "pageLogo",
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
            )
            Text(
                text = pagetitle,
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            )
        }
        if(progressPage.value < 1f){
            LinearProgressIndicator(progress = progressPage.value, color = Color.Red, modifier = Modifier.fillMaxWidth())
        }

        Column (modifier = Modifier
            .fillMaxHeight(0.7f)
            .background(color = Color.Red)
            , horizontalAlignment = Alignment.CenterHorizontally) {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = {
                WebView(it).apply {
                    this.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    webViewClient = WebViewClient()
                    webChromeClient = object : WebChromeClient(){
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            progress = newProgress/100f
                            canGoBack = view?.canGoBack() ?: false
                            canGoForward = view?.canGoForward() ?: false
                        }

                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            console.add(consoleMessage?.message().toString())
                            coroutineScope.launch {
                                scrollstate.animateScrollTo(scrollstate.maxValue+100)
                            }
                            return super.onConsoleMessage(consoleMessage)
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            pagetitle = title ?: ""
                        }

                        override fun onReceivedIcon(view: WebView?, icon: Bitmap) {
                            updateIcon(icon)
                        }

                        override fun onJsAlert(
                            view: WebView?,
                            url: String?,
                            message: String?,
                            result: JsResult?
                        ): Boolean {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            return super.onJsAlert(view, url, message, result)
                        }

                    }
                    settings.javaScriptEnabled = true
                    loadUrl(customURL)
                    webview = this
                }
            },update = {
                webview = it
            })
        }
        Row (modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(text = "Console Message", color = Color.Black , fontWeight = FontWeight.Bold)
            Button(onClick = { webview?.goForward() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black,
                    disabledContentColor = Color.Gray,
                    disabledContainerColor = Color.Transparent),
                enabled = canGoForward
            ) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "forward")
            }
        }
        Column (modifier = Modifier
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollstate)){
            console.forEach{
                Divider()
                Text(text = it, color = Color.Black, modifier = Modifier.padding(10.dp))
            }
        }
    }

    BackHandler(enabled = canGoBack) {
        webview?.goBack()
    }
}