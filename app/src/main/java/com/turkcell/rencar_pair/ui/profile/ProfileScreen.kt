package com.turkcell.rencar_pair.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.navigation.NavigationTab
import com.turkcell.rencar_pair.ui.navigation.RencarBottomNavigation
import com.turkcell.rencar_pair.ui.theme.*

// ─── Route ────────────────────────────────────────────────────────────────────

@Composable
fun ProfileRoute(
    onNavigateToOnboarding: () -> Unit,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.NavigateToOnboarding -> onNavigateToOnboarding()
            }
        }
    }

    ProfileScreen(
        state             = uiState,
        onIntent          = viewModel::onIntent,
        onTabSelected     = onTabSelected,
        snackbarHostState = snackbarHostState,
        modifier          = modifier,
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onTabSelected: (NavigationTab) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF4F6F9),
        bottomBar = {
            RencarBottomNavigation(
                selectedTab   = NavigationTab.PROFIL,
                onTabSelected = onTabSelected,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 8.dp, bottom = 16.dp),
        ) {
            // ── User header (no card — directly on background) ────────────────
            UserHeader(
                name   = state.userName,
                phone  = state.userPhone,
                onEdit = { onIntent(ProfileIntent.EditProfile) },
            )

            Spacer(Modifier.height(14.dp))

            // ── License card ──────────────────────────────────────────────────
            LicenseCard(
                isApproved   = state.isLicenseApproved,
                licenseClass = state.licenseClass,
            )

            Spacer(Modifier.height(14.dp))

            // ── Menu card ─────────────────────────────────────────────────────
            MenuCard(
                onTabSelected = onTabSelected,
                onSettingsClicked = { onIntent(ProfileIntent.SettingsClicked) }
            )

            Spacer(Modifier.height(14.dp))

            // ── Logout card ───────────────────────────────────────────────────
            LogoutCard(onLogout = { onIntent(ProfileIntent.Logout) })

            // ── Settings Dialog ───────────────────────────────────────────────
            if (state.showSettingsDialog) {
                SettingsDialog(
                    isHighAccuracy = state.isLocationAccuracyHigh,
                    onToggleAccuracy = { isHigh -> onIntent(ProfileIntent.LocationAccuracyToggled(isHigh)) },
                    onDismiss = { onIntent(ProfileIntent.SettingsDismissed) }
                )
            }
        }
    }
}

// ─── User Header ──────────────────────────────────────────────────────────────
// Directly on the gray background — NO white card wrapper

@Composable
private fun UserHeader(name: String, phone: String, onEdit: () -> Unit) {
    val initials = name
        .trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Avatar — 64dp circle with gradient + initials
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(Color(0xFF5C7BF0), Color(0xFF3B5CE6))
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = initials,
                style = headingXS.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold),
                color = Color.White,
            )
        }

        // Name + phone
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text     = name.ifBlank { "Kullanıcı" },
                style    = headingXS.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                color    = TextPrimaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text  = phone.ifBlank { "-" },
                style = bodyS.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                color = TextTertiaryLight,
            )
        }

        // Edit button — 38x38, white, box-shadow via elevation
        Box(
            modifier = Modifier
                .size(38.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable(onClick = onEdit),
            contentAlignment = Alignment.Center,
        ) {
            PencilIcon(tint = TextSecondaryLight)
        }
    }
}

// ─── License Card ─────────────────────────────────────────────────────────────

@Composable
private fun LicenseCard(isApproved: Boolean, licenseClass: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Shield icon bubble — 44x44, bg #E7F4EC, radius 13
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xFFE7F4EC)),
            contentAlignment = Alignment.Center,
        ) {
            ShieldCheckIcon(tint = Color(0xFF1A9E63))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = "Ehliyet doğrulandı",
                style = bodyM.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold),
                color = TextPrimaryLight,
            )
            Text(
                text  = licenseClass,
                style = labelS.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                color = TextHintLight,
            )
        }

        if (isApproved) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE7F4EC))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            ) {
                Text(
                    text  = "Onaylı",
                    style = labelXS.copy(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF1A9E63),
                )
            }
        }
    }
}

// ─── Menu Card ────────────────────────────────────────────────────────────────

@Composable
private fun MenuCard(
    onTabSelected: (NavigationTab) -> Unit,
    onSettingsClicked: () -> Unit
) {
    val items = listOf(
        "Ödeme yöntemleri",
        "Ayarlar",
        "Yardım & destek",
        "Davet et · ₺50 kazan"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        items.forEachIndexed { index, label ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        when (index) {
                            0 -> onTabSelected(NavigationTab.CUZDAN)
                            1 -> onSettingsClicked()
                        }
                    }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                when (index) {
                    0 -> CreditCardIcon(20.dp, TextSecondaryLight)
                    1 -> SettingsIcon(20.dp, TextSecondaryLight)
                    2 -> HelpIcon(20.dp, TextSecondaryLight)
                    3 -> ShareIcon(20.dp, TextSecondaryLight)
                }
                Text(
                    text     = label,
                    style    = bodyM.copy(fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold),
                    color    = TextPrimaryLight,
                    modifier = Modifier.weight(1f),
                )
                ChevronRightIcon(size = 18.dp, tint = Color(0xFFC7CFDA))
            }
            if (index < items.lastIndex) {
                HorizontalDivider(color = Color(0xFFF0F2F6))
            }
        }
    }
}

