package io.github.takusan23.touchphotowidget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.GridCells
import androidx.glance.appwidget.lazy.LazyVerticalGrid
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding

class TouchPhotoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {

            // 押した画像、選択していない場合は null
            val selectPhoto = remember { mutableStateOf<PhotoTool.PhotoData?>(null) }
            // 画像一覧
            val bitmapList = remember { mutableStateOf(emptyList<PhotoTool.PhotoData>()) }

            // 画像をロード
            LaunchedEffect(key1 = Unit) {
                bitmapList.value = PhotoTool.getLatestPhotoBitmap(context, limit = 10) // TODO 戻す
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                if (selectPhoto.value != null) {
                    // 選択した画像がある
                    PhotoDetail(
                        photoData = selectPhoto.value!!,
                        onBack = { selectPhoto.value = null }
                    )
                } else {
                    // 一覧表示
                    PhotoGridList(
                        photoDataList = bitmapList.value,
                        onClick = { bitmap -> selectPhoto.value = bitmap }
                    )
                }
            }
        }
    }

    /**
     * グリッド表示で写真を表示する
     *
     * @param context [Context]
     * @param onClick 写真を押したら呼ばれる
     */
    @Composable
    fun PhotoGridList(
        photoDataList: List<PhotoTool.PhotoData>,
        onClick: (PhotoTool.PhotoData) -> Unit
    ) {
        LazyVerticalGrid(
            modifier = GlanceModifier.fillMaxSize(),
            gridCells = GridCells.Fixed(4)
        ) {
            items(photoDataList) { photoData ->
                Image(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { onClick(photoData) },
                    provider = ImageProvider(photoData.bitmap),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            }
        }
    }

    /**
     * 写真の詳細画面
     *
     * @param photoData [PhotoTool.PhotoData]
     * @param onBack 戻る押した時
     */
    @Composable
    fun PhotoDetail(
        photoData: PhotoTool.PhotoData,
        onBack: () -> Unit
    ) {
        // 画像アプリで開くための Intent
        // data に Uri を渡すことで対応しているアプリをあぶり出す
        val intent = remember { Intent(Intent.ACTION_VIEW, photoData.uri) }

        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Button(
                    modifier = GlanceModifier.padding(10.dp),
                    text = "戻る",
                    onClick = onBack
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Button(
                    modifier = GlanceModifier.padding(10.dp),
                    text = "開く",
                    onClick = actionStartActivity(intent)
                )
            }
            Image(
                modifier = GlanceModifier.fillMaxSize(),
                provider = ImageProvider(photoData.bitmap),
                contentDescription = null
            )
        }
    }

}