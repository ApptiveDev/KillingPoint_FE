package com.killingpart.killingpoint.ui.screen.WriteDiaryScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

fun formatTime(seconds: Float): String {
    val totalSeconds = seconds.toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

/** 구간자르기 개선안(PM 스펙) 구현
 *
 * 좌표계: 모든 요소를 '절대 초(sec)' 기준으로 통일
 *  - startSec / endSec : 선택 구간(절대 초)
 *  - viewCenterSec     : 뷰포트 가로 중앙에 오는 절대 초 (Animatable)
 *  - secToX(sec)       : viewportWidth/2 + (sec - viewCenterSec) * pxPerSec
 *
 * 인터랙션
 *  - 핸들 드래그: 구간 리사이즈. 뗀 후 0.5초 대기 → 0.4초 tween 으로 구간 중앙이 뷰 중앙으로 복귀
 *      좌핸들은 뗄 때 새 시작점부터 재생 새로고침(onSeek), 우핸들은 재생 유지
 *  - 좌측 핸들 탭: 구간 처음부터 재생(onSeek)
 *  - 핸들 0.5초 롱프레스: 선택 구간 안쪽 2초 루프 활성(onLoopChange), 떼거나 움직이면 해제
 *      좌핸들 = start~start+2, 우핸들 = end-2~end
 *      루프 중에는 핸들이 바깥으로 비켜서고 2초 밴드/배지가 강조 표시됨
 *  - 트랙 탭: 그 지점부터 재생(onSeek)
 *  - 트랙 가로 드래그: 구간 길이를 유지한 채 스펙트럼바 스크롤(구간은 화면 중앙 고정),
 *      뗀 뒤 새 구간 시작부터 다시 재생 (미니맵 스크럽과 동일)
 *  - -1s/+1s: 구간 앞/뒤 1초 확장(재생 유지), 조정 후 중앙 복귀
 *
 * 재생 표시
 *  - currentPlaySec 위치에 흰색 인디케이터, 재생된 구간 막대는 네온색으로 채워짐
 */

private enum class HandleSide { LEFT, RIGHT }

/** 구간 조절 후 중앙 복귀 모션이 시작되기 전 대기 시간 */
private const val recenterDelayMillis = 500

/** 핸들 롱프레스 시 반복 재생되는 구간 길이(초) */
private const val loopWindowSec = 2f

@Composable
fun KillingPartSelector(
    totalDuration: Int,
    /** 첫 레이아웃 시 선택 구간 (다른 영상으로 바꿀 때 부모에서 넘김) */
    initialStartSec: Float = 0f,
    initialDurationSec: Float = 20f,
    /** 현재 재생 위치(절대 초). 재생 인디케이터/채우기 표시용 */
    currentPlaySec: Float = 0f,
    isPlaying: Boolean = false,
    onStartChange: (start: Float, end: Float, duration: Float) -> Unit,
    /** 특정 지점부터 재생 요청(트랙 탭 / 좌핸들 탭) */
    onSeek: (sec: Float) -> Unit = {},
    /** 2초 루프 구간 변경. null 이면 루프 해제 */
    onLoopChange: (loopStart: Float?, loopEnd: Float?) -> Unit = { _, _ -> },
    onHandleAdjusted: ((handleSide: String) -> Unit)? = null
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val minDurationSec = 10f
    val maxDurationSec = 30f.coerceAtMost(totalDuration.toFloat().coerceAtLeast(minDurationSec))

    // 뷰포트에 보이는 시간 폭(초). 최대 구간(30s) + 여백이 항상 들어오도록.
    val visibleSeconds = 40f

    // ---- 치수 (개선안 픽셀 기준, dp 변환) ----
    val trackHeight = 120.dp
    val boxHeight = 92.dp
    val handleWidth = 22.dp
    val handleHeight = 74.dp
    val handleCorner = 7.dp
    val boxCorner = 12.dp
    val edgeButtonSize = 32.dp

    val barWidth = 3.dp
    val barGap = 4.dp
    val barWidthPx = with(density) { barWidth.toPx() }
    val barGapPx = with(density) { barGap.toPx() }
    val handleWidthPx = with(density) { handleWidth.toPx() }
    val handleHeightPx = with(density) { handleHeight.toPx() }
    val trackHeightPx = with(density) { trackHeight.toPx() }
    val handleTouchPadPx = with(density) { 4.dp.toPx() }

    // ---- 상태 ----
    var viewportWidthPx by remember { mutableStateOf(0f) }
    var pxPerSec by remember { mutableStateOf(1f) }

    var startSec by remember { mutableStateOf(initialStartSec) }
    var endSec by remember { mutableStateOf(initialStartSec + initialDurationSec) }
    val viewCenterSec = remember { Animatable(initialStartSec + initialDurationSec / 2f) }

    var pressedSide by remember { mutableStateOf<HandleSide?>(null) }
    var loopSide by remember { mutableStateOf<HandleSide?>(null) }
    var initialized by remember { mutableStateOf(false) }

    var lastCommittedStart by remember { mutableStateOf(Float.NaN) }
    var lastCommittedEnd by remember { mutableStateOf(Float.NaN) }

    val latestOnHandleAdjusted by rememberUpdatedState(onHandleAdjusted)
    val latestOnLoopChange by rememberUpdatedState(onLoopChange)
    val latestOnSeek by rememberUpdatedState(onSeek)

    fun secToX(sec: Float): Float = viewportWidthPx / 2f + (sec - viewCenterSec.value) * pxPerSec
    fun xToSec(x: Float): Float = viewCenterSec.value + (x - viewportWidthPx / 2f) / pxPerSec

    fun commit(force: Boolean = false) {
        val d = (endSec - startSec).coerceAtLeast(0f)
        if (force || startSec != lastCommittedStart || endSec != lastCommittedEnd) {
            onStartChange(startSec, endSec, d)
            lastCommittedStart = startSec
            lastCommittedEnd = endSec
        }
    }

    /** 구간 조절이 끝난 뒤 [delayMillis] 만큼 쉬었다가 구간 중앙을 뷰 중앙으로 복귀 */
    fun recenter(animated: Boolean = true, delayMillis: Int = recenterDelayMillis) {
        val target = (startSec + endSec) / 2f
        scope.launch {
            if (animated) {
                viewCenterSec.animateTo(
                    target,
                    tween(durationMillis = 400, delayMillis = delayMillis, easing = LinearEasing)
                )
            } else {
                viewCenterSec.snapTo(target)
            }
        }
    }

    // 최초 초기화
    LaunchedEffect(viewportWidthPx, totalDuration) {
        if (viewportWidthPx > 0f && !initialized) {
            pxPerSec = viewportWidthPx / visibleSeconds

            val initDur = initialDurationSec.coerceIn(minDurationSec, maxDurationSec)
            val maxStart = (totalDuration.toFloat() - initDur).coerceAtLeast(0f)
            startSec = initialStartSec.coerceIn(0f, maxStart)
            endSec = (startSec + initDur).coerceAtMost(totalDuration.toFloat())
            viewCenterSec.snapTo((startSec + endSec) / 2f)

            commit(force = true)
            initialized = true
        }
    }

    // 재생 인디케이터: 프레임 기반 1x 진행 + 권위값(currentPlaySec) 큰 점프만 보정
    //  - onCurrentSecond 는 ~1초 간격(지터 有) → 매 프레임 실제 경과시간만큼 진행시켜 부드럽게
    //  - seek/루프 등 0.75초 초과 점프만 즉시 반영(그 외 소소한 지터는 무시해 멈춤/역행 방지)
    var displayPlaySec by remember { mutableStateOf(0f) }
    LaunchedEffect(currentPlaySec) {
        if (abs(currentPlaySec - displayPlaySec) > 0.75f) {
            displayPlaySec = currentPlaySec
        }
    }
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    displayPlaySec += (now - last) / 1_000_000_000f
                }
                last = now
            }
        }
    }

    fun applyResize(side: HandleSide, dxPx: Float) {
        val dSec = dxPx / pxPerSec
        when (side) {
            HandleSide.LEFT -> {
                startSec = (startSec + dSec).coerceIn(
                    (endSec - maxDurationSec).coerceAtLeast(0f),
                    endSec - minDurationSec
                )
            }
            HandleSide.RIGHT -> {
                endSec = (endSec + dSec).coerceIn(
                    startSec + minDurationSec,
                    (startSec + maxDurationSec).coerceAtMost(totalDuration.toFloat())
                )
            }
        }
        commit()
    }

    fun activateLoop(side: HandleSide) {
        loopSide = side
        val (ls, le) = when (side) {
            HandleSide.LEFT -> startSec to (startSec + loopWindowSec).coerceAtMost(endSec)
            HandleSide.RIGHT -> (endSec - loopWindowSec).coerceAtLeast(startSec) to endSec
        }
        latestOnLoopChange(ls, le)
    }

    fun deactivateLoop() {
        if (loopSide != null) {
            loopSide = null
            latestOnLoopChange(null, null)
        }
    }

    // ---- 2초 루프 구간(절대 초). 두 핸들 모두 선택 구간 안쪽으로 잡는다 ----
    val loopStartSec = when (loopSide) {
        HandleSide.LEFT -> startSec
        HandleSide.RIGHT -> (endSec - loopWindowSec).coerceAtLeast(startSec)
        null -> Float.NaN
    }
    val loopEndSec = when (loopSide) {
        HandleSide.LEFT -> (startSec + loopWindowSec).coerceAtMost(endSec)
        HandleSide.RIGHT -> endSec
        null -> Float.NaN
    }

    // 루프 중에는 해당 핸들이 2초 밴드를 가리지 않도록 바깥쪽으로 비켜선다.
    // (2초 폭 ≈ 19dp < 핸들 폭 22dp 라 비키지 않으면 밴드가 완전히 가려짐)
    val leftHandleShiftPx by animateFloatAsState(
        if (loopSide == HandleSide.LEFT) -handleWidthPx else 0f,
        label = "leftHandleShift"
    )
    val rightHandleShiftPx by animateFloatAsState(
        if (loopSide == HandleSide.RIGHT) handleWidthPx else 0f,
        label = "rightHandleShift"
    )

    // 루프 밴드 깜빡임(강조)
    val loopPulseTransition = rememberInfiniteTransition(label = "loopPulse")
    val loopPulse by loopPulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loopPulseValue"
    )

    fun extendFront() {
        val newStart = (startSec - 1f)
            .coerceAtLeast(0f)
            .coerceAtLeast(endSec - maxDurationSec)
        if (newStart != startSec) {
            startSec = newStart
            commit()
            recenter()
        }
    }

    fun extendBack() {
        val newEnd = (endSec + 1f)
            .coerceAtMost(totalDuration.toFloat())
            .coerceAtMost(startSec + maxDurationSec)
        if (newEnd != endSec) {
            endSec = newEnd
            commit()
            recenter()
        }
    }

    /** 스펙트럼바 가로 스크롤: 구간 길이는 유지한 채 통째로 이동(미니맵 스크럽과 동일 동작) */
    fun panSectionTo(newStartSec: Float) {
        val dur = endSec - startSec
        val ns = newStartSec.coerceIn(0f, (totalDuration.toFloat() - dur).coerceAtLeast(0f))
        if (ns != startSec) {
            startSec = ns
            endSec = ns + dur
            commit()
        }
        // 구간은 항상 화면 중앙에 고정된 채 파형만 흐르도록
        recenter(animated = false)
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // ===== 메인 트랙 =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .onGloballyPositioned {
                    val w = it.size.width.toFloat()
                    if (w > 0f && w != viewportWidthPx) {
                        viewportWidthPx = w
                        if (initialized) pxPerSec = w / visibleSeconds
                    }
                }
                // 트랙 탭 → 그 지점부터 재생 / 가로 드래그 → 스펙트럼바 스크롤
                .pointerInput(Unit) {
                    val slop = viewConfiguration.touchSlop
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        // 핸들 위에서 시작한 제스처는 핸들(리사이즈/루프)이 처리
                        if (isOnHandle(
                                pos = down.position,
                                leftHandleLeftX = secToX(startSec) + leftHandleShiftPx,
                                rightHandleLeftX = secToX(endSec) - handleWidthPx + rightHandleShiftPx,
                                handleWidthPx = handleWidthPx,
                                handleHeightPx = handleHeightPx,
                                trackHeightPx = trackHeightPx,
                                padPx = handleTouchPadPx
                            )
                        ) return@awaitEachGesture
                        var panning = false
                        var totalDx = 0f
                        var accStartSec = 0f
                        var canceled = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val ch = event.changes.firstOrNull { it.id == down.id } ?: break
                            // 핸들 등 자식이 가져간 제스처는 트랙에서 처리하지 않음
                            if (ch.isConsumed && !panning) { canceled = true; break }
                            if (!ch.pressed) break

                            val dx = ch.positionChange().x
                            totalDx += dx
                            if (!panning && abs(totalDx) > slop && pxPerSec > 0f) {
                                panning = true
                                accStartSec = startSec
                            }
                            if (panning) {
                                // 오른쪽으로 끌면 이전 시간대가 보이도록(파형이 따라옴)
                                accStartSec -= dx / pxPerSec
                                panSectionTo(accStartSec)
                                ch.consume()
                            }
                        }

                        when {
                            canceled -> Unit
                            panning -> {
                                commit(force = true)
                                // 새 구간 시작부터 다시 미리듣기
                                latestOnSeek(startSec)
                                latestOnHandleAdjusted?.invoke(KillingPartHandle.SPECTRUM_BAR)
                            }
                            else -> {
                                // 탭: 누른 지점부터 미리듣기
                                val sec = xToSec(down.position.x)
                                if (sec in startSec..endSec) {
                                    latestOnSeek(sec)
                                }
                            }
                        }
                    }
                }
        ) {
            // ---- 파형 / 박스 / 인디케이터 캔버스 ----
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerY = h / 2f
                if (pxPerSec <= 0f) return@Canvas

                fun sx(sec: Float) = w / 2f + (sec - viewCenterSec.value) * pxPerSec

                val stepSec = (barWidthPx + barGapPx) / pxPerSec
                val eps = 0.001f

                var i = 0
                var sec = 0f
                while (sec <= totalDuration.toFloat()) {
                    val x = sx(sec)
                    if (x >= -barWidthPx && x <= w + barWidthPx) {
                        val noise = abs(sin(i * 2.3f) + sin(i * 0.7f) * 0.5f) / 1.5f
                        val barH = with(density) { (18.dp.toPx()) } + noise * with(density) { 46.dp.toPx() }
                        val top = centerY - barH / 2f

                        val inSection = sec >= startSec - eps && sec <= endSec + eps
                        // 재생 인디케이터가 지나간(재생된) 구간 막대는 네온색으로
                        val played = displayPlaySec > startSec - eps && sec <= displayPlaySec + eps
                        val color = when {
                            !inSection -> Color(0xFF454545)
                            played -> mainGreen
                            else -> Color.White
                        }
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x - barWidthPx / 2f, top),
                            size = Size(barWidthPx, barH),
                            cornerRadius = CornerRadius(barWidthPx, barWidthPx)
                        )
                    }
                    i++
                    sec += stepSec
                }

                // 박스 지오메트리 (루프 밴드/테두리/인디케이터 공용)
                val boxLeft = sx(startSec)
                val boxRight = sx(endSec)
                val bH = with(density) { boxHeight.toPx() }
                val boxTop = centerY - bH / 2f
                val strokePx = with(density) { 2.dp.toPx() }
                val rad = with(density) { boxCorner.toPx() }
                val boxPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(boxLeft, boxTop, boxRight, boxTop + bH),
                            cornerRadius = CornerRadius(rad, rad)
                        )
                    )
                }

                // 루프 2초 밴드 하이라이트 (박스 컨테이너 라운드에 맞춰 클립)
                //  - 밴드 밖 선택 구간은 어둡게 덮어 2초 구간만 도드라지게
                //  - 밴드는 펄스로 밝기가 오가고, 양 끝에 경계선을 그려 루프 범위를 명확히
                if (loopSide != null) {
                    val lx = sx(loopStartSec)
                    val rx = sx(loopEndSec)
                    clipPath(boxPath) {
                        // 루프 밖 구간 디밍
                        drawRect(
                            color = Color(0xFF060606).copy(alpha = 0.55f),
                            topLeft = Offset(boxLeft, boxTop),
                            size = Size((lx - boxLeft).coerceAtLeast(0f), bH)
                        )
                        drawRect(
                            color = Color(0xFF060606).copy(alpha = 0.55f),
                            topLeft = Offset(rx, boxTop),
                            size = Size((boxRight - rx).coerceAtLeast(0f), bH)
                        )
                        // 2초 밴드
                        drawRect(
                            color = mainGreen.copy(alpha = 0.22f + 0.20f * loopPulse),
                            topLeft = Offset(lx, boxTop),
                            size = Size((rx - lx).coerceAtLeast(0f), bH)
                        )
                        // 밴드 경계선
                        val edgePx = with(density) { 2.dp.toPx() }
                        drawRect(
                            color = mainGreen,
                            topLeft = Offset(lx, boxTop),
                            size = Size(edgePx, bH)
                        )
                        drawRect(
                            color = mainGreen,
                            topLeft = Offset(rx - edgePx, boxTop),
                            size = Size(edgePx, bH)
                        )
                    }
                }

                // 선택 구간 네온 박스 테두리
                drawRoundRect(
                    color = mainGreen,
                    topLeft = Offset(boxLeft, boxTop),
                    size = Size((boxRight - boxLeft).coerceAtLeast(0f), bH),
                    cornerRadius = CornerRadius(rad, rad),
                    style = Stroke(width = strokePx)
                )

                // 흰색 재생 인디케이터: 박스 컨테이너 path로 클립 → 라운드 코너 근처에선
                //  자동으로 짧아지고 가운데선 박스 높이를 꽉 채움 (고정 높이 아님)
                if (displayPlaySec in startSec..endSec) {
                    val px = sx(displayPlaySec)
                    clipPath(boxPath) {
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(px - strokePx / 2f, boxTop),
                            size = Size(strokePx, bH)
                        )
                    }
                }
            }

            // ---- 좌/우 핸들 (제스처) ----
            HandleView(
                side = HandleSide.LEFT,
                width = handleWidth,
                height = handleHeight,
                corner = handleCorner,
                pressed = pressedSide == HandleSide.LEFT,
                modifier = Modifier
                    .offset {
                        // 박스 컨테이너 안쪽: 핸들 왼쪽 끝이 구간 시작(박스 좌측)에 맞도록
                        // 루프 중에는 밴드가 보이도록 핸들 폭만큼 왼쪽으로 비켜섬
                        IntOffset(
                            (secToX(startSec) + leftHandleShiftPx).roundToInt(),
                            0
                        )
                    }
                    .align(Alignment.CenterStart)
                    .zIndex(10f)
                    .handleGesture(
                        side = HandleSide.LEFT,
                        onPressChange = { s, p -> pressedSide = if (p) s else null },
                        onTap = { latestOnSeek(startSec) },
                        onLoopStart = { activateLoop(it) },
                        onLoopEnd = { deactivateLoop() },
                        onDrag = { s, dx -> applyResize(s, dx) },
                        onDragEnd = {
                            commit(force = true)
                            recenter()
                            // 좌측 핸들 조절이 끝나면 새 시작점부터 다시 미리듣기
                            latestOnSeek(startSec)
                            latestOnHandleAdjusted?.invoke(KillingPartHandle.LEFT)
                        }
                    )
            )

            HandleView(
                side = HandleSide.RIGHT,
                width = handleWidth,
                height = handleHeight,
                corner = handleCorner,
                pressed = pressedSide == HandleSide.RIGHT,
                modifier = Modifier
                    .offset {
                        // 박스 컨테이너 안쪽: 핸들 오른쪽 끝이 구간 끝(박스 우측)에 맞도록
                        // 루프 중에는 밴드가 보이도록 핸들 폭만큼 오른쪽으로 비켜섬
                        IntOffset(
                            (secToX(endSec) - handleWidthPx + rightHandleShiftPx).roundToInt(),
                            0
                        )
                    }
                    .align(Alignment.CenterStart)
                    .zIndex(10f)
                    .handleGesture(
                        side = HandleSide.RIGHT,
                        onPressChange = { s, p -> pressedSide = if (p) s else null },
                        onTap = { /* 우핸들 탭은 동작 없음 */ },
                        onLoopStart = { activateLoop(it) },
                        onLoopEnd = { deactivateLoop() },
                        onDrag = { s, dx -> applyResize(s, dx) },
                        onDragEnd = {
                            commit(force = true)
                            recenter()
                            latestOnHandleAdjusted?.invoke(KillingPartHandle.RIGHT)
                        }
                    )
            )

            // ---- 루프 배지: 2초 밴드 위에 "2초 반복" 표시 ----
            if (loopSide != null && viewportWidthPx > 0f) {
                val bandCenterX = secToX((loopStartSec + loopEndSec) / 2f)
                LoopBadge(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(11f)
                        .offset {
                            IntOffset((bandCenterX - viewportWidthPx / 2f).roundToInt(), 0)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ---- 핸들 시간 라벨 (핸들 위치 따라, 살짝 위로) ----
        val labelHalfPx = with(density) { 38.dp.toPx() }
        Box(modifier = Modifier.fillMaxWidth().height(16.dp)) {
            HandleTimeLabel(
                text = formatTime(startSec),
                modifier = Modifier.offset {
                    IntOffset(
                        (secToX(startSec) - labelHalfPx)
                            .coerceIn(0f, (viewportWidthPx - labelHalfPx * 2f).coerceAtLeast(0f))
                            .roundToInt(),
                        0
                    )
                }
            )
            HandleTimeLabel(
                text = formatTime(endSec),
                modifier = Modifier.offset {
                    IntOffset(
                        (secToX(endSec) - labelHalfPx)
                            .coerceIn(0f, (viewportWidthPx - labelHalfPx * 2f).coerceAtLeast(0f))
                            .roundToInt(),
                        0
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ---- -1s / +1s 버튼: 양쪽 끝 고정 (초 텍스트는 핸들 따라 이동) ----
        // 최대 구간(30s)에 도달했거나 트랙 경계에 닿으면 비활성화
        val durationEps = 0.05f
        val atMaxDuration = (endSec - startSec) >= maxDurationSec - durationEps
        val canExtendFront = !atMaxDuration && startSec > durationEps
        val canExtendBack = !atMaxDuration && endSec < totalDuration.toFloat() - durationEps
        Box(modifier = Modifier.fillMaxWidth().height(edgeButtonSize)) {
            EdgeStepButton(
                label = "-1s",
                size = edgeButtonSize,
                enabled = canExtendFront,
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = { extendFront() }
            )
            EdgeStepButton(
                label = "+1s",
                size = edgeButtonSize,
                enabled = canExtendBack,
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { extendBack() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 미니맵 =====
        MiniMap(
            totalDuration = totalDuration,
            startSec = startSec,
            endSec = endSec,
            onScrub = { newStartSec ->
                val dur = (endSec - startSec)
                val ns = newStartSec.coerceIn(0f, (totalDuration - dur).coerceAtLeast(0f))
                startSec = ns
                endSec = ns + dur
                commit()
                recenter(animated = false)
            },
            onScrubEnd = {
                commit(force = true)
                // 미니맵으로 구간을 옮기면 새 구간 시작부터 다시 재생
                latestOnSeek(startSec)
                latestOnHandleAdjusted?.invoke(KillingPartHandle.SPECTRUM_BAR)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "선택 구간: ${formatTime(startSec)} ~ ${formatTime(endSec)}",
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W500,
                fontSize = 10.sp,
                color = mainGreen
            )
            Text(
                text = "최대 30초",
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.W500,
                fontSize = 10.sp,
                color = mainGreen
            )
        }
    }
}

/** 트랙 위 좌표가 좌/우 핸들 영역(여유 [padPx] 포함) 안인지 */
private fun isOnHandle(
    pos: Offset,
    leftHandleLeftX: Float,
    rightHandleLeftX: Float,
    handleWidthPx: Float,
    handleHeightPx: Float,
    trackHeightPx: Float,
    padPx: Float
): Boolean {
    val top = (trackHeightPx - handleHeightPx) / 2f - padPx
    val bottom = trackHeightPx - top
    if (pos.y < top || pos.y > bottom) return false
    val inLeft = pos.x >= leftHandleLeftX - padPx && pos.x <= leftHandleLeftX + handleWidthPx + padPx
    val inRight = pos.x >= rightHandleLeftX - padPx && pos.x <= rightHandleLeftX + handleWidthPx + padPx
    return inLeft || inRight
}

/** 핸들 시간 라벨 상수 (분석 이벤트 handle_side 값) */
private object KillingPartHandle {
    const val LEFT = "left"
    const val RIGHT = "right"
    const val SPECTRUM_BAR = "spectrum_bar"
}

@Composable
private fun HandleView(
    side: HandleSide,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    corner: androidx.compose.ui.unit.Dp,
    pressed: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (pressed) 1.16f else 1f, label = "handleScale")
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .width(width)
            .height(height)
            .background(mainGreen, RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (side == HandleSide.LEFT) Icons.Filled.KeyboardArrowLeft else Icons.Filled.KeyboardArrowRight,
            contentDescription = if (side == HandleSide.LEFT) "left handle" else "right handle",
            tint = Color(0xFF0A0A0A)
        )
    }
}

/** 롱프레스 2초 루프 중 밴드 위에 뜨는 배지 */
@Composable
private fun LoopBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(15.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(mainGreen)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "2초 반복",
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.W700,
            fontSize = 8.sp,
            color = Color(0xFF0A0A0A)
        )
    }
}

@Composable
private fun HandleTimeLabel(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.width(76.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 11.sp,
            color = Color.White
        )
    }
}

@Composable
private fun EdgeStepButton(
    label: String,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        when {
            !enabled -> 0.3f
            pressed -> 0.5f
            else -> 1f
        },
        label = "stepBtnAlpha"
    )
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { this.alpha = alpha }
            .clip(CircleShape)
            .background(if (enabled) mainGreen else Color(0xFF6E6E6E))
            .then(
                if (enabled) Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            pressed = true
                            tryAwaitRelease()
                            pressed = false
                        },
                        onTap = { onClick() }
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.W700,
            fontSize = 11.sp,
            color = Color(0xFF0A0A0A)
        )
    }
}

