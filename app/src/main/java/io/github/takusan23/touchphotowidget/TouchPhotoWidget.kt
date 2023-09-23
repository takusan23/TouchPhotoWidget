package io.github.takusan23.touchphotowidget

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
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
import androidx.glance.layout.size
import androidx.glance.material3.ColorProviders
import io.github.takusan23.touchphotowidget.ui.theme.DarkColorScheme
import io.github.takusan23.touchphotowidget.ui.theme.LightColorScheme

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

            // テーマ機能
            GlanceTheme(
                colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    GlanceTheme.colors
                } else {
                    colors
                }
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.secondaryContainer)
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
                // 戻るボタン
                Image(
                    modifier = GlanceModifier
                        .size(40.dp)
                        .padding(5.dp)
                        .cornerRadius(10.dp)
                        .clickable(onBack),
                    provider = ImageProvider(resId = R.drawable.outline_arrow_back_24),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                // アプリを開く
                // 塗りつぶし
                Image(
                    modifier = GlanceModifier
                        .size(40.dp)
                        .padding(5.dp)
                        .background(GlanceTheme.colors.primary)
                        .cornerRadius(10.dp)
                        .clickable(actionStartActivity(intent)),
                    provider = ImageProvider(resId = R.drawable.outline_open_in_new_24),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.primaryContainer)
                )
            }
            Image(
                modifier = GlanceModifier.fillMaxSize(),
                provider = ImageProvider(photoData.bitmap),
                contentDescription = null
            )
        }
    }

    companion object {
        val colors = ColorProviders(
            light = LightColorScheme,
            dark = DarkColorScheme
        )
    }

}