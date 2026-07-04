package com.turkcell.rencar_pair.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.rencar_pair.data.wallet.PaymentCard
import com.turkcell.rencar_pair.data.wallet.WalletTransaction
import com.turkcell.rencar_pair.ui.navigation.NavigationTab
import com.turkcell.rencar_pair.ui.navigation.RencarBottomNavigation
import com.turkcell.rencar_pair.ui.theme.*

// ─── Route ────────────────────────────────────────────────────────────────────

@Composable
fun WalletRoute(
    onBack: () -> Unit,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WalletEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is WalletEffect.NavigateBack -> onBack()
            }
        }
    }

    WalletScreen(
        state             = uiState,
        onIntent          = viewModel::onIntent,
        onTabSelected     = onTabSelected,
        snackbarHostState = snackbarHostState,
        modifier          = modifier,
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    state: WalletUiState,
    onIntent: (WalletIntent) -> Unit,
    onTabSelected: (NavigationTab) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text   = "Cüzdan",
                        style  = headingXL,
                        color  = TextPrimaryLight,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                ),
            )
        },
        bottomBar = {
            RencarBottomNavigation(
                selectedTab   = NavigationTab.CUZDAN,
                onTabSelected = onTabSelected,
            )
        },
        containerColor = BackgroundLight,
        modifier = modifier,
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            // ── Balance Card ─────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                BalanceCard(
                    balance = state.balance,
                    onLoadClick = { onIntent(WalletIntent.ToggleLoadBalanceDialog) },
                )
                Spacer(Modifier.height(20.dp))
            }

            // ── Saved Cards header ───────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text  = "Kayıtlı kartlar",
                        style = titleM.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimaryLight,
                    )
                    Text(
                        text     = "+ Ekle",
                        style    = titleS,
                        color    = Primary,
                        modifier = Modifier.clickable { onIntent(WalletIntent.ToggleAddCardDialog) },
                    )
                }
                Spacer(Modifier.height(11.dp))
            }

            // ── Card items ───────────────────────────────────────────────────
            items(state.cards) { card ->
                SavedCardRow(card)
                Spacer(Modifier.height(10.dp))
            }

            // ── Recent transactions header ────────────────────────────────────
            item {
                Spacer(Modifier.height(10.dp))
                Text(
                    text  = "Son işlemler",
                    style = titleM.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryLight,
                )
                Spacer(Modifier.height(11.dp))
            }

            // ── Transaction container (white card with dividers) ──────────────
            if (state.transactions.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                    ) {
                        state.transactions.forEachIndexed { index, tx ->
                            TransactionRow(tx)
                            if (index < state.transactions.lastIndex) {
                                HorizontalDivider(color = DividerLight)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            } else {
                item {
                    Text(
                        text      = "Henüz işlem bulunmuyor.",
                        style     = bodyM,
                        color     = TextHintLight,
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }

    // ── Add Card bottom sheet ─────────────────────────────────────────────────
    if (state.showAddCardDialog) {
        ModalBottomSheet(
            onDismissRequest = { onIntent(WalletIntent.ToggleAddCardDialog) },
            containerColor   = BackgroundLight,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
            ) {
                // ── Sheet title
                Text(
                    text     = "Yeni Kart Ekle",
                    style    = titleM.copy(fontWeight = FontWeight.Bold),
                    color    = TextPrimaryLight,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )

                // ── Live card preview
                LiveCardPreview(
                    cardNoRaw    = state.cardNoRaw,
                    expiry       = state.cardExpiryInput,
                    holderName   = state.cardHolderInput,
                    modifier     = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                )

                Spacer(Modifier.height(24.dp))

                // ── Input fields on white surface
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Card number field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Kart Numarası", style = labelS.copy(fontWeight = FontWeight.Bold), color = TextTertiaryLight)
                        OutlinedTextField(
                            value         = state.cardNoInput,
                            onValueChange = { onIntent(WalletIntent.CardNoChanged(it)) },
                            placeholder   = { Text("0000  0000  0000  0000", color = TextHintLight) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine    = true,
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = Primary,
                                unfocusedBorderColor = BorderDefaultLight,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // Expiry + Holder name side by side
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Son Kullanma", style = labelS.copy(fontWeight = FontWeight.Bold), color = TextTertiaryLight)
                            OutlinedTextField(
                                value         = state.cardExpiryInput,
                                onValueChange = { onIntent(WalletIntent.CardExpiryChanged(it)) },
                                placeholder   = { Text("MM/YY", color = TextHintLight) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine    = true,
                                shape         = RoundedCornerShape(12.dp),
                                colors        = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor   = Primary,
                                    unfocusedBorderColor = BorderDefaultLight,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Kart Sahibi", style = labelS.copy(fontWeight = FontWeight.Bold), color = TextTertiaryLight)
                            OutlinedTextField(
                                value         = state.cardHolderInput,
                                onValueChange = { onIntent(WalletIntent.CardHolderChanged(it)) },
                                placeholder   = { Text("Ad Soyad", color = TextHintLight) },
                                singleLine    = true,
                                shape         = RoundedCornerShape(12.dp),
                                colors        = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor   = Primary,
                                    unfocusedBorderColor = BorderDefaultLight,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick  = { onIntent(WalletIntent.SubmitAddCard) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text("Kartı Kaydet", style = titleL)
                    }
                }
            }
        }
    }


    // ── Load Balance bottom sheet ─────────────────────────────────────────────
    if (state.showLoadBalanceDialog) {
        ModalBottomSheet(
            onDismissRequest = { onIntent(WalletIntent.ToggleLoadBalanceDialog) },
            containerColor   = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Bakiye Yükle", style = titleM.copy(fontWeight = FontWeight.Bold), color = TextPrimaryLight)
                OutlinedTextField(
                    value         = state.loadAmountInput,
                    onValueChange = { onIntent(WalletIntent.LoadAmountChanged(it)) },
                    label         = { Text("Yüklenecek Tutar (TL)") },
                    placeholder   = { Text("200") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier      = Modifier.fillMaxWidth(),
                )
                Text("Kullanılacak kart", style = bodyS.copy(fontWeight = FontWeight.Bold), color = TextPrimaryLight)
                state.cards.forEach { card ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (state.selectedCardId == card.id) Primary.copy(alpha = 0.08f)
                                else Color(0xFFF4F6F9)
                            )
                            .clickable { onIntent(WalletIntent.SelectCardForLoad(card.id)) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CardBadge(type = card.type)
                            Text(card.number, style = titleS, color = TextPrimaryLight)
                        }
                        RadioButton(
                            selected = state.selectedCardId == card.id,
                            onClick  = { onIntent(WalletIntent.SelectCardForLoad(card.id)) },
                            colors   = RadioButtonDefaults.colors(selectedColor = Primary),
                        )
                    }
                }
                Button(
                    onClick  = { onIntent(WalletIntent.SubmitLoadBalance) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Primary),
                ) { Text("Yüklemeyi Onayla", style = titleL) }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── Balance Card ──────────────────────────────────────────────────────────────

@Composable
private fun BalanceCard(balance: Double, onLoadClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1E7FE0), Color(0xFF0B6BCB)),
                    start  = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end    = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .padding(20.dp),
    ) {
        // Decorative circle top-right
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-30).dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape),
        )

        Column {
            Text(
                text  = "Rencar bakiyesi",
                style = bodyS,
                color = Color.White.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text   = "₺${String.format("%,.2f", balance).replace(",", ".")}",
                style  = displayM.copy(fontSize = 34.sp, letterSpacing = (-1).sp),
                color  = Color.White,
            )
            Spacer(Modifier.height(18.dp))
            // Bakiye Yükle frosted button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable(onClick = onLoadClick),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Default.Add,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(18.dp),
                    )
                    Text(
                        text  = "Bakiye Yükle",
                        style = titleS,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

// ─── Saved Card Row ────────────────────────────────────────────────────────────

@Composable
private fun SavedCardRow(card: PaymentCard) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CardBadge(type = card.type)
            Column {
                Text(
                    text  = card.number,
                    style = titleS,
                    color = TextPrimaryLight,
                )
                Text(
                    text  = "Son kullanma ${card.expiryDate}",
                    style = labelS.copy(fontSize = 11.5.sp),
                    color = TextHintLight,
                )
            }
        }
        if (card.isDefault) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SuccessBackgroundLight)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text  = "Varsayılan",
                    style = labelMicro.copy(fontWeight = FontWeight.ExtraBold),
                    color = SuccessStrong,
                )
            }
        }
    }
}

