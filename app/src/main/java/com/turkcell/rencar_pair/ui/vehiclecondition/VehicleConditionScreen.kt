package com.turkcell.rencar_pair.ui.vehiclecondition

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.common.photo.PhotoSourceSheet
import com.turkcell.rencar_pair.ui.common.photo.readImageBytes
import com.turkcell.rencar_pair.ui.common.photo.rememberPhotoPicker
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.SuccessBackgroundDark
import com.turkcell.rencar_pair.ui.theme.SuccessBackgroundLight
import com.turkcell.rencar_pair.ui.theme.SuccessStrong
import com.turkcell.rencar_pair.ui.theme.SuccessStrongDark
import com.turkcell.rencar_pair.ui.theme.TextOnPrimary
import com.turkcell.rencar_pair.ui.theme.TextTertiaryDark
import com.turkcell.rencar_pair.ui.theme.TextTertiaryLight
import com.turkcell.rencar_pair.ui.theme.Warning as WarningColor
import com.turkcell.rencar_pair.ui.theme.bodyS
import com.turkcell.rencar_pair.ui.theme.headingXL
import com.turkcell.rencar_pair.ui.theme.labelM
import com.turkcell.rencar_pair.ui.theme.titleL
import com.turkcell.rencar_pair.ui.theme.titleS

