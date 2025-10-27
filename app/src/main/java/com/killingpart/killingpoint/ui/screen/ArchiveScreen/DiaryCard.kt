package com.killingpart.killingpoint.ui.screen.ArchiveScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.killingpart.killingpoint.data.model.Diary
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily

@Composable
fun DiaryCard(
    diary: Diary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        // 앨범 이미지
        AsyncImage(
            model = diary.albumImageUrl,
            contentDescription = "Album Art",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 오버레이 그라데이션 (텍스트 가독성을 위해)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.3f)
                )
        )
        
        // 좋아요 수 (좌상단)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.btn_star_big_on),
                contentDescription = "Likes",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "25", // TODO: 실제 좋아요 수로 교체
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
        
        // 공개 범위 아이콘 (우상단)
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
            contentDescription = "Scope",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(16.dp)
        )
        
        // 하단 텍스트 정보
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            // 노래 제목
            Text(
                text = diary.musicTitle,
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // 아티스트명
            Text(
                text = diary.artist,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 날짜
            Text(
                text = diary.createDate ?: "1999.12.12",
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = PaperlogyFontFamily,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiaryCardPreview() {
    //
    val mockDiary = Diary(
        artist = "Michael Jackson",
        musicTitle = "Xscape",
        albumImageUrl = "https://i.scdn.co/image/ab67616d0000b273ff4618b6c70a8518fbd08625",
        content = "목데이터1",
        videoUrl = "https://www.youtube-nocookie.com/embed/ki08IcGubwQ",
        scope = "PRIVATE",
        duration = "string",
        start = "string",
        end = "string",
        createDate = "1999.12.12",
        updateDate = "string"
    )
    
    DiaryCard(
        diary = mockDiary,
        modifier = Modifier.padding(16.dp)
    )
}