// ─── Card Badge (VISA / MC gradient pill) ─────────────────────────────────────

@Composable
private fun CardBadge(type: String) {
    val gradient = if (type == "VISA") {
        Brush.linearGradient(listOf(Color(0xFF1A1F71), Color(0xFF0B6BCB)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFEB001B), Color(0xFFF79E1B)))
    }
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(gradient),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = if (type == "VISA") "VISA" else "MC",
            color = Color.White,
            style = labelXS.copy(
                fontSize   = if (type == "VISA") 9.sp else 8.sp,
                fontWeight = FontWeight.ExtraBold,
            ),
        )
    }
}

// ─── Transaction Row ───────────────────────────────────────────────────────────

@Composable
private fun TransactionRow(tx: WalletTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icon bubble
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (tx.isIncome) SuccessBackgroundLight else ErrorBackgroundLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = if (tx.isIncome) Icons.Default.Add else Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint               = if (tx.isIncome) SuccessStrong else ErrorDefault,
                    modifier           = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text  = tx.title,
                    style = bodyM.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryLight,
                )
                Text(
                    text  = tx.date,
                    style = labelS.copy(fontSize = 11.5.sp),
                    color = TextHintLight,
                )
            }
        }
        // Amount
        Text(
            text  = "${if (tx.isIncome) "+" else "-"}₺${String.format("%.2f", tx.amount)}",
            style = titleS.copy(fontWeight = FontWeight.ExtraBold),
            color = if (tx.isIncome) SuccessStrong else TextPrimaryLight,
        )
    }
}

