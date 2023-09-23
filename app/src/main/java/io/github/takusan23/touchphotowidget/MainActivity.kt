package io.github.takusan23.touchphotowidget

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import io.github.takusan23.touchphotowidget.ui.theme.TouchPhotoWidgetTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val isGranted = remember {
                // 初期値は権限があるか
                mutableStateOf(ContextCompat.checkSelfPermission(context, REQUEST_PERMISSION) == PackageManager.PERMISSION_GRANTED)
            }
            // 権限コールバック
            val requestPermission = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
                isGranted.value = it
            }

            TouchPhotoWidgetTheme {
                Scaffold(
                    topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        if (isGranted.value) {
                            Text(text = "ホーム画面を長押しして、ウィジェットを追加してください")
                        } else {
                            Text(text = "写真を取得する権限が必要です")
                            Button(onClick = {
                                requestPermission.launch(REQUEST_PERMISSION)
                            }) { Text(text = "権限をリクエストする") }
                        }

                    }
                }
            }
        }
    }

    companion object {

        /** 必要な権限 */
        val REQUEST_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

    }

}