@Composable
fun VehicleConditionRoute(
    onBack: () -> Unit,
    onNavigateToActiveRental: (
        rentalId: String,
        vehicleId: String,
        brand: String,
        model: String,
        plate: String,
        pricePerDay: Double,
    ) -> Unit,
    onNavigateToTripSummary: (
        rentalId: String,
        brand: String,
        model: String,
        plate: String,
        durationSeconds: Long,
        distanceMeters: Double,
        totalPrice: Double,
    ) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VehicleConditionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                VehicleConditionEffect.NavigateBack -> onBack()

                is VehicleConditionEffect.NavigateToActiveRental ->
                    onNavigateToActiveRental(
                        effect.rentalId,
                        effect.vehicleId,
                        effect.brand,
                        effect.model,
                        effect.plate,
                        effect.pricePerDay,
                    )

                is VehicleConditionEffect.NavigateToTripSummary ->
                    onNavigateToTripSummary(
                        effect.rentalId,
                        effect.brand,
                        effect.model,
                        effect.plate,
                        effect.durationSeconds,
                        effect.distanceMeters,
                        effect.totalPrice,
                    )

                is VehicleConditionEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    VehicleConditionScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun VehicleConditionScreen(
    state: VehicleConditionUiState,
    onIntent: (VehicleConditionIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val isBefore = state.mode == VehicleConditionMode.BEFORE

    // Sistem/donanim geri tusu da ekrandaki ok ile ayni intent'i tetiklemeli; aksi halde
    // BEFORE modunda PREPARING kiralama iptal edilmeden ekran kapanir (bkz. docs/decisions.md).
    BackHandler { onIntent(VehicleConditionIntent.BackClicked) }
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val textTertiary = if (isDark) TextTertiaryDark else TextTertiaryLight

    // Ehliyet dogrulamadaki foto deseninin ortak bilesene cikarilmis hali
    // (ui/common/photo): yon kartina dokun -> kamera/galeri sec -> gorsel byte'lari
    // PhotoCaptured intent'iyle ViewModel'e akar.
    var pendingSide by remember { mutableStateOf<VehicleSide?>(null) }
    var showSourceSheet by remember { mutableStateOf(false) }
    val photoPicker = rememberPhotoPicker { uri ->
        val side = pendingSide
        pendingSide = null
        if (side != null) {
            val bytes = readImageBytes(context.contentResolver, uri)
            if (bytes != null) {
                onIntent(VehicleConditionIntent.PhotoCaptured(side, bytes))
            }
        }
    }
    val requestPhoto: (VehicleSide) -> Unit = { side ->
        pendingSide = side
        showSourceSheet = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = { onIntent(VehicleConditionIntent.BackClicked) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Araç durumu", style = headingXL, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        text = if (isBefore) {
                            "Başlamadan önce ${state.totalSides} yönü çek"
                        } else {
                            "Teslim etmeden önce ${state.totalSides} yönü çek"
                        },
                        style = bodyS,
                        color = textTertiary,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${state.brand} ${state.model} · ${state.plate}",
                    style = bodyS,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${state.checkedCount} / ${state.totalSides} çekildi",
                    style = titleS,
                    color = Primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                VehicleSidePhotoCard(
                    side = VehicleSide.ON,
                    checked = VehicleSide.ON in state.checkedSides,
                    isUploading = state.uploadingSide == VehicleSide.ON,
                    onClick = { requestPhoto(VehicleSide.ON) },
                    modifier = Modifier.weight(1f),
                )
                VehicleSidePhotoCard(
                    side = VehicleSide.ARKA,
                    checked = VehicleSide.ARKA in state.checkedSides,
                    isUploading = state.uploadingSide == VehicleSide.ARKA,
                    onClick = { requestPhoto(VehicleSide.ARKA) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                VehicleSidePhotoCard(
                    side = VehicleSide.SOL,
                    checked = VehicleSide.SOL in state.checkedSides,
                    isUploading = state.uploadingSide == VehicleSide.SOL,
                    onClick = { requestPhoto(VehicleSide.SOL) },
                    modifier = Modifier.weight(1f),
                )
                VehicleSidePhotoCard(
                    side = VehicleSide.SAG,
                    checked = VehicleSide.SAG in state.checkedSides,
                    isUploading = state.uploadingSide == VehicleSide.SAG,
                    onClick = { requestPhoto(VehicleSide.SAG) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = WarningColor,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Hasarları net çek — teslim sonrası anlaşmazlığı önler.",
                    style = bodyS,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onIntent(VehicleConditionIntent.ConfirmClicked) },
                enabled = state.isConfirmEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = TextOnPrimary),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(color = TextOnPrimary, strokeWidth = 2.dp, modifier = Modifier.height(20.dp))
                } else {
                    val suffix = if (state.checkedCount == state.totalSides) "" else " · ${state.remainingCount} foto kaldı"
                    if (isBefore) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = TextOnPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Kilidi Aç ve Sürüşü Başlat$suffix", style = titleL, color = TextOnPrimary)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextOnPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Kilitle$suffix", style = titleL, color = TextOnPrimary)
                    }
                }
            }
        }
    }

    if (showSourceSheet) {
        PhotoSourceSheet(
            title = "${pendingSide?.label ?: ""} fotoğrafı için yöntem seçin",
            onCameraSelected = {
                showSourceSheet = false
                pendingSide?.let { side ->
                    photoPicker.launchCamera("condition_${side.apiName.lowercase()}.jpg")
                }
            },
            onGallerySelected = {
                showSourceSheet = false
                photoPicker.launchGallery()
            },
            onDismiss = {
                showSourceSheet = false
                pendingSide = null
            },
        )
    }
}

@Composable
private fun VehicleSidePhotoCard(
    side: VehicleSide,
    checked: Boolean,
    isUploading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val successBackground = if (isDark) SuccessBackgroundDark else SuccessBackgroundLight
    val successStrong = if (isDark) SuccessStrongDark else SuccessStrong
    val backgroundColor = if (checked) successBackground else MaterialTheme.colorScheme.surface
    val borderColor = if (checked) successStrong else MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .aspectRatio(1.05f)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable(enabled = !checked && !isUploading, onClick = onClick),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(text = side.label, style = labelM, color = TextOnPrimary)
                }
                if (checked) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(50))
                            .background(successStrong),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (isUploading) {
                    CircularProgressIndicator(
                        color = Primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp),
                    )
                } else if (checked) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = successStrong,
                        modifier = Modifier.size(36.dp),
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Fotoğraf çek",
                                tint = TextOnPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Fotoğraf çek", style = bodyS, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
