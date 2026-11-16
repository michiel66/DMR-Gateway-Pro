package io.github.korppujauho.pocketts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.korppujauho.pocketts.Session
import io.github.korppujauho.pocketts.VoiceSettings
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

/*
 * DMR Gateway Pro - independently branded Android presentation.
 * 2026-11-16. PocketTS audio, protocol, PTT and session handling remain intact.
 * GPL-3.0-or-later; original PocketTS copyright notices remain in the source.
 */
private val ElectricBlue = Color(0xFF008EFF)
private val CyanGlow = Color(0xFF17C6FF)
private val AmberGlow = Color(0xFFFF9100)
private val DarkBlue = Color(0xFF05101F)
private val DeepPanel = Color(0xFF07182C)
private val LightText = Color(0xFFE4F4FF)

@Composable
fun GatewayDashboard(
    state: Session.UiState,
    actions: ServerActions,
    view: View,
    onView: (View) -> Unit,
    onShare: () -> Unit,
    channelList: @Composable () -> Unit,
    chatPanel: @Composable () -> Unit,
) {
    val connected = state.phase == Session.Phase.CONNECTED
    val pttMode = state.voice.activation == VoiceSettings.Activation.PTT
    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    BoxWithConstraints(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF020711), Color(0xFF071D31), Color(0xFF01050D))))
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        val compact = maxHeight < 705.dp
        val narrowScreen = maxWidth < 365.dp
        val buttonSize = when {
            maxHeight < 545.dp -> 108.dp
            maxHeight < 645.dp -> 132.dp
            compact -> 150.dp
            else -> 174.dp
        }
        val space = if (compact) 6.dp else 9.dp

        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(space)) {
            Row(
                Modifier.fillMaxWidth().height(if (compact) 51.dp else 58.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GatewayQuickButton(
                    if (state.inputMuted || !state.micAllowed) Icons.Filled.MicOff else Icons.Filled.Mic,
                    "Microfoon", alert = state.inputMuted || !state.micAllowed,
                    onClick = actions::toggleMic,
                )
                GatewayQuickButton(
                    if (state.outputMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                    "Geluid", alert = state.outputMuted, onClick = actions::toggleSound,
                )
                GatewayQuickButton(Icons.Filled.ScreenShare, "Scherm delen", onClick = onShare)
                GatewayQuickButton(Icons.Filled.People, "Gebruikers", onClick = { onView(View.CHANNELS) })
                GatewayQuickButton(Icons.Filled.ChatBubble, "Chat", onClick = { onView(View.CHAT) })
            }

            Row(
                Modifier.fillMaxWidth().height(if (compact) 44.dp else 51.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepPanel)
                    .border(1.dp, ElectricBlue.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
            ) {
                GatewayTab("Kanalen", active = view == View.CHANNELS, modifier = Modifier.weight(1f)) { onView(View.CHANNELS) }
                GatewayTab("Chat", active = view == View.CHAT, modifier = Modifier.weight(1f)) { onView(View.CHAT) }
            }

            Column(
                Modifier.fillMaxWidth().weight(1f)
                    .shadow(7.dp, RoundedCornerShape(19.dp), ambientColor = ElectricBlue.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(19.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF071C32), Color(0xFF03101F))))
                    .border(1.5.dp, ElectricBlue, RoundedCornerShape(19.dp)),
            ) {
                Row(
                    Modifier.fillMaxWidth().height(49.dp)
                        .background(Brush.horizontalGradient(listOf(Color(0xFF002A51), Color(0xFF071A2F), Color(0xFF002A51))))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Headset, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(9.dp))
                    Text(
                        "COMMUNICATION NETWORK", color = LightText, fontWeight = FontWeight.Bold,
                        fontSize = if (narrowScreen) 13.sp else 15.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(if (connected) "●" else "○", color = if (connected) Color(0xFF28FF63) else AmberGlow, fontSize = 17.sp)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(CyanGlow.copy(alpha = 0.75f)))
                if (state.serverName.isNotBlank()) {
                    Text(
                        state.serverName, color = CyanGlow.copy(alpha = 0.9f), fontSize = 11.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 5.dp),
                    )
                }
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    if (view == View.CHANNELS) channelList() else chatPanel()
                }
            }

            if (!keyboardOpen) {
                Row(
                    Modifier.fillMaxWidth().height(buttonSize + 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    GatewaySideButton(Icons.Filled.People, "Gebruikers", Modifier.weight(1f)) { onView(View.CHANNELS) }
                    Spacer(Modifier.width(8.dp))
                    GatewayPttButton(
                        size = buttonSize,
                        held = state.pttHeld,
                        enabled = connected && pttMode,
                        onHold = actions::setPtt,
                    )
                    Spacer(Modifier.width(8.dp))
                    GatewaySideButton(
                        if (state.outputMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                        "Audio", Modifier.weight(1f),
                    ) { actions.toggleSound() }
                }
                GatewayStatusBar(connected, state.pttHeld, pttMode, Modifier.fillMaxWidth().height(38.dp))
            }
        }
    }
}

