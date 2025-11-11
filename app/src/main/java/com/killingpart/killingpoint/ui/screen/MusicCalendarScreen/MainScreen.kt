package com.killingpart.killingpoint.ui.screen.MusicCalendarScreen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import java.time.LocalDate
import java.time.YearMonth
import java.time.DayOfWeek

/**
 * MusicCalendarScreen
 * - 음악 일기를 달력 형태로 보여주는 메인 화면
 * - 월 선택, 날짜 선택, 선택된 일기 표시 기능 포함
 */
@Composable
fun MusicCalendarScreen(
    diaries: List<Diary>
) {
    // 현재 선택된 날짜 (null이면 아무것도 선택되지 않은 상태)
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // 현재 표시 중인 달 (기본값: 현재 달)
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // 월 선택기(MonthPicker) 표시 여부
    var showMonthPicker by remember { mutableStateOf(false) }

    // 최근 3년간의 Month 목록 (현재 ~ 36개월 전까지)
    val availableMonths = remember {
        val months = mutableListOf<YearMonth>()
        var m = YearMonth.now()
        repeat(37) {  // 현재 달 포함 37개월
            months.add(m)
            m = m.minusMonths(1)
        }
        months
    }

    // 일기 리스트를 날짜별로 그룹화
    val diariesByDate = remember(diaries) {
        diaries.groupBy {
            runCatching {
                LocalDate.parse(it.createDate.split("T")[0])
            }.getOrNull()
        }.filterKeys { it != null }.mapKeys { it.key!! }
    }

    // 선택된 날짜의 일기
    val selectedDiary = selectedDate?.let { diariesByDate[it]?.firstOrNull() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 스크롤 가능
            .padding(horizontal = 30.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        // 현재 연도 표시
        Text(
            text = "${currentMonth.year}년",
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(4.dp))

        // 현재 월 표시 + 클릭 시 MonthPicker 열기
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { showMonthPicker = !showMonthPicker }
        ) {
            Text(
                text = "${currentMonth.monthValue}월",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "월 선택",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // 월 선택기 표시 (토글)
        // 월 선택 토글
        if (showMonthPicker) {
            Spacer(modifier = Modifier.height(16.dp))

            // 외부 전체를 clickable로 덮지 말고, 그냥 MonthPicker만 표시
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                MonthPicker(
                    availableMonths = availableMonths,
                    selectedMonth = currentMonth,
                    onMonthSelected = { newMonth ->
                        currentMonth = newMonth
                        selectedDate = null // 달 변경 시 선택된 날짜 초기화
                    },
                    onDismiss = {
                        // 그냥 닫기만 담당
                        showMonthPicker = false
                    },
                )
            }
        }


        Spacer(Modifier.height(16.dp))

        // 🔹 요일 헤더 (SUN~SAT)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val days = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
            days.forEach {
                Text(
                    text = it,
                    color = when (it) {
                        "SUN" -> Color(0xFFFF3B30)
                        "SAT" -> Color(0xFF007AFF)
                        else -> Color.White
                    },
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    modifier = Modifier.width(40.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 🔹 달력 그리드 표시
        CalendarGrid(
            yearMonth = currentMonth,
            diariesByDate = diariesByDate,
            selectedDate = selectedDate,
            onDateClick = { date ->
                // 같은 날짜를 다시 클릭하면 선택 해제
                selectedDate = if (selectedDate == date) null else date
            }
        )

        Spacer(Modifier.height(16.dp))

        // 🔹 선택된 날짜 구분선 + 날짜 표시
        if (selectedDate != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(mainGreen)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${selectedDate!!.dayOfMonth}일",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(16.dp))
        }

        // 🔹 선택된 날짜의 일기 표시
        if (selectedDiary != null) {
            DiaryEntryCard(selectedDiary)
        } else if (selectedDate != null) {
            Text(
                text = "이 날짜에 등록된 킬링파트가 없습니다.",
                color = Color(0xFFA4A4A6),
                fontFamily = PaperlogyFontFamily,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * MonthPicker
 * - 년/월 선택 휠
 * - 닫을 때만 onMonthSelected 호출 (중간 변경은 즉시 반영 X)
 */
@Composable
fun MonthPicker(
    availableMonths: List<YearMonth>,
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
    onDismissHandlerReady: ((() -> Unit) -> Unit)? = null, // 사용 안 해도 시그니처는 유지
    onSelectionChanged: ((Int, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 년도와 월을 분리
    val years = availableMonths.map { it.year }.distinct().sortedDescending()
    val months = (1..12).toList()

    var selectedYear by remember { mutableStateOf(selectedMonth.year) }
    var selectedMonthValue by remember { mutableStateOf(selectedMonth.monthValue) }

    // onDismissHandlerReady 안 쓰더라도, 기존 구조 깨기 싫으면 빈 핸들러 넘겨줘도 됨
    LaunchedEffect(Unit) {
        onDismissHandlerReady?.invoke {
            // 필요하면 여기에 "외부에서 닫을 때" 로직 넣을 수 있음
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color.Black, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 년도 휠
                WheelPicker(
                    items = years.map { "${it}년" },
                    selectedIndex = years.indexOf(selectedYear).coerceAtLeast(0),
                    onSelected = { index ->
                        selectedYear = years[index]
                        onSelectionChanged?.invoke(selectedYear, selectedMonthValue)
                    },
                    modifier = Modifier.weight(1f)
                )

                // 월 휠
                WheelPicker(
                    items = months.map { "${it}월" },
                    selectedIndex = selectedMonthValue - 1,
                    onSelected = { index ->
                        selectedMonthValue = months[index]
                        onSelectionChanged?.invoke(selectedYear, selectedMonthValue)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 하단에 "취소 / 확인" 버튼 영역 추가
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "취소",
                    color = Color(0xFFAAAAAA),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable {
                            // 선택 반영 없이 그냥 닫기
                            onDismiss()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "확인",
                    color = mainGreen,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .pointerInput(Unit) {} // 스크롤 소비 방지
                        .clickable {
                            // 현재 선택된 년/월로 확정
                            val newMonth = YearMonth.of(selectedYear, selectedMonthValue)
                            Log.d("MonthPicker", "확인 클릭됨: $newMonth") // ✅ 로그 찍기
                            if (availableMonths.contains(newMonth)) {
                                onMonthSelected(newMonth)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // 중앙 선택 영역 표시선 (기존 그대로 유지)
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 16.dp, vertical = 32.dp) // 위/아래 살짝 여유
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(mainGreen)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(mainGreen)
                )
            }
        }
    }
}


/**
 * WheelPicker
 * - 년도/월 리스트를 휠 형태로 스크롤 선택
 */
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 40.dp
    val visibleItems = 3
    val centerOffset = visibleItems / 2
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (selectedIndex - centerOffset).coerceAtLeast(0))
    val density = LocalDensity.current
    var wasScrolling by remember { mutableStateOf(false) }

    // 🔹 스크롤 끝난 후 중앙 항목 계산
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            wasScrolling = true
        } else if (wasScrolling) {
            wasScrolling = false
            // ✅ 중앙 항목 인덱스를 실제 offset 포함해서 계산
            val itemHeightPx = with(density) { itemHeight.toPx() }
            val scrollOffset = listState.firstVisibleItemScrollOffset.toFloat()
            val offsetRatio = scrollOffset / itemHeightPx

            // 중앙 항목은 현재 보이는 첫 번째 항목 + offset 보정
            val centerIndex = (listState.firstVisibleItemIndex + offsetRatio + centerOffset)
                .toInt()
                .coerceIn(0, items.size - 1)

            onSelected(centerIndex)
            // 중앙 맞추기 위해 살짝 보정 스크롤
            listState.animateScrollToItem((centerIndex - centerOffset).coerceAtLeast(0))
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleItems)
            .clipToBounds()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = itemHeight * centerOffset,
                bottom = itemHeight * centerOffset
            )
        ) {
            itemsIndexed(items) { index, item ->
                val firstVisibleIndex = listState.firstVisibleItemIndex
                val centerIndex = firstVisibleIndex + centerOffset
                val isSelected = index == centerIndex
                val alpha = when (kotlin.math.abs(index - centerIndex)) {
                    0 -> 1f
                    1 -> 0.6f
                    else -> 0.3f
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        color = if (isSelected) Color.White else Color(0xFF6A6B6C),
                        fontFamily = PaperlogyFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = if (isSelected) 18.sp else 16.sp,
                        modifier = Modifier.alpha(alpha)
                    )
                }
            }
        }
    }
}


@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    diariesByDate: Map<LocalDate, List<Diary>>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek
    val daysInMonth = yearMonth.lengthOfMonth()
    
    // 첫 주의 시작 날짜 계산 (일요일부터 시작)
    val startOffset = when (firstDayOfWeek) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var dayCounter = 1
        var weekCounter = 0
        
        while (dayCounter <= daysInMonth || weekCounter == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    if (weekCounter == 0 && dayOfWeek < startOffset) {
                        // 이전 달의 날짜 (빈 칸)
                        Box(modifier = Modifier.size(40.dp))
                    } else if (dayCounter <= daysInMonth) {
                        val date = yearMonth.atDay(dayCounter)
                        val hasDiary = diariesByDate.containsKey(date)
                        val isSelected = selectedDate == date
                        val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
                        val isSaturday = date.dayOfWeek == DayOfWeek.SATURDAY
                        
                        CalendarDayCell(
                            day = dayCounter,
                            hasDiary = hasDiary,
                            isSelected = isSelected,
                            isSunday = isSunday,
                            isSaturday = isSaturday,
                            isCurrentMonth = true,
                            onClick = { onDateClick(date) }
                        )
                        dayCounter++
                    } else {
                        // 다음 달의 날짜 (빈 칸)
                        Box(modifier = Modifier.size(40.dp))
                    }
                }
            }
            weekCounter++
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int,
    hasDiary: Boolean,
    isSelected: Boolean,
    isSunday: Boolean,
    isSaturday: Boolean,
    isCurrentMonth: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (hasDiary && isSelected) mainGreen
                else Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (hasDiary && isSelected) {
            // 선택된 날짜에 일기가 있는 경우: 녹색 배경 + 음악 아이콘
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "음악",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        } else {
            // 일기가 있거나 없는 경우: 날짜 표시
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day.toString(),
                    color = when {
                        isSunday -> Color(0xFFFF3B30)
                        isSaturday -> Color(0xFF007AFF)
                        else -> Color.White
                    },
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                if (hasDiary) {
                    // 일기가 있는 경우 작은 음악 아이콘 표시
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "음악",
                        tint = mainGreen,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DiaryEntryCard(diary: Diary) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 앨범 아트 및 정보 카드
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = diary.albumImageUrl,
                contentDescription = "앨범 아트",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.example_video),
                error = painterResource(id = R.drawable.example_video)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = diary.musicTitle,
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = diary.artist,
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
            
            // 코멘트 읽기 버튼
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* TODO: 코멘트 읽기 기능 */ }
            ) {
                Text(
                    text = "코멘트읽기",
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(id = R.drawable.move),
                    contentDescription = "이동",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 재생 중 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "재생 중",
                color = mainGreen,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = diary.musicTitle,
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "음악",
                tint = mainGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 재생 컨트롤 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.shuffle),
                contentDescription = "셔플",
                modifier = Modifier.size(24.dp)
            )
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color = Color(0xFF161616), RoundedCornerShape(30.dp))
                    .clickable { /* TODO: 이전 곡 */ },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.skip_back),
                    contentDescription = "이전 곡",
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(color = Color.White, RoundedCornerShape(50.dp))
                    .clickable { /* TODO: 재생/일시정지 */ },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pause),
                    contentDescription = "일시정지",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color = Color(0xFF161616), RoundedCornerShape(30.dp))
                    .clickable { /* TODO: 다음 곡 */ },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.skip_fwd),
                    contentDescription = "다음 곡",
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Image(
                painter = painterResource(id = R.drawable.repeat),
                contentDescription = "반복",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