// ─── Live Card Preview ────────────────────────────────────────────────────────

@Composable
fun LiveCardPreview(
    cardNoRaw: String,
    expiry: String,
    holderName: String,
    modifier: Modifier = Modifier,
) {
    // Detect card type from first digit
    val isVisa = cardNoRaw.startsWith("4")
    val isMc   = cardNoRaw.startsWith("5")

    // Build displayed number: actual digits + bullet placeholders grouped by 4
    val groups = (0 until 4).map { groupIndex ->
        val start = groupIndex * 4
        val end   = minOf(start + 4, cardNoRaw.length)
        val typed = if (start < cardNoRaw.length) cardNoRaw.substring(start, end) else ""
        val dots  = "•".repeat(4 - typed.length)
        typed + dots
    }
    val displayNumber = groups.joinToString("  ")
    val displayExpiry = expiry.ifBlank { "MM/YY" }
    val displayHolder = holderName.uppercase().ifBlank { "AD SOYAD" }

    Box(
        modifier = modifier
            .aspectRatio(1.586f)          // standard ISO 7810 card ratio
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E7FE0),
                        Color(0xFF0A56A8),
                        Color(0xFF0B3F80),
                    ),
                    start = Offset(0f, 0f),
                    end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            ),
    ) {
        // Decorative circles
        Box(
            Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            Modifier
                .size(120.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 40.dp)
                .background(Color.White.copy(alpha = 0.06f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // ── Top row: chip + network logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // EMV Chip
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 26.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFD4A843), Color(0xFFF5D67A), Color(0xFFD4A843))
                            )
                        ),
                )
                // Network name
                Text(
                    text  = when {
                        isVisa -> "VISA"
                        isMc   -> "MC"
                        else   -> ""
                    },
                    style = titleL.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontSize = if (isVisa) 15.sp else 13.sp,
                    ),
                    color = Color.White,
                )
            }

            // ── Card number
            Text(
                text  = displayNumber,
                style = titleM.copy(
                    fontWeight   = FontWeight.Bold,
                    fontSize     = 17.sp,
                    letterSpacing = 2.sp,
                ),
                color = Color.White,
            )

            // ── Bottom row: holder + expiry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text  = "KART SAHİBİ",
                        style = labelXS.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Text(
                        text  = displayHolder,
                        style = labelM.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text  = "SON KULLANMA",
                        style = labelXS.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Text(
                        text  = displayExpiry,
                        style = labelM.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
            }
        }
    }
}