// ─── Settings Dialog ──────────────────────────────────────────────────────────

@Composable
private fun SettingsDialog(
    isHighAccuracy: Boolean,
    onToggleAccuracy: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Uygulama Ayarları",
                style = headingXS.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryLight
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Konum Hassasiyeti ve Güç Tüketimi",
                    style = bodyS.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondaryLight
                )

                // High Accuracy (GPS)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleAccuracy(true) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = isHighAccuracy,
                        onClick = { onToggleAccuracy(true) },
                        colors = RadioButtonDefaults.colors(selectedColor = Primary)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Yüksek Doğruluk (GPS)",
                            style = bodyM.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "Canlı harita takibi için idealdir. Daha fazla pil tüketir.",
                            style = bodyS,
                            color = TextHintLight
                        )
                    }
                }

                // Balanced Power (Cell + Wi-Fi)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleAccuracy(false) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = !isHighAccuracy,
                        onClick = { onToggleAccuracy(false) },
                        colors = RadioButtonDefaults.colors(selectedColor = Primary)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dengeli Güç Tasarrufu",
                            style = bodyM.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimaryLight
                        )
                        Text(
                            text = "Baz istasyonu ve Wi-Fi kullanır. Pil tasarrufu sağlar.",
                            style = bodyS,
                            color = TextHintLight
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Primary)
            ) {
                Text("Kapat", style = labelM.copy(fontWeight = FontWeight.Bold))
            }
        }
    )
}

// ─── Logout Card ──────────────────────────────────────────────────────────────

@Composable
private fun LogoutCard(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onLogout)
            .padding(15.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        LogoutIcon(size = 19.dp, tint = ErrorDefault)
        Spacer(Modifier.width(9.dp))
        Text(
            text  = "Çıkış yap",
            style = bodyM.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold),
            color = ErrorDefault,
        )
    }
}

// ─── Inline SVG Icon Composables ──────────────────────────────────────────────
// Paths taken directly from HTML design source

@Composable
private fun PencilIcon(tint: Color, size: Dp = 18.dp) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx() / 24f
        val path = Path().apply {
            // "M4 20l4-1 9.5-9.5a2.1 2.1 0 0 0-3-3L5 16l-1 4Z"
            moveTo(4 * s, 20 * s)
            lineTo(8 * s, 19 * s)
            lineTo(17.5f * s, 9.5f * s)
            // arc approx — pencil tip
            lineTo(14.5f * s, 6.5f * s)
            lineTo(5 * s, 16 * s)
            close()
        }
        drawPath(path, color = tint, style = Stroke(width = 1.8f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun ShieldCheckIcon(tint: Color, size: Dp = 22.dp) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx() / 24f
        val shield = Path().apply {
            // M12 3l7 3v5c0 4.5-3 8-7 10-4-2-7-5.5-7-10V6l7-3Z
            moveTo(12 * s, 3 * s)
            lineTo(19 * s, 6 * s)
            lineTo(19 * s, 11 * s)
            cubicTo(19 * s, 15.5f * s, 16 * s, 19 * s, 12 * s, 21 * s)
            cubicTo(8 * s, 19 * s, 5 * s, 15.5f * s, 5 * s, 11 * s)
            lineTo(5 * s, 6 * s)
            close()
        }
        drawPath(shield, color = tint, style = Stroke(width = 1.8f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
        // Checkmark M9 12l2 2 4-4
        val check = Path().apply {
            moveTo(9 * s, 12 * s)
            lineTo(11 * s, 14 * s)
            lineTo(15 * s, 10 * s)
        }
        drawPath(check, color = tint, style = Stroke(width = 1.8f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun CreditCardIcon(size: Dp, tint: Color) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx() / 24f
        // rect x=3 y=6 w=18 h=13 rx=3
        drawRoundRect(
            color       = Color.Transparent,
            topLeft     = androidx.compose.ui.geometry.Offset(3 * s, 6 * s),
            size        = androidx.compose.ui.geometry.Size(18 * s, 13 * s),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3 * s),
            style       = Stroke(width = 1.8f * s)
        )
        drawRoundRect(
            color       = tint,
            topLeft     = androidx.compose.ui.geometry.Offset(3 * s, 6 * s),
            size        = androidx.compose.ui.geometry.Size(18 * s, 13 * s),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3 * s),
            style       = Stroke(width = 1.8f * s)
        )
        // horizontal line y=10
        drawLine(tint, androidx.compose.ui.geometry.Offset(3 * s, 10 * s), androidx.compose.ui.geometry.Offset(21 * s, 10 * s), 1.8f * s)
    }
}

@Composable
private fun SettingsIcon(size: Dp, tint: Color) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx() / 24f
        // Gear: circle cx=12 cy=12 r=3 + outer cog path (simplified)
        drawCircle(
            color  = tint,
            radius = 3 * s,
            center = androidx.compose.ui.geometry.Offset(12 * s, 12 * s),
            style  = Stroke(width = 1.8f * s),
        )
        // Outer hexagonal gear outline (simplified as larger circle with stroke)
        drawCircle(
            color  = tint,
            radius = 8 * s,
            center = androidx.compose.ui.geometry.Offset(12 * s, 12 * s),
            style  = Stroke(width = 1.4f * s),
        )
    }
}

