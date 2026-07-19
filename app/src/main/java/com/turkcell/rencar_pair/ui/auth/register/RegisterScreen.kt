package com.turkcell.rencar_pair.ui.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.RencarpairTheme
import com.turkcell.rencar_pair.ui.theme.TextOnPrimary
import com.turkcell.rencar_pair.ui.theme.bodyM
import com.turkcell.rencar_pair.ui.theme.headingXL
import com.turkcell.rencar_pair.ui.theme.labelM
import com.turkcell.rencar_pair.ui.theme.titleL
import com.turkcell.rencar_pair.ui.theme.titleS

@Composable
fun RegisterRoute(
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RegisterEffect.ShowSuccessAndNavigate -> {
                    // Kayit sonrasi kullaniciyi dogrudan giris ekranina yonlendiriyoruz
                    // (snackbar zaten "giris yapin" diyor). Ana ekrana/onboarding'e
                    // atmak yerine login, mesajla tutarli ve dogal akis.
                    snackbarHostState.showSnackbar("Kayıt başarılı! Lütfen giriş yapın.")
                    onNavigateToLogin()
                }
                is RegisterEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    RegisterScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onNavigateToLogin = onNavigateToLogin,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onIntent: (RegisterIntent) -> Unit,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            BackButton(onClick = onBack)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Hesap oluştur",
                style = headingXL,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bilgilerini doldur, saniyeler içinde hazır ol.",
                style = bodyM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            FormField(
                label = "Ad Soyad",
                value = state.fullName,
                onValueChange = { onIntent(RegisterIntent.FullNameChanged(it)) },
                placeholder = "Ahmet Yilmaz",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                testTag = "register_fullname_input",
            )

            Spacer(modifier = Modifier.height(16.dp))

            FormField(
                label = "E-posta",
                value = state.email,
                onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
                placeholder = "ornek@eposta.com",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                testTag = "register_email_input",
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Telefon numarası",
                style = labelM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            PhoneInputRow(
                phoneNumber = state.phone,
                onPhoneNumberChange = { onIntent(RegisterIntent.PhoneChanged(it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                label = "Şifre",
                value = state.password,
                isVisible = state.isPasswordVisible,
                onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
                onToggleVisibility = { onIntent(RegisterIntent.TogglePasswordVisibility) },
                imeAction = ImeAction.Done,
                onDone = { onIntent(RegisterIntent.Submit); keyboardController?.hide() },
                testTag = "register_password_input",
                toggleTestTag = "register_password_toggle",
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onIntent(RegisterIntent.Submit); keyboardController?.hide() },
                enabled = state.isSubmitEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("register_submit_button"),
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
                    Text(text = "Kayıt Ol", style = titleL, color = TextOnPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LoginLinkRow(
                onLoginClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .testTag("register_back_button"),
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
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(text = label, style = labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        val fieldBorder = if (value.isNotEmpty()) Primary else MaterialTheme.colorScheme.outline
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .border(1.dp, fieldBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp)
                .testTag(testTag),
            textStyle = bodyM.copy(color = MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = bodyM,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    isVisible: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    imeAction: ImeAction,
    onDone: () -> Unit,
    testTag: String,
    toggleTestTag: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(text = label, style = labelM, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        val fieldBorder = if (value.isNotEmpty()) Primary else MaterialTheme.colorScheme.outline
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .border(1.dp, fieldBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp)
                .testTag(testTag),
            textStyle = bodyM.copy(color = MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction,
            ),
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = "••••••",
                                style = bodyM,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                        }
                        innerTextField()
                    }
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isVisible) "Gizle" else "Göster",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(onClick = onToggleVisibility)
                            .testTag(toggleTestTag),
                    )
                }
            },
        )
    }
}

@Composable
private fun PhoneInputRow(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeBorder = Primary
    val idleBorder = MaterialTheme.colorScheme.outline

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .height(54.dp)
                .width(90.dp)
                .border(1.dp, idleBorder, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "TR  +90", style = titleS, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.width(8.dp))

        val fieldBorder = if (phoneNumber.isNotEmpty()) activeBorder else idleBorder

        BasicTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .border(1.dp, fieldBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp)
                .testTag("register_phone_input"),
            textStyle = bodyM.copy(color = MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (phoneNumber.isEmpty()) {
                        Text(
                            text = "532 000 00 00",
                            style = bodyM,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun LoginLinkRow(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val annotatedText = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
            append("Zaten hesabın var mı? ")
        }
        withStyle(SpanStyle(color = Primary)) {
            append("Giriş yap")
        }
    }

    androidx.compose.foundation.text.ClickableText(
        text = annotatedText,
        style = bodyM.copy(textAlign = TextAlign.Center),
        modifier = modifier.testTag("register_login_link"),
        onClick = { offset ->
            val start = annotatedText.indexOf("Giriş yap")
            if (offset >= start) onLoginClick()
        },
    )
}

@Preview(showBackground = true, name = "Register - Light")
@Composable
private fun RegisterScreenLightPreview() {
    RencarpairTheme(darkTheme = false) {
        RegisterScreen(
            state = RegisterUiState(fullName = "Ahmet Yilmaz", email = "ahmet@test.com"),
            onIntent = {},
            onBack = {},
            onNavigateToLogin = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C0F14, name = "Register - Dark")
@Composable
private fun RegisterScreenDarkPreview() {
    RencarpairTheme(darkTheme = true) {
        RegisterScreen(
            state = RegisterUiState(),
            onIntent = {},
            onBack = {},
            onNavigateToLogin = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
