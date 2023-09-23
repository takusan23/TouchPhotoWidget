package io.github.takusan23.touchphotowidget

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

object PhotoTool {

    /**
     * 写真を取得する
     *
     * @param context [Context]
     * @param limit 上限
     * @param size [Bitmap] の大きさ
     * @return [PhotoData] の配列
     */
    suspend fun getLatestPhotoBitmap(
        context: Context,
        limit: Int = 20,
        size: Int = 200
    ): List<PhotoData> = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val selection = arrayOf(MediaStore.MediaColumns._ID)
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // LIMIT が使える
            contentResolver.query(
                uri,
                selection,
                bundleOf(
                    ContentResolver.QUERY_ARG_LIMIT to limit,
                    ContentResolver.QUERY_ARG_SQL_SORT_ORDER to sortOrder
                ),
                null
            )
        } else {
            // 使えないので、取り出す際にやる
            contentResolver.query(
                uri,
                selection,
                null,
                null,
                sortOrder
            )
        }?.use { cursor ->
            cursor.moveToFirst()
            // 返す
            (0 until min(cursor.count, limit))
                .map {
                    // コンテンツの ID を取得
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    cursor.moveToNext()
                    id
                }
                .map { id ->
                    // ID から Uri を取得
                    ContentUris.withAppendedId(uri, id)
                }
                .map { uri ->
                    // Uri から Bitmap を返す
                    // Glide で小さくしてから Bitmap を取得する
                    PhotoData(getBitmap(context, uri, size), uri)
                }
        } ?: emptyList()
    }

    /**
     * 画像をロードする
     * Glide を使うので小さくして Bitmap を返せます
     *
     * @param context [Context]
     * @param uri [Uri]
     * @param size サイズ
     * @return [Bitmap]
     */
    suspend fun getBitmap(
        context: Context,
        uri: Uri,
        size: Int,
    ): Bitmap = withContext(Dispatchers.IO) {
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .submit(size, size)
            .get()
    }

    /**
     * [Bitmap] と [Uri] のデータクラス
     */
    data class PhotoData(
        val bitmap: Bitmap,
        val uri: Uri
    )

}