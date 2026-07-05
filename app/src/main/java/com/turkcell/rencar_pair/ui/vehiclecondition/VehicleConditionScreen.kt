package com.turkcell.rencar_pair.ui.vehiclecondition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.theme.BackgroundDark
import com.turkcell.rencar_pair.ui.theme.BorderSubtleDark
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.SuccessStrongDark
import com.turkcell.rencar_pair.ui.theme.SurfaceDark
import com.turkcell.rencar_pair.ui.theme.SurfaceElevatedDark
import com.turkcell.rencar_pair.ui.theme.TextOnPrimary
import com.turkcell.rencar_pair.ui.theme.TextPrimaryDark
import com.turkcell.rencar_pair.ui.theme.TextSecondaryDark
import com.turkcell.rencar_pair.ui.theme.TextTertiaryDark
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
    modifier: Modifier = Modifier,
    viewModel: VehicleConditionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VehicleConditionEffect.NavigateToActiveRental ->
                    onNavigateToActiveRental(
                        effect.rentalId,
                        effect.vehicleId,
                        effect.brand,
                        effect.model,
                        effect.plate,
                        effect.pricePerDay,
                    )
            }
        }
    }

    VehicleConditionScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun VehicleConditionScreen(
    state: VehicleConditionUiState,
    onIntent: (VehicleConditionIntent) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = BackgroundDark,
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
                        .background(SurfaceElevatedDark)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = TextPrimaryDark,
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Araç durumu", style = headingXL, color = TextPrimaryDark)
                    Text(
                        text = "Başlamadan önce ${state.totalSides} yönü çek",
                        style = bodyS,
                        color = TextTertiaryDark,
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
                    color = TextSecondaryDark,
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
                    onClick = { onIntent(VehicleConditionIntent.PhotoMockCaptured(VehicleSide.ON)) },
                    modifier = Modifier.weight(1f),
                )
                VehicleSidePhotoCard(
                    side = VehicleSide.ARKA,
                    checked = VehicleSide.ARKA in state.checkedSides,
                    onClick = { onIntent(VehicleConditionIntent.PhotoMockCaptured(VehicleSide.ARKA)) },
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
                    onClick = { onIntent(VehicleConditionIntent.PhotoMockCaptured(VehicleSide.SOL)) },
                    modifier = Modifier.weight(1f),
                )
                VehicleSidePhotoCard(
                    side = VehicleSide.SAG,
                    checked = VehicleSide.SAG in state.checkedSides,
                    onClick = { onIntent(VehicleConditionIntent.PhotoMockCaptured(VehicleSide.SAG)) },
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
                    .background(SurfaceDark)
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
                    color = TextSecondaryDark,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onIntent(VehicleConditionIntent.StartRentalClicked) },
                enabled = state.isStartEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = TextOnPrimary),
            ) {
                val suffix = if (state.isStartEnabled) "" else " · ${state.remainingCount} foto kaldı"
                Text(text = "Kiralamayı Başlat$suffix", style = titleL, color = TextOnPrimary)
            }
        }
    }
}

@Composable
private fun VehicleSidePhotoCard(
    side: VehicleSide,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (checked) Color(0xFF17301F) else SurfaceDark
    val borderColor = if (checked) SuccessStrongDark else BorderSubtleDark

    Box(
        modifier = modifier
            .aspectRatio(1.05f)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable(enabled = !checked, onClick = onClick),
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
                    Text(text = side.label, style = labelM, color = TextPrimaryDark)
                }
                if (checked) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(50))
                            .background(SuccessStrongDark),
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
                if (checked) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = SuccessStrongDark,
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
                        Text(text = "Fotoğraf çek", style = bodyS, color = TextSecondaryDark)
                    }
                }
            }
        }
    }
}
