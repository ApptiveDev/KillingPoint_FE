package com.killingpart.killingpoint.ui.screen.DiaryDetailScreen

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import coil.imageLoader
import coil.request.ImageRequest
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val CardWidth = 360.dp
private val CardHeight = 640.dp

@Composable
fun DiaryShareCard(
    artwork: ImageBitmap?,
    musicTitle: String,
    artist: String,
    content: String,
    dateText: String,
    tagText: String,
    startText: String,
    endText: String,
    startProgress: Float,
    endProgress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .requiredSize(CardWidth, CardHeight)
            .background(Color(0xFF1D1E20))
    ) {
        // AppBackground 와 동일한 어두운 원 배경
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF060606),
                radius = size.minDimension * 0.85f,
                center = Offset(size.width * 0.1f, size.height * 0.37f)
            )
            drawCircle(
                color = Color(0xFF060606),
                radius = size.minDimension * 1.5f,
                center = Offset(size.width * 1.1f, size.height * 1.2f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 21.dp)
                .padding(top = 38.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 트랙 카드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(292.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black.copy(alpha = 0.72f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(width = 200.dp, height = 150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 앨범 커버 뒤 CD (오른쪽으로 살짝 빼꼼)
                        Image(
                            painter = painterResource(id = R.drawable.cd),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(150.dp)
                                .offset(x = 40.dp)
                        )
                        // 앨범 커버 (앞)
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .offset(x = (-14).dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2A2A2C)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (artwork != null) {
                                Image(
                                    bitmap = artwork,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = musicTitle,
                        color = Color.White,
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = artist,
                        color = Color.White.copy(alpha = 0.82f),
                        fontFamily = PaperlogyFontFamily,
                        fontSize = 13.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 36.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        ) {
                            val y = size.height / 2
                            drawLine(
                                color = Color.White.copy(alpha = 0.42f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            val sx = size.width * startProgress.coerceIn(0f, 1f)
                            val ex = size.width * endProgress.coerceIn(0f, 1f)
                            if (ex > sx) {
                                drawLine(
                                    color = mainGreen,
                                    start = Offset(sx, y),
                                    end = Offset(ex, y),
                                    strokeWidth = 6.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = startText,
                                color = Color.White.copy(alpha = 0.62f),
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 10.sp,
                                modifier = Modifier.align(
                                    BiasAlignment(2f * startProgress.coerceIn(0f, 1f) - 1f, 0f)
                                )
                            )
                            Text(
                                text = endText,
                                color = Color.White.copy(alpha = 0.62f),
                                fontFamily = PaperlogyFontFamily,
                                fontSize = 10.sp,
                                modifier = Modifier.align(
                                    BiasAlignment(2f * endProgress.coerceIn(0f, 1f) - 1f, 0f)
                                )
                            )
                        }
                    }
                }
            }

            // 코멘트 카드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(238.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.White.copy(alpha = 0.10f))
            ) {
                Text(
                    text = if (content.isBlank()) "작성된 코멘트가 없어요." else content,
                    color = Color.White.copy(alpha = 0.92f),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp)
                        .padding(top = 24.dp, bottom = 74.dp)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 18.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = dateText,
                            color = Color.White.copy(alpha = 0.52f),
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = tagText,
                            color = Color.White.copy(alpha = 0.70f),
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Image(
                        painter = painterResource(id = R.drawable.kp_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }
        }
    }
}

object DiaryShareImage {

    /** Coil 로 앨범 아트를 미리 불러온다 (캡처 전에 이미지가 준비되도록). */
    suspend fun loadArtwork(context: Context, url: String): ImageBitmap? {
        if (url.isBlank()) return null
        return try {
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // 캡처(software canvas)에 그릴 수 있도록
                .build()
            val result = context.imageLoader.execute(request)
            (result.drawable as? BitmapDrawable)?.bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    /** 비트맵을 갤러리(Pictures/KillingPart)에 저장. minSdk 29+ 라 권한 불필요. */
    suspend fun saveToGallery(
        context: Context,
        bitmap: Bitmap,
        displayName: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/KillingPart"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext Result.failure(IllegalStateException("저장 위치를 만들지 못했어요."))

            resolver.openOutputStream(uri).use { out ->
                if (out == null || !bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    return@withContext Result.failure(IllegalStateException("이미지를 저장하지 못했어요."))
                }
            }

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
