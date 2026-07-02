package com.turkcell.rencar_pair.ui.auth.license

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun LicenseRoute(
    onNavigateToNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LicenseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LicenseEffect.NavigateToNext -> onNavigateToNext()
                is LicenseEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    LicenseScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    state: LicenseUiState,
    onIntent: (LicenseIntent) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    var showSelfieSelector by remember { mutableStateOf(false) }

    val frontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onIntent(LicenseIntent.FrontImageSelected(uri))
        }
    }

    val backLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onIntent(LicenseIntent.BackImageSelected(uri))
        }
    }

    val selfieGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onIntent(LicenseIntent.SelfieImageSelected(uri))
        }
    }

    val selfieCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val cacheFile = File(context.cacheDir, "selfie_temp.jpg")
                FileOutputStream(cacheFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                val uri = Uri.fromFile(cacheFile)
                onIntent(LicenseIntent.SelfieImageSelected(uri))
            } catch (_: Exception) { }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Back Button and Page Title Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BackButton(onClick = {
                    if (state.currentStep == LicenseStep.EHLIYET) {
                        onBack()
                    } else {
                        onIntent(LicenseIntent.BackStepClicked)
                    }
                })
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = when (state.currentStep) {
                            LicenseStep.EHLIYET -> "Ehliyet doğrulama"
                            LicenseStep.SELFIE -> "Selfie doğrulaması"
                            LicenseStep.ONAY -> "Doğrulama durumu"
                        },
                        style = headingXL,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 28.sp
                    )
                    Text(
                        text = when (state.currentStep) {
                            LicenseStep.EHLIYET -> "Kiralamadan önce tek seferlik"
                            LicenseStep.SELFIE -> "Yüz benzerlik analizi için selfie yükleyin"
                            LicenseStep.ONAY -> "Doğrulama sürecinizin durumu"
                        },
                        style = bodyS,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Stepper Header
            StepperHeader(currentStep = state.currentStep)

            Spacer(modifier = Modifier.height(32.dp))

            // Step Based Layout
            when (state.currentStep) {
                LicenseStep.EHLIYET -> {
                    // Front License Upload
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Ehliyet ön yüz",
                            style = titleS,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UploadSlot(
                            imageUri = state.frontImageUri,
                            onClick = { frontLauncher.launch("image/*") },
                            placeholderText = "Ön yüzü çek veya yükle"
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Back License Upload
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Ehliyet arka yüz",
                            style = titleS,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UploadSlot(
                            imageUri = state.backImageUri,
                            onClick = { backLauncher.launch("image/*") },
                            placeholderText = "Arka yüzü çek veya yükle"
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    InfoBox(textRes = "Bilgilerin güvenle saklanır. Doğrulama genelde birkaç dakika sürer.")
                }
                LicenseStep.SELFIE -> {
                    // Selfie Upload
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Selfie",
                            style = titleS,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UploadSlot(
                            imageUri = state.selfieImageUri,
                            onClick = { showSelfieSelector = true },
                            placeholderText = "Selfie çek veya yükle"
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    InfoBox(textRes = "Fotoğrafınız ehliyetteki fotoğrafınızla otomatik olarak kıyaslanacaktır.")
                }
                LicenseStep.ONAY -> {
                    // Status view
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (state.status) {
                            "UNDER_REVIEW" -> {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassEmpty,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Belgeleriniz Kontrol Ediliyor",
                                    style = headingL,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )
                                
                                if (state.licenseId != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Belge Numarası: ${state.licenseId}",
                                        style = labelM,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary,
                                        modifier = Modifier
                                            .background(Primary.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Yapay zeka yüz benzerlik analizi gerçekleştiriliyor. Bu işlem birkaç saniye sürebilir.",
                                    style = bodyM,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                // Debug auto-approve simulator button
                                if (state.licenseId != null) {
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Button(
                                        onClick = { onIntent(LicenseIntent.TriggerAutoApprove) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SuccessDefault
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Yapay Zeka Onayını Simüle Et", color = Color.White, style = labelM)
                                    }
                                }
                            }
                            "APPROVED" -> {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(SuccessDefault.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessDefault,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Tebrikler! Ehliyetiniz Onaylandı",
                                    style = headingL,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )

                                if (state.licenseId != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Belge Numarası: ${state.licenseId}",
                                        style = labelM,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessDefault,
                                        modifier = Modifier
                                            .background(SuccessDefault.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Kimliğiniz yapay zeka eşleştirmesiyle başarıyla doğrulandı. Artık araç kiralamaya hazırsınız.",
                                    style = bodyM,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            else -> {
                                // REJECTED or other errors
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(ErrorDefault.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = ErrorDefault,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Ehliyetiniz Onaylanamadı",
                                    style = headingL,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Yüklediğiniz görseller veya selfie eşleşmesi doğrulanamadı. Lütfen belgelerinizi kontrol ederek tekrar yükleyin.",
                                    style = bodyM,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (state.currentStep != LicenseStep.ONAY) {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Bottom Action Button
            val isButtonEnabled = state.isSubmitEnabled && !state.isLoading
            Button(
                onClick = {
                    when (state.currentStep) {
                        LicenseStep.EHLIYET -> {
                            onIntent(LicenseIntent.NextStepClicked)
                        }
                        LicenseStep.SELFIE -> {
                            val frontUri = state.frontImageUri
                            val backUri = state.backImageUri
                            if (frontUri != null && backUri != null) {
                                try {
                                    val frontBytes = contentResolver.openInputStream(frontUri)?.use { it.readBytes() }
                                    val backBytes = contentResolver.openInputStream(backUri)?.use { it.readBytes() }
                                    if (frontBytes != null && backBytes != null) {
                                        onIntent(LicenseIntent.Submit(frontBytes, backBytes))
                                    }
                                } catch (e: Exception) {
                                    onIntent(LicenseIntent.Submit(ByteArray(0), ByteArray(0)))
                                }
                            }
                        }
                        LicenseStep.ONAY -> {
                            if (state.status == "APPROVED") {
                                onIntent(LicenseIntent.NextStepClicked)
                            } else if (state.status == "REJECTED") {
                                // Reset to re-verify
                                onIntent(LicenseIntent.BackStepClicked)
                            }
                        }
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = TextOnPrimary,
                    disabledContainerColor = Primary.copy(alpha = 0.4f),
                    disabledContentColor = TextOnPrimary.copy(alpha = 0.6f),
                ),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = TextOnPrimary,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = when (state.currentStep) {
                            LicenseStep.EHLIYET -> "Devam Et"
                            LicenseStep.SELFIE -> "Gönder ve Devam Et"
                            LicenseStep.ONAY -> if (state.status == "APPROVED") "Devam Et" else "Yeniden Dene"
                        },
                        style = titleL,
                        color = TextOnPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Method selector bottom sheet for Selfie
        if (showSelfieSelector) {
            ModalBottomSheet(
                onDismissRequest = { showSelfieSelector = false },
                containerColor = MaterialTheme.colorScheme.surface,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selfie yükleme yöntemi seçin",
                        style = titleM,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable {
                                showSelfieSelector = false
                                selfieCameraLauncher.launch(null)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Kamera ile çek",
                            style = bodyM,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable {
                                showSelfieSelector = false
                                selfieGalleryLauncher.launch("image/*")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Galeriden seç",
                            style = bodyM,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StepperHeader(currentStep: LicenseStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StepItem(
            number = 1,
            title = "Ehliyet",
            isActive = currentStep == LicenseStep.EHLIYET,
            isCompleted = currentStep > LicenseStep.EHLIYET
        )
        StepDivider()
        StepItem(
            number = 2,
            title = "Selfie",
            isActive = currentStep == LicenseStep.SELFIE,
            isCompleted = currentStep > LicenseStep.SELFIE
        )
        StepDivider()
        StepItem(
            number = 3,
            title = "Onay",
            isActive = currentStep == LicenseStep.ONAY,
            isCompleted = currentStep == LicenseStep.ONAY && false
        )
    }
}

@Composable
private fun StepItem(
    number: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> SuccessDefault
                        isActive -> Primary
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            } else {
                Text(
                    text = number.toString(),
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = labelS,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = title,
            style = labelS,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun RowScope.StepDivider() {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(2.dp)
            .padding(horizontal = 8.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    )
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Geri",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun UploadedRow(
    title: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable(onClick = onClear)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = titleS,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier
                .background(SuccessDefault, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Yüklendi",
                color = Color.White,
                style = labelS,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun UploadSlot(
    imageUri: Uri?,
    onClick: () -> Unit,
    placeholderText: String
) {
    val context = LocalContext.current
    val strokeColor = if (imageUri != null) SuccessDefault else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    val bitmap: Bitmap? = remember(imageUri) {
        if (imageUri == null) null else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .dashedBorder(color = strokeColor, strokeWidth = 1.5f, cornerRadius = 48f)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Semi-transparent overlay to ensure legibility of the green badge
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
                // Green checkmark badge centered
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(SuccessDefault, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Yüklendi",
                        color = Color.White,
                        style = labelM,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = placeholderText,
                    style = bodyS,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun InfoBox(textRes: String) {
    val isDark = isSystemInDarkTheme()
    val boxBg = if (isDark) InfoBackgroundDark else InfoBackgroundLight
    val iconColor = if (isDark) InfoIconDark else InfoIconLight
    val highlightColor = if (isDark) InfoTextDark else InfoTextLight

    val annotatedInfo = buildAnnotatedString {
        if (textRes.contains("birkaç dakika")) {
            append("Bilgilerin güvenle saklanır. Doğrulama genelde ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = highlightColor)) {
                append("birkaç dakika")
            }
            append(" sürer.")
        } else {
            append(textRes)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(boxBg)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = annotatedInfo,
            style = bodyS,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
    }
}

// Custom Modifier for dashed borders
private fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Float = 3f,
    dashLength: Float = 14f,
    gapLength: Float = 10f,
    cornerRadius: Float = 48f
) = this.drawBehind {
    val stroke = Stroke(
        width = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
    )
}
