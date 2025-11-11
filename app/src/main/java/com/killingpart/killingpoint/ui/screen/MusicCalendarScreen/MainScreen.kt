package com.killingpart.killingpoint.ui.screen.MusicCalendarScreen

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

@Composable
fun MusicCalendarScreen(
    diaries: List<Diary>
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val now = YearMonth.now()
    var currentMonth by remember { mutableStateOf(now) }
    var showMonthPicker by remember { mutableStateOf(false) }
    
    // 최근 3년치의 달 목록 생성 (현재 달부터 역순으로)
    val availableMonths = remember {
        val months = mutableListOf<YearMonth>()
        var month = now
        // 현재 달부터 3년 전까지 (총 37개월: 현재 + 36개월)
        for (i in 0..36) {
            months.add(month)
            month = month.minusMonths(1)
        }
        months
    }
    
    // 일기를 날짜별로 그룹화
    val diariesByDate = remember(diaries) {
        diaries.groupBy { diary ->
            try {
                val datePart = diary.createDate.split("T")[0]
                LocalDate.parse(datePart)
            } catch (e: Exception) {
                null
            }
        }.filterKeys { it != null }.mapKeys { it.key!! }
    }
    
    // 선택된 날짜의 일기
    val selectedDiary = selectedDate?.let { date ->
        diariesByDate[date]?.firstOrNull()
    }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 30.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // 년도 표시
        Text(
            text = "${currentMonth.year}년",
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 월 표시 (드롭다운 아이콘 포함, 클릭 가능)
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
        
        // 월 선택 토글
        if (showMonthPicker) {
            Spacer(modifier = Modifier.height(16.dp))
            var dismissHandler by remember { mutableStateOf<(() -> Unit)?>(null) }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        // 외부 클릭 시 현재 선택된 값으로 확정하고 닫기
                        dismissHandler?.invoke()
                    }
            ) {
                MonthPicker(
                    availableMonths = availableMonths,
                    selectedMonth = currentMonth,
                    onMonthSelected = { month ->
                        currentMonth = month
                        selectedDate = null // 달 변경 시 선택된 날짜 초기화
                    },
                    onDismiss = {
                        showMonthPicker = false
                    },
                    onDismissHandlerReady = { handler ->
                        dismissHandler = handler
                    },
                    modifier = Modifier.clickable(enabled = false) { } // 내부 클릭은 전파 방지
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 요일 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val weekdays = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
            weekdays.forEach { day ->
                Text(
                    text = day,
                    color = when (day) {
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
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 달력 그리드
        CalendarGrid(
            yearMonth = currentMonth,
            diariesByDate = diariesByDate,
            selectedDate = selectedDate,
            onDateClick = { date ->
                selectedDate = if (selectedDate == date) null else date
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 선택된 날짜 구분선 및 표시
        if (selectedDate != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(mainGreen)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${selectedDate!!.dayOfMonth}일",
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 선택된 날짜의 일기 표시
        if (selectedDiary != null) {
            DiaryEntryCard(diary = selectedDiary)
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

@Composable
fun MonthPicker(
    availableMonths: List<YearMonth>,
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
    onDismissHandlerReady: ((() -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 년도와 월을 분리
    val years = availableMonths.map { it.year }.distinct().sortedDescending()
    val months = (1..12).toList()
    
    var selectedYear by remember { mutableStateOf(selectedMonth.year) }
    var selectedMonthValue by remember { mutableStateOf(selectedMonth.monthValue) }
    var previousSelectedMonth by remember { mutableStateOf(selectedMonth) }
    
    // 토글을 닫을 때 호출되는 함수
    val handleDismiss: () -> Unit = {
        // 현재 선택된 값으로 확정
        val finalMonth = YearMonth.of(selectedYear, selectedMonthValue)
        if (availableMonths.contains(finalMonth) && finalMonth != selectedMonth) {
            onMonthSelected(finalMonth)
        }
        onDismiss()
    }
    
    // 외부에서 handleDismiss를 호출할 수 있도록 전달
    LaunchedEffect(handleDismiss) {
        onDismissHandlerReady?.invoke(handleDismiss)
    }
    
    // selectedMonth가 외부에서 변경되면 내부 상태도 업데이트
    LaunchedEffect(selectedMonth) {
        if (selectedMonth != previousSelectedMonth) {
            selectedYear = selectedMonth.year
            selectedMonthValue = selectedMonth.monthValue
            previousSelectedMonth = selectedMonth
        }
    }
    
    // 선택이 실제로 변경되었을 때만 onMonthSelected 호출 (스크롤 완료 후에만)
    LaunchedEffect(selectedYear, selectedMonthValue) {
        kotlinx.coroutines.delay(300) // 스크롤 완료 대기
        val newMonth = YearMonth.of(selectedYear, selectedMonthValue)
        if (newMonth != previousSelectedMonth && availableMonths.contains(newMonth)) {
            previousSelectedMonth = newMonth
            onMonthSelected(newMonth)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Black, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 년도 휠
            WheelPicker(
                items = years.map { "${it}년" },
                selectedIndex = years.indexOf(selectedYear).coerceAtLeast(0),
                onSelected = { index ->
                    selectedYear = years[index]
                },
                modifier = Modifier.weight(1f)
            )
            
            // 월 휠
            WheelPicker(
                items = months.map { "${it}월" },
                selectedIndex = selectedMonthValue - 1,
                onSelected = { index ->
                    selectedMonthValue = months[index]
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        // 중앙 선택 영역 표시선
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
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

@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 40.dp
    val visibleItems = 3 // 보이는 항목 수
    val centerOffset = visibleItems / 2 // 중앙 위치 (1)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (selectedIndex - centerOffset).coerceAtLeast(0))
    val density = LocalDensity.current
    var wasScrolling by remember { mutableStateOf(false) }
    
    // 초기 스크롤 위치 설정
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            val targetIndex = (selectedIndex - centerOffset).coerceIn(0, (items.size - 1).coerceAtLeast(0))
            listState.animateScrollToItem(targetIndex)
        }
    }
    
    // 스크롤 상태 감지 및 스냅
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            wasScrolling = true
        } else if (wasScrolling) {
            // 스크롤이 끝났을 때 중앙에 가장 가까운 항목으로 스냅
            wasScrolling = false
            val firstVisible = listState.firstVisibleItemIndex
            val centerIndex = if (firstVisible == 0) {
                0 // 첫 번째 항목이 보이면 첫 번째 항목 선택
            } else {
                firstVisible + centerOffset
            }
            val targetIndex = centerIndex.coerceIn(0, items.size - 1)
            val scrollToIndex = if (targetIndex == 0) {
                0 // 첫 번째 항목은 스크롤 위치 0
            } else {
                (targetIndex - centerOffset).coerceIn(0, (items.size - 1).coerceAtLeast(0))
            }
            listState.animateScrollToItem(scrollToIndex)
            onSelected(targetIndex)
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
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(items) { index, item ->
                val firstVisibleIndex = listState.firstVisibleItemIndex
                val firstVisibleScrollOffset = listState.firstVisibleItemScrollOffset
                val itemHeightPx = with(density) { itemHeight.toPx() }
                
                // 중앙 인덱스 계산: 첫 번째 보이는 항목 + 중앙 오프셋
                // 단, 첫 번째 항목(인덱스 0)이 보이고 스크롤 오프셋이 작으면 첫 번째 항목이 중앙
                val centerIndex = if (firstVisibleIndex == 0 && firstVisibleScrollOffset < itemHeightPx / 2) {
                    0
                } else {
                    firstVisibleIndex + centerOffset
                }.coerceIn(0, items.size - 1)
                
                val isSelected = index == centerIndex
                val distanceFromCenter = kotlin.math.abs(index - centerIndex)
                
                val alpha = when {
                    distanceFromCenter == 0 -> 1f
                    distanceFromCenter == 1 -> 0.6f
                    else -> 0.3f
                }
                
                Box(
                    modifier = Modifier
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
