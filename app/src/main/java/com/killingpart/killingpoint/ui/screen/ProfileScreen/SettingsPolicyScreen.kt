package com.killingpart.killingpoint.ui.screen.ProfileScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.killingpart.killingpoint.ui.screen.OnboardingScreen.OnboardingPolicyType
import com.killingpart.killingpoint.ui.screen.OnboardingScreen.PolicyBodyLine
import com.killingpart.killingpoint.ui.screen.OnboardingScreen.formatSectionLines
import com.killingpart.killingpoint.ui.screen.OnboardingScreen.getOnboardingPolicyContent
import com.killingpart.killingpoint.ui.screen.OnboardingScreen.splitPolicySections
import com.killingpart.killingpoint.ui.theme.PaperlogyFontFamily

@Composable
fun SettingsPolicyScreen(
    navController: NavController,
    policyType: OnboardingPolicyType
) {
    val content = getOnboardingPolicyContent(policyType)

    SettingsBackgroundBox {
        SettingsPolicyContent(
            title = content.title,
            body = content.body,
            onBack = { navController.popBackStack() }
        )
    }
}

@Composable
private fun SettingsPolicyContent(
    title: String,
    body: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SettingsBackground)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            SettingsTopBar(
                title = title,
                onBack = onBack
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 24.dp)
        ) {
            items(splitPolicySections(body)) { section ->
                Text(
                    text = section.title,
                    color = Color.White,
                    fontFamily = PaperlogyFontFamily,
                    fontWeight = FontWeight.W700,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                formatSectionLines(section.lines).forEach { line ->
                    when (line) {
                        PolicyBodyLine.Blank -> Spacer(modifier = Modifier.height(8.dp))
                        is PolicyBodyLine.Plain -> PolicyText(line.text)
                        is PolicyBodyLine.Numbered -> PolicyText("${line.index}. ${line.text}")
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun PolicyText(text: String) {
    Text(
        text = text,
        color = Color(0xFFDDDDDD),
        fontFamily = PaperlogyFontFamily,
        fontSize = 10.sp,
        lineHeight = 16.sp
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, widthDp = 360, heightDp = 800)
@Composable
private fun SettingsPolicyScreenPreview() {
    val content = getOnboardingPolicyContent(OnboardingPolicyType.SERVICE_TERMS)
    SettingsBackgroundBox {
        SettingsPolicyContent(
            title = content.title,
            body = content.body,
            onBack = {}
        )
    }
}