@Composable
private fun GatewayQuickButton(icon: ImageVector, title: String, alert: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier.size(52.dp)
            .shadow(5.dp, RoundedCornerShape(13.dp), ambientColor = ElectricBlue.copy(alpha = 0.33f))
            .clip(RoundedCornerShape(13.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF153650), Color(0xFF061323))))
            .border(1.dp, if (alert) AmberGlow else ElectricBlue, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = title, tint = if (alert) AmberGlow else LightText, modifier = Modifier.size(25.dp))
    }
}

@Composable
private fun GatewayTab(title: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier.fillMaxSize().padding(2.dp).clip(shape)
            .background(
                if (active) Brush.verticalGradient(listOf(Color(0xFF9F3D00), Color(0xFF251205)))
                else Brush.verticalGradient(listOf(Color(0xFF102C44), Color(0xFF061426))),
            )
            .border(if (active) 2.dp else 0.5.dp, if (active) AmberGlow else ElectricBlue.copy(alpha = 0.55f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(title, color = LightText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GatewaySideButton(icon: ImageVector, title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(17.dp)
    Column(
        modifier.height(77.dp).clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF123C60), Color(0xFF071323))))
            .border(1.5.dp, ElectricBlue, shape).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = LightText, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(3.dp))
        Text(title, color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun GatewayPttButton(size: androidx.compose.ui.unit.Dp, held: Boolean, enabled: Boolean, onHold: (Boolean) -> Unit) {
    val ring = if (held) AmberGlow else ElectricBlue
    val label = if (held) "ZENDEN" else if (enabled) "PTT — ingedrukt houden" else "PTT — kies PTT in instellingen"
    val gesture = if (enabled) Modifier.holdToTalk(held, label, onHold = onHold) else Modifier
    Column(
        Modifier.size(size)
            .shadow(16.dp, CircleShape, ambientColor = ring.copy(alpha = 0.7f), spotColor = ring.copy(alpha = 0.7f))
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    if (held) listOf(Color(0xFFFFAA3A), Color(0xFFB62B00), Color(0xFF260700))
                    else listOf(Color(0xFF007DCE), Color(0xFF002F83), Color(0xFF010A23)),
                ),
            )
            .border(5.dp, ring, CircleShape)
            .then(gesture),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.27f))
        Text("PTT", color = Color.White, fontWeight = FontWeight.Black, fontSize = (size.value * 0.25f).sp)
        if (!enabled) Text("INSTELLEN", color = Color(0xFFFFD08A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GatewayStatusBar(connected: Boolean, held: Boolean, pttMode: Boolean, modifier: Modifier = Modifier) {
    val fmt = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
    var clock by remember { mutableStateOf(LocalTime.now().format(fmt)) }
    LaunchedEffect(Unit) {
        while (true) {
            clock = LocalTime.now().format(fmt)
            delay(1000)
        }
    }
    Row(
        modifier.clip(RoundedCornerShape(12.dp))
            .background(DarkBlue)
            .border(1.dp, ElectricBlue.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            when {
                held -> "● ZENDEN"
                connected && pttMode -> "● Verbonden · PTT"
                connected -> "● Verbonden · VOX"
                else -> "○ Verbinden…"
            },
            color = if (connected) Color(0xFF35FF72) else AmberGlow,
            fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
        )
        Text(clock, color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
