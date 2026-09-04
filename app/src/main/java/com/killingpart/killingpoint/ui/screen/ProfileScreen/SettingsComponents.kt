package com.killingpart.killingpoint.ui.screen.ProfileScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily
import com.killingpart.killingpoint.ui.theme.mainGreen
import org.json.JSONObject

internal val SettingsBackground = Color(0xFF121212)

@Composable
internal fun SettingsBackgroundBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SettingsBackground)
    ) {
        content()
    }
}

@Composable
internal fun SettingsTopBar(
    title: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(top = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color.White
            )
        }
        Text(
            text = title,
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 14.sp
        )
    }
}

@Composable
internal fun EditTopBar(
    title: String,
    actionText: String,
    actionEnabled: Boolean,
    onBack: () -> Unit,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(top = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color.White
            )
        }
        Text(
            text = title,
            color = Color.White,
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 14.sp
        )
        Text(
            text = actionText,
            color = if (actionEnabled) mainGreen else Color(0xFF6A6A6A),
            fontFamily = PaperlogyFontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable(enabled = actionEnabled) { onAction() }
                .padding(8.dp)
        )
    }
}

@Composable
internal fun SettingsListCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SettingsBackground, RoundedCornerShape(8.dp))
    ) {
        content()
    }
}

@Composable
internal fun SettingsTab(
    tabTitle: String,
    detailText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable { onClick() }
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = tabTitle,
            color = Color(0xFFCCCCCC),
            fontFamily = PaperlogyFontFamily,
            fontSize = 12.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (detailText != null) {
                Text(
                    text = detailText,
                    color = Color(0xFF8E8E93),
                    fontFamily = PaperlogyFontFamily,
                    fontSize = 13.sp
                )
            }
            Image(
                painter = painterResource(id = R.drawable.detail_right),
                contentDescription = null,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
internal fun SettingsValueRow(
    tabTitle: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = tabTitle,
            color = Color(0xFFCCCCCC),
            fontFamily = PaperlogyFontFamily,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = Color(0xFF8E8E93),
            fontFamily = PaperlogyFontFamily,
            fontSize = 11.sp
        )
    }
}

@Composable
internal fun SettingsSwitchRow(
    tabTitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = tabTitle,
            color = Color(0xFFCCCCCC),
            fontFamily = PaperlogyFontFamily,
            fontSize = 12.sp
        )
        SettingsPushSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsPushSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) mainGreen else Color(0xFF4A4A4A),
        animationSpec = tween(durationMillis = 180),
        label = "settingsPushSwitchTrackColor"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 18.dp else 2.dp,
        animationSpec = tween(durationMillis = 180),
        label = "settingsPushSwitchThumbOffset"
    )

    Box(
        modifier = Modifier
            .size(width = 38.dp, height = 22.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(18.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White)
        )
    }
}

@Composable
internal fun SettingsDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 12.dp)
            .background(Color(0xFF2A2A2A))
    )
}

@Composable
internal fun SettingsLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFF8A8A8A),
        fontFamily = PaperlogyFontFamily,
        fontSize = 11.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 14.dp)
    )
}

@Composable
internal fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF171717), RoundedCornerShape(12.dp))
            .border(1.dp, mainGreen, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontFamily = PaperlogyFontFamily,
                fontSize = 13.sp
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            ),
            cursorBrush = SolidColor(mainGreen)
        )
    }
}

internal fun parseSettingsApiError(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val jsonStart = raw.indexOf("{")
    if (jsonStart == -1) return raw
    return try {
        val json = JSONObject(raw.substring(jsonStart))
        when {
            json.has("message") -> json.getString("message")
            json.has("fieldErrors") -> {
                val fieldErrors = json.getJSONArray("fieldErrors")
                val messages = mutableListOf<String>()
                for (i in 0 until fieldErrors.length()) {
                    val errorObj = fieldErrors.getJSONObject(i)
                    errorObj.keys().forEach { key ->
                        messages.add(errorObj.getString(key))
                    }
                }
                messages.joinToString("\n").ifBlank { raw }
            }
            else -> raw
        }
    } catch (_: Exception) {
        raw
    }
}