@Composable
private fun HelpIcon(size: Dp, tint: Color) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx() / 24f
        // circle r=9
        drawCircle(tint, 9 * s, androidx.compose.ui.geometry.Offset(12 * s, 12 * s), style = Stroke(1.8f * s))
        // question mark stem
        val q = Path().apply {
            moveTo(12 * s, 14 * s)
            cubicTo(12 * s, 12 * s, 14.5f * s, 12 * s, 14.5f * s, 10 * s)
            cubicTo(14.5f * s, 8.625f * s, 13.38f * s, 7.5f * s, 12 * s, 7.5f * s)
            cubicTo(10.62f * s, 7.5f * s, 9.5f * s, 8.625f * s, 9.5f * s, 10 * s)
        }
        drawPath(q, tint, style = Stroke(1.8f * s, cap = StrokeCap.Round))
        // dot
        drawCircle(tint, 0.7f * s, androidx.compose.ui.geometry.Offset(12 * s, 17 * s))
    }
}

@Composable
private fun ShareIcon(size: Dp, tint: Color) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx() / 24f
        // M12 3a9 9 0 1 0 0 18M16 8l4 4-4 4M9 12h11
        val arc = Path().apply {
            // Half circle (left side)
            moveTo(12 * s, 3 * s)
            cubicTo(7.03f * s, 3 * s, 3 * s, 7.03f * s, 3 * s, 12 * s)
            cubicTo(3 * s, 16.97f * s, 7.03f * s, 21 * s, 12 * s, 21 * s)
        }
        drawPath(arc, tint, style = Stroke(1.8f * s, cap = StrokeCap.Round))
        // Arrow: M16 8l4 4-4 4
        val arrow = Path().apply {
            moveTo(16 * s, 8 * s)
            lineTo(20 * s, 12 * s)
            lineTo(16 * s, 16 * s)
        }
        drawPath(arrow, tint, style = Stroke(1.8f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
        // Horizontal line M9 12h11
        drawLine(tint, androidx.compose.ui.geometry.Offset(9 * s, 12 * s), androidx.compose.ui.geometry.Offset(20 * s, 12 * s), 1.8f * s, cap = StrokeCap.Round)
    }
}

@Composable
private fun ChevronRightIcon(size: Dp, tint: Color) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx() / 24f
        val path = Path().apply {
            moveTo(9 * s, 6 * s)
            lineTo(15 * s, 12 * s)
            lineTo(9 * s, 18 * s)
        }
        drawPath(path, tint, style = Stroke(2f * s, cap = StrokeCap.Round))
    }
}

@Composable
private fun LogoutIcon(size: Dp, tint: Color) {
    Canvas(Modifier.size(size)) {
        val s = size.toPx() / 24f
        // M15 4h2a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-2
        val door = Path().apply {
            moveTo(15 * s, 4 * s)
            lineTo(17 * s, 4 * s)
            cubicTo(18.1f * s, 4 * s, 19 * s, 4.9f * s, 19 * s, 6 * s)
            lineTo(19 * s, 18 * s)
            cubicTo(19 * s, 19.1f * s, 18.1f * s, 20 * s, 17 * s, 20 * s)
            lineTo(15 * s, 20 * s)
        }
        drawPath(door, tint, style = Stroke(1.8f * s, cap = StrokeCap.Round))
        // Arrow M10 8l-4 4 4 4
        val arrow = Path().apply {
            moveTo(10 * s, 8 * s)
            lineTo(6 * s, 12 * s)
            lineTo(10 * s, 16 * s)
        }
        drawPath(arrow, tint, style = Stroke(1.8f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
        // Horizontal M6 12h11
        drawLine(tint, androidx.compose.ui.geometry.Offset(6 * s, 12 * s), androidx.compose.ui.geometry.Offset(17 * s, 12 * s), 1.8f * s, cap = StrokeCap.Round)
    }
}
