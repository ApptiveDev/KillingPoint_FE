package com.killingpart.killingpoint.ui.screen.DiaryDetailScreen

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
        Image(
            painter = painterResource(id = R.drawable.my_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.48f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 21.dp)
                .padding(top = 38.dp, bottom = 42.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(292.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black.copy(alpha = 0.72f))
                    .border(0.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AlbumDiskArtwork(
                        artwork = artwork,
                        modifier = Modifier.size(width = 200.dp, height = 142.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = musicTitle,
                            color = Color.White,
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 266.dp)
                        )
                        Text(
                            text = artist,
                            color = Color.White.copy(alpha = 0.82f),
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 12.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 266.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    TimelineRange(
                        startText = startText,
                        endText = endText,
                        startProgress = startProgress,
                        endProgress = endProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 코멘트 카드
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(272.dp)
                    .height(241.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0xFF1F1F1F))
                    .border(0.dp, color = Color.Transparent, RoundedCornerShape(13.dp))
            ) {
                Text(
                    text = if (content.isBlank()) "작성된 코멘트가 없어요." else content,
                    color = Color.White.copy(alpha = 0.92f),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.5.sp,
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
                            color = Color(0xFF7B7B7B),
                            fontFamily = PaperlogyFontFamily,
                            fontSize = 8.5.sp
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = tagText,
                            color = Color(0xFF7B7B7B),
                            fontFamily = PaperlogyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 8.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Image(
                        painter = painterResource(id = R.drawable.ic_killingpart),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(7.dp))
                    )
                }
            }
        }
    }
}

/**
 * iOS AddSearchDetailAlbumArtworkView(coverSize=160) 를 코드로 재현.
 * 뒤쪽 LP 회전판(그라디언트 원판 + 홈 9개 + 외곽 링 + 중앙 라벨)이 오른쪽으로 빼꼼 나오고,
 * 앞쪽에 정사각 앨범 커버(모서리 16, 흰 0.12 테두리)를 겹친다.
 */
@Composable
private fun AlbumDiskArtwork(
    artwork: ImageBitmap?,
    modifier: Modifier = Modifier
) {
    val coverSize = 140.dp
    val diskSize = coverSize * 0.9f              // 144
    val centerLabelSize = diskSize * 0.34f       // 48.96
    val centerImageInset = centerLabelSize * 0.12f
    val centerHoleSize = (diskSize * 0.05f).coerceAtLeast(4.dp)
    val grooveBaseInset = diskSize * 0.08f
    val grooveStepInset = diskSize * 0.04f
    val grooveStroke = (diskSize * 0.006f).coerceAtLeast(0.8.dp)
    val outerRingStroke = (diskSize * 0.008f).coerceAtLeast(1.dp)
    val contentWidth = coverSize * 1.48f         // coverSize + coverSize*0.48 = 236.8

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(width = contentWidth, height = coverSize)) {
            // 뒤: LP 회전판 (오른쪽으로 offset)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = coverSize * 0.55f)
                    .size(diskSize),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = size.minDimension / 2f
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.95f),
                                Color.White.copy(alpha = 0.06f),
                                Color.Black.copy(alpha = 0.98f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        ),
                        radius = r,
                        center = center
                    )
                    for (i in 0 until 9) {
                        val inset = grooveBaseInset.toPx() + i * grooveStepInset.toPx()
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = r - inset,
                            center = center,
                            style = Stroke(width = grooveStroke.toPx())
                        )
                    }
                    drawCircle(
                        color = Color.White.copy(alpha = 0.26f),
                        radius = r,
                        center = center,
                        style = Stroke(width = outerRingStroke.toPx())
                    )
                }

                Box(
                    modifier = Modifier
                        .size(centerLabelSize)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.86f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (artwork != null) {
                        Image(
                            bitmap = artwork,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(centerImageInset)
                                .clip(CircleShape)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(centerHoleSize)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.9f))
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(coverSize)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF2A2A2C))
                    .border(0.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(6.dp)),
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
    }
}