@Composable
private fun MiniMap(
    totalDuration: Int,
    startSec: Float,
    endSec: Float,
    onScrub: (newStartSec: Float) -> Unit,
    onScrubEnd: () -> Unit
) {
    val density = LocalDensity.current
    var widthPx by remember { mutableStateOf(0f) }
    val latestStartSec by rememberUpdatedState(startSec)
    val latestEndSec by rememberUpdatedState(endSec)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF1F1F1F), RoundedCornerShape(22.dp))
            .border(1.dp, Color(0xFF3A3A3A), RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .onSizeChanged { widthPx = it.width.toFloat() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (totalDuration <= 0) return@Canvas
            val unitWidth = size.width / totalDuration.toFloat()
            val barSpace = unitWidth * 0.4f
            val miniBarWidth = (unitWidth - barSpace).coerceAtLeast(1f)
            for (i in 0 until totalDuration) {
                val x = i * unitWidth
                val phase = (i.toFloat() / totalDuration.toFloat()) * (Math.PI.toFloat() * 10f)
                val wave = (sin(phase.toDouble()).toFloat() + 1f) / 2f
                val hh = size.height * (0.3f + wave * 0.6f)
                val top = (size.height - hh) / 2f
                drawRoundRect(
                    color = Color(0xFF9A9A9A),
                    topLeft = Offset(x, top),
                    size = Size(miniBarWidth, hh),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }

        val safeTotal = totalDuration.coerceAtLeast(1).toFloat()
        val leftRatio = (startSec / safeTotal).coerceIn(0f, 1f)
        val widthRatio = ((endSec - startSec) / safeTotal).coerceIn(0f, 1f - leftRatio)
        val selLeftPx = widthPx * leftRatio
        val selWidthPx = widthPx * widthRatio

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(density) { selWidthPx.toDp() })
                .graphicsLayer { translationX = selLeftPx }
                .background(mainGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .border(1.dp, mainGreen, RoundedCornerShape(8.dp))
                .pointerInput(widthPx, totalDuration) {
                    var accStartSec = 0f
                    detectDragGestures(
                        onDragStart = { accStartSec = latestStartSec },
                        onDragEnd = { onScrubEnd() },
                        onDragCancel = { onScrubEnd() }
                    ) { change, drag ->
                        change.consume()
                        if (widthPx <= 0f) return@detectDragGestures
                        val dSec = (drag.x / widthPx) * safeTotal
                        accStartSec += dSec
                        onScrub(accStartSec)
                    }
                }
        )
    }
}

