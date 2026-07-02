package com.turkcell.rencar_pair.ui.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.R
import com.turkcell.rencar_pair.ui.theme.BackgroundDark
import com.turkcell.rencar_pair.ui.theme.BackgroundLight
import com.turkcell.rencar_pair.ui.theme.BorderDefaultDark
import com.turkcell.rencar_pair.ui.theme.BorderDefaultLight
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.PrimaryLight
import com.turkcell.rencar_pair.ui.theme.PrimaryOnDark
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import com.turkcell.rencar_pair.ui.theme.TextOnPrimary
import com.turkcell.rencar_pair.ui.theme.TextSecondaryLight
import com.turkcell.rencar_pair.ui.theme.TextTertiaryLight
import com.turkcell.rencar_pair.ui.theme.bodyL
import com.turkcell.rencar_pair.ui.theme.bodyM
import com.turkcell.rencar_pair.ui.theme.displayL
import com.turkcell.rencar_pair.ui.theme.titleL

@Composable
fun OnboardingRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OnboardingEffect.NavigateToHome  -> onNavigateToHome()
                is OnboardingEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    OnboardingScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = !MaterialTheme.colorScheme.background.equals(
        Color(0xFFEEF1F6)
    )
    val glowColor = if (MaterialTheme.colorScheme.background == Color(BackgroundLight.value))
        Primary.copy(alpha = 0.18f)
    else
        Primary.copy(alpha = 0.22f)

    val linkColor = if (MaterialTheme.colorScheme.background == Color(BackgroundLight.value))
        Primary else PrimaryOnDark

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        AppIconWithGlow(glowColor = glowColor)

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Rencar",
            style = displayL,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Yakindaki araci bul,\ndakikalar icinde yola cik.",
            style = bodyL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        PageIndicator(
            currentPage = state.currentPage,
            totalPages = state.totalPages,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onIntent(OnboardingIntent.StartApp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = TextOnPrimary,
            ),
        ) {
            Text(
                text = "Hemen Basla",
                style = titleL,
                color = TextOnPrimary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SecondaryLoginRow(
            linkColor = linkColor,
            onLoginClick = { onIntent(OnboardingIntent.GoToLogin) },
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AppIconWithGlow(
    glowColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(200.dp),
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor, Color.Transparent),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.minDimension / 1.5f,
                        ),
                    )
                },
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryLight, Primary),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    ),
                ),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_car),
                contentDescription = null,
                tint = TextOnPrimary,
                modifier = Modifier.size(52.dp),
            )
        }
    }
}

@Composable
private fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalPages) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(if (isActive) 24.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) Primary
                        else MaterialTheme.colorScheme.outline,
                    ),
            )
        }
    }
}

@Composable
private fun SecondaryLoginRow(
    linkColor: Color,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val annotatedText = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
            append("Zaten hesabim var · ")
        }
        withStyle(SpanStyle(color = linkColor)) {
            append("Giris yap")
        }
    }

    androidx.compose.foundation.text.ClickableText(
        text = annotatedText,
        style = bodyM.copy(textAlign = TextAlign.Center),
        modifier = modifier,
        onClick = { offset ->
            val loginStart = annotatedText.indexOf("Giris yap")
            if (offset >= loginStart) onLoginClick()
        },
    )
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
private fun OnboardingScreenLightPreview() {
    RencarpairTheme(darkTheme = false) {
        OnboardingScreen(
            state = OnboardingUiState(),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0F14, name = "Dark Mode")
@Composable
private fun OnboardingScreenDarkPreview() {
    RencarpairTheme(darkTheme = true) {
        OnboardingScreen(
            state = OnboardingUiState(),
            onIntent = {},
        )
    }
}