@Composable
private fun TimelineRange(
    startText: String,
    endText: String,
    startProgress: Float,
    endProgress: Float,
    modifier: Modifier = Modifier
) {
    val trackHeight = 4.dp
    val segmentHeight = 6.dp
    val labelWidth = 40.dp
    val labelY = 24.dp
    val totalHeight = 38.dp
    val halfLabel = labelWidth / 2
    val minGap = labelWidth + 4.dp

    BoxWithConstraints(modifier = modifier.height(totalHeight)) {
        val width = maxWidth
        val sp = startProgress.coerceIn(0f, 1f)
        val ep = endProgress.coerceIn(0f, 1f)
        val startX = width * sp
        val endX = width * ep
        val segmentWidth = (endX - startX).coerceAtLeast(2.dp)

        val upper = (width - halfLabel).coerceAtLeast(halfLabel)
        val clampedStart = startX.coerceIn(halfLabel, upper)
        val clampedEnd = endX.coerceIn(halfLabel, upper)
        val initLeft = minOf(clampedStart, clampedEnd)
        val initRight = maxOf(clampedStart, clampedEnd)
        val initGap = initRight - initLeft
        val adjUpper = (width - minGap / 2).coerceAtLeast(minGap / 2)
        val adjCenter = ((initLeft + initRight) / 2).coerceIn(minGap / 2, adjUpper)
        val leftX = if (initGap < minGap) adjCenter - minGap / 2 else initLeft
        val rightX = if (initGap < minGap) adjCenter + minGap / 2 else initRight
        val isStartLeft = clampedStart <= clampedEnd
        val startLabelX = if (isStartLeft) leftX else rightX
        val endLabelX = if (isStartLeft) rightX else leftX

        // 트랙
        Box(
            modifier = Modifier
                .offset(y = 2.5.dp)
                .fillMaxWidth().padding(horizontal = 10.dp)
                .height(trackHeight)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.42f))
        )
        // 구간
        Box(
            modifier = Modifier
                .offset(x = startX, y = 2.dp)
                .width(segmentWidth)
                .height(segmentHeight)
                .clip(CircleShape)
                .background(mainGreen)
        )
        // 시작 라벨
        Text(
            text = startText,
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Thin,
            fontSize = 11.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(labelWidth)
                .offset(x = startLabelX - halfLabel, y = labelY - 7.dp)
        )
        // 끝 라벨
        Text(
            text = endText,
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Thin,
            fontSize = 11.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(labelWidth)
                .offset(x = endLabelX - halfLabel, y = labelY - 7.dp)
        )
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

    /**
     * 카드 비트맵을 캐시에 저장한 뒤 FileProvider URI 로 네이티브 공유 시트를 띄운다.
     * 이미지 + 딥링크 URL(텍스트)을 함께 공유한다.
     */
    suspend fun shareImageNative(
        context: Context,
        bitmap: Bitmap,
        linkUrl: String
    ): Result<Unit> {
        return try {
            val uri = withContext(Dispatchers.IO) {
                val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
                val file = File(dir, "diary_share_${bitmap.hashCode()}.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, linkUrl)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "공유하기"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 카드 이미지를 인스타그램 스토리 배경으로 공유한다.
     * Facebook(Meta) App ID 가 필요하며, 인스타그램 앱이 설치돼 있어야 한다.
     */
    suspend fun shareInstagramStory(
        context: Context,
        bitmap: Bitmap,
        linkUrl: String,
        facebookAppId: String
    ): Result<Unit> {
        return try {
            if (facebookAppId.isBlank()) {
                return Result.failure(IllegalStateException("인스타 스토리 공유를 위해 Facebook App ID 설정이 필요해요."))
            }

            val uri = withContext(Dispatchers.IO) {
                val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
                val file = File(dir, "insta_story_${bitmap.hashCode()}.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }

            val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
                setDataAndType(uri, "image/png")
                putExtra("source_application", facebookAppId)
                putExtra("content_url", linkUrl)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.grantUriPermission("com.instagram.android", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (intent.resolveActivity(context.packageManager) == null) {
                return Result.failure(IllegalStateException("인스타그램 앱을 열 수 없어요. 설치 여부를 확인해 주세요."))
            }
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 카카오 SDK FeedTemplate 로 카톡 리치 카드(이미지+제목+설명+버튼)를 공유한다.
     * 카드 이미지는 카카오 이미지 서버에 업로드하므로 별도 백엔드가 필요 없다.
     */
    suspend fun shareKakao(
        context: Context,
        bitmap: Bitmap,
        title: String,
        description: String,
        linkUrl: String,
        diaryId: Long
    ): Result<Unit> {
        return try {
            if (!ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
                return Result.failure(IllegalStateException("카카오톡이 설치되어 있지 않아요."))
            }

            val file = withContext(Dispatchers.IO) {
                val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
                val f = File(dir, "kakao_share_${bitmap.hashCode()}.png")
                FileOutputStream(f).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
                f
            }

            // 1) 카드 이미지를 카카오 서버에 업로드
            val imageUrl = suspendCancellableCoroutine<String> { cont ->
                ShareClient.instance.uploadImage(file) { result, error ->
                    when {
                        error != null -> cont.resumeWithException(error)
                        result != null -> cont.resume(result.infos.original.url)
                        else -> cont.resumeWithException(IllegalStateException("이미지 업로드에 실패했어요."))
                    }
                }
            }

            // 2) FeedTemplate 구성 후 공유
            // androidExecutionParams: 카드 탭 시 앱 설치자는 kakao{앱키}://kakaolink?route=diary&diaryId=... 로 앱이 열림
            val executionParams = if (diaryId > 0) {
                mapOf("route" to "diary", "diaryId" to diaryId.toString())
            } else {
                emptyMap()
            }
            val link = Link(
                webUrl = linkUrl,
                mobileWebUrl = linkUrl,
                androidExecutionParams = executionParams,
                iosExecutionParams = executionParams
            )
            val template = FeedTemplate(
                content = Content(
                    title = title,
                    imageUrl = imageUrl,
                    link = link,
                    description = description
                ),
                buttons = listOf(
                    Button(title = "킬링파트 보러가기", link = link)
                )
            )

            suspendCancellableCoroutine<Unit> { cont ->
                ShareClient.instance.shareDefault(context, template) { sharingResult, error ->
                    when {
                        error != null -> cont.resumeWithException(error)
                        sharingResult != null -> {
                            context.startActivity(sharingResult.intent)
                            cont.resume(Unit)
                        }
                        else -> cont.resumeWithException(IllegalStateException("카카오톡 공유에 실패했어요."))
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/** ContextWrapper 를 풀어 Activity 를 찾는다. */
fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * Compose 콘텐츠를 화면과 무관하게 오프스크린 ComposeView 로 렌더링해 Bitmap 으로 반환한다.
 * view.draw(canvas) 는 동기 소프트웨어 렌더링이라 유튜브 영상 재생/정지 상태와 무관하게 동작한다.
 *
 * iOS(ImageRenderer scale=3) 와 동일하게, 기기 density 와 무관하게 dp 크기의 [targetScale] 배
 * 해상도로 고정 출력한다. (예: 360x640dp -> 1080x1920px)
 */
suspend fun renderComposableToBitmap(
    activity: Activity,
    targetScale: Float = 3f,
    content: @androidx.compose.runtime.Composable () -> Unit
): Bitmap {
    val root = activity.window.decorView as ViewGroup
    val composeView = ComposeView(activity).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        visibility = View.INVISIBLE
        setContent(content)
    }
    root.addView(
        composeView,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    )

    return try {
        // 컴포지션/레이아웃이 끝나도록 몇 프레임 대기 (Choreographer 기반이라 화면 상태와 무관)
        awaitFrame()
        awaitFrame()

        val unspecified = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        composeView.measure(unspecified, unspecified)
        val width = composeView.measuredWidth.coerceAtLeast(1)
        val height = composeView.measuredHeight.coerceAtLeast(1)
        composeView.layout(0, 0, width, height)

        // 기기 density px -> dp*targetScale px 로 스케일해 고정 해상도(예: 1080x1920)로 출력
        val density = activity.resources.displayMetrics.density
        val drawScale = targetScale / density
        val targetWidth = ((width / density) * targetScale).roundToInt().coerceAtLeast(1)
        val targetHeight = ((height / density) * targetScale).roundToInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(drawScale, drawScale)
        composeView.draw(canvas)
        bitmap
    } finally {
        root.removeView(composeView)
    }
}
