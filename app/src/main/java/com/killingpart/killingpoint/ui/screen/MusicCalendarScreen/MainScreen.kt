package com.killingpart.killingpoint.ui.screen.MusicCalendarScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
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
        
        // 월 표시 (드롭다운 아이콘 포함)
        Row(
            verticalAlignment = Alignment.CenterVertically
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