/**
 * 핸들 제스처: 탭 / 드래그(리사이즈) / 0.5초 롱프레스(2초 루프)를 하나의 제스처로 판별.
 *  - 0.5초 내 이동(slop 초과) → 드래그
 *  - 0.5초 내 손 뗌 → 탭
 *  - 0.5초 유지 → 루프 활성 (이후 이동 시 루프 해제 후 드래그)
 */
private fun Modifier.handleGesture(
    side: HandleSide,
    onPressChange: (HandleSide, Boolean) -> Unit,
    onTap: (HandleSide) -> Unit,
    onLoopStart: (HandleSide) -> Unit,
    onLoopEnd: () -> Unit,
    onDrag: (HandleSide, Float) -> Unit,
    onDragEnd: () -> Unit
): Modifier = this.pointerInput(side) {
    val slop = viewConfiguration.touchSlop
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        onPressChange(side, true)
        var dragging = false
        var looping = false

        fun changeFor(changes: List<PointerInputChange>): PointerInputChange =
            changes.firstOrNull { it.id == down.id } ?: changes.first()

        // 1단계: 0.5초 내 tap / drag / (timeout=long press) 판별
        val decided: String? = withTimeoutOrNull(500L) {
            var result = "tap"
            while (true) {
                val event = awaitPointerEvent()
                val ch = changeFor(event.changes)
                if (!ch.pressed) { result = "tap"; break }
                if (abs(ch.position.x - down.position.x) > slop) { result = "drag"; break }
            }
            result
        }

        when (decided) {
            null -> {
                // 롱프레스 → 루프 활성
                looping = true
                onLoopStart(side)
            }
            "tap" -> {
                onPressChange(side, false)
                onTap(side)
                return@awaitEachGesture
            }
            "drag" -> {
                dragging = true
            }
        }

        // 2단계: 이후 포인터 추적
        while (true) {
            val event = awaitPointerEvent()
            val ch = changeFor(event.changes)
            if (!ch.pressed) break
            val dx = ch.positionChange().x
            if (looping) {
                // 루프 중 이동하면 루프 해제 후 드래그 전환
                if (abs(ch.position.x - down.position.x) > slop) {
                    looping = false
                    onLoopEnd()
                    dragging = true
                    if (dx != 0f) { onDrag(side, dx); ch.consume() }
                }
            } else if (dragging) {
                if (dx != 0f) { onDrag(side, dx); ch.consume() }
            }
        }

        if (looping) onLoopEnd()
        if (dragging) onDragEnd()
        onPressChange(side, false)
    }
}

@Preview
@Composable
fun KillingPartSelectorPreview() {
    KillingPartSelector(
        totalDuration = 150,
        initialStartSec = 90f,
        initialDurationSec = 26f,
        currentPlaySec = 100f,
        isPlaying = true,
        onStartChange = { _, _, _ -> }
    )
}
