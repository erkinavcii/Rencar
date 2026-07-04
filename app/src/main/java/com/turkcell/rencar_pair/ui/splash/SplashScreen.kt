package com.turkcell.rencar_pair.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.R
import com.turkcell.rencar_pair.ui.theme.BackgroundLight
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.PrimaryLight
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import com.turkcell.rencar_pair.ui.theme.TextOnPrimary
import com.turkcell.rencar_pair.ui.theme.bodyM
import com.turkcell.rencar_pair.ui.theme.displayL

@Composable
fun SplashRoute(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SplashEffect.Finished -> onSplashFinished()
            }
        }
    }

    SplashScreen(state = uiState, modifier = modifier)
}

@Composable
fun SplashScreen(
    state: SplashUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        SplashLogo()
    }
}

@Composable
private fun SplashLogo(modifier: Modifier = Modifier) {
    val glowColor = if (MaterialTheme.colorScheme.background == Color(BackgroundLight.value))
        Primary.copy(alpha = 0.20f)
    else
        Primary.copy(alpha = 0.28f)

    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        )
    }

    val pulseTransition = rememberInfiniteTransition(label = "splash-pulse")
    val ringProgressA by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
        ),
        label = "ring-a",
    )
    val ringProgressB by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, delayMillis = 800, easing = LinearEasing),
        ),
        label = "ring-b",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer {
                    val scale = 0.85f + 0.15f * entrance.value
                    scaleX = scale
                    scaleY = scale
                    alpha = entrance.value
                }
                .drawBehind {
                    drawPulseRing(glowColor, ringProgressA)
                    drawPulseRing(glowColor, ringProgressB)
                },
        ) {
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Rencar",
            style = displayL,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer { alpha = entrance.value },
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Yakındaki aracı bul, dakikalar içinde yola çık.",
            style = bodyM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer { alpha = entrance.value },
        )
    }
}

private fun DrawScope.drawPulseRing(color: Color, progress: Float) {
    val maxRadius = size.minDimension / 2f
    val radius = maxRadius * (0.35f + 0.65f * progress)
    val fade = (1f - progress).coerceIn(0f, 1f)
    drawCircle(
        color = color.copy(alpha = color.alpha * fade),
        radius = radius,
        center = Offset(size.width / 2f, size.height / 2f),
        style = Stroke(width = 3.dp.toPx()),
    )
}

@Preview(showBackground = true, name = "Splash - Light")
@Composable
private fun SplashScreenLightPreview() {
    RencarpairTheme(darkTheme = false) {
        SplashScreen(state = SplashUiState())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0F14, name = "Splash - Dark")
@Composable
private fun SplashScreenDarkPreview() {
    RencarpairTheme(darkTheme = true) {
        SplashScreen(state = SplashUiState())
    }
}
