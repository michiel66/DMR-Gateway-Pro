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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import io.github.korppujauho.pocketts.TestSound
import io.github.korppujauho.pocketts.VoiceSettings
import io.github.korppujauho.pocketts.proto.ServerModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

/*
 * DG Gateway Pro - independently branded Android presentation.
 * 2026-11-16. PocketTS audio, protocol, PTT and session handling remain intact.
 * GPL-3.0-or-later; original PocketTS copyright notices remain in the source.
 */
private val ElectricBlue = Color(0xFF008EFF)
private val CyanGlow = Color(0xFF17C6FF)
private val AmberGlow = Color(0xFFFF9100)
private val DarkBlue = Color(0xFF05101F)
private val DeepPanel = Color(0xFF07182C)
private val LightText = Color(0xFFE4F4FF)
private val StandbyGreen = Color(0xFF26F16A)
private val TransmitRed = Color(0xFFFF3542)
// Metallic frame in the reference: a cool bright edge with darker brushed-steel reflections.
private val SteelEdge = Brush.linearGradient(
    listOf(
        Color(0xFF66788B), Color(0xFFF8FBFF), Color(0xFF7C93A7),
        Color(0xFF344557), Color(0xFFE2EDF5), Color(0xFF62788C),
    ),
)
private val SteelRing = Brush.sweepGradient(
    listOf(
        Color(0xFF344658), Color(0xFFF6FAFF), Color(0xFF7B8B9C),
        Color(0xFF27384B), Color(0xFFE8F0F7), Color(0xFF47596D),
        Color(0xFF344658),
    ),
)

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
    val ownNickname = state.client(state.ownId)?.nickname.orEmpty()
    val ownRegion = DgRegions.regionOf(ownNickname)
    val pttMode = state.voice.activation == VoiceSettings.Activation.PTT
    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    var showUsers by remember { mutableStateOf(false) }
    var showAudio by remember { mutableStateOf(false) }
    // Actual PocketTS receive decoder's talking state; not a visual simulation.
    val packetVoice by Session.voices.collectAsState()
    val incomingVoiceActive = packetVoice.talking.any { it != state.ownId }
    // Every visible connected client across the server's entire channel tree.
    // Exclude server-query identities just like PocketTS's channel listing does.
    val onlineUsers = remember(state.tree) {
        flattenTree(state.tree)
            .flatMap { it.node.clients }
            .filter { it.type != 1 && it.nickname.isNotBlank() }
            .distinctBy { it.id }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.nickname })
    }

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
                    "Geluid", alert = state.outputMuted, onClick = { showAudio = true },
                )
                GatewayQuickButton(Icons.Filled.ScreenShare, "Scherm delen", onClick = onShare)
                GatewayQuickButton(Icons.Filled.People, "Gebruikers", onClick = { showUsers = true })
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
                    .shadow(13.dp, RoundedCornerShape(23.dp), ambientColor = Color(0xFF94AABD).copy(alpha = 0.30f))
                    .clip(RoundedCornerShape(23.dp))
                    .background(SteelEdge)
                    .border(1.dp, Color(0xFFD8E6EF), RoundedCornerShape(23.dp))
                    .padding(5.dp) // brushed steel bezel wraps the complete screen
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF071C32), Color(0xFF03101F))))
                    .border(1.5.dp, ElectricBlue, RoundedCornerShape(18.dp)),
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
                        "DG REGIO NETWERK", color = LightText, fontWeight = FontWeight.Bold,
                        fontSize = if (narrowScreen) 13.sp else 15.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(if (connected) "●" else "○", color = if (connected) Color(0xFF28FF63) else AmberGlow, fontSize = 17.sp)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(CyanGlow.copy(alpha = 0.75f)))
                if (ownNickname.isNotBlank()) {
                    Text(
                        "${ownRegion?.fullLabel ?: "DG-999 · Overige"}  ·  $ownNickname",
                        color = CyanGlow.copy(alpha = 0.9f), fontSize = 11.sp,
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
                    GatewaySideButton(Icons.Filled.People, "Gebruikers", Modifier.weight(1f)) { showUsers = true }
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
                    ) { showAudio = true }
                }
                GatewayStatusBar(connected, state.pttHeld, pttMode, Modifier.fillMaxWidth().height(38.dp))
            }
        }
    }
    if (showUsers) {
        OnlineUsersPopup(onlineUsers, onClose = { showUsers = false })
    }
    if (showAudio) {
        AudioRepairPopup(
            muted = state.outputMuted,
            incomingVoiceActive = incomingVoiceActive,
            currentChannel = state.ownChannelName,
            mediaSpeaker = !state.voice.callAudio && !state.voice.useHeadset,
            pocketTsMode = state.voice.echoCancellation && state.voice.useHeadset && !state.voice.earpiece,
            onToggleMute = actions::toggleSound,
            onTestSound = { TestSound.play(state.voice) },
            onPocketTs = {
                // PocketTS v1.7.0 defaults; recover persisted audio settings from 2.0.1
                // without changing activation, mic level, noise suppression or PTT.
                Session.updateVoice(state.voice.copy(
                    echoCancellation = true,
                    earpiece = false,
                    useHeadset = true,
                    speakerRefusedAt = 0,
                    voiceVolumeDb = 0,
                ))
                if (state.outputMuted) actions.toggleSound()
            },
            onSpeaker = {
                // Only a manual alternative if the exact PocketTS route fails on this phone.
                Session.updateVoice(state.voice.copy(
                    earpiece = false,
                    echoCancellation = false,
                    useHeadset = false,
                    voiceVolumeDb = 0,
                ))
                if (state.outputMuted) actions.toggleSound()
            },
            onClose = { showAudio = false },
        )
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
    val signal = if (held) TransmitRed else StandbyGreen
    val label = if (held) "ZENDEN" else if (enabled) "PTT — ingedrukt houden" else "PTT — kies PTT in instellingen"
    val gesture = if (enabled) Modifier.holdToTalk(held, label, onHold = onHold) else Modifier
    Box(
        Modifier.size(size)
            .shadow(18.dp, CircleShape, ambientColor = signal.copy(alpha = 0.60f), spotColor = signal.copy(alpha = 0.55f))
            .clip(CircleShape)
            .background(SteelRing)
            .border(1.5.dp, Color(0xFFF2F7FB), CircleShape)
            .then(gesture)  // the entire steel-framed circle is press-to-talk
            .padding(9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        if (held)
                            listOf(Color(0xFFFF636A), Color(0xFFDA172A), Color(0xFF530614))
                        else
                            listOf(Color(0xFF31F77D), Color(0xFF008D38), Color(0xFF003417)),
                    ),
                )
                .border(3.dp, signal, CircleShape),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.27f))
            Text("PTT", color = Color.White, fontWeight = FontWeight.Black, fontSize = (size.value * 0.25f).sp)
            if (!enabled) Text("INSTELLEN", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** Names only, for ALL channels. The list refreshes as PocketTS receives server updates. */
@Composable
private fun OnlineUsersPopup(users: List<ServerModel.Client>, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text("Online gebruikers (${users.size})", color = LightText, fontWeight = FontWeight.Bold)
        },
        text = {
            if (users.isEmpty()) {
                Text("Geen gebruikers online", color = LightText)
            } else {
                LazyColumn(
                    Modifier.fillMaxWidth().heightIn(max = 395.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(users, key = { it.id }) { person ->
                        Text(
                            person.nickname,
                            color = LightText,
                            fontSize = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("Sluiten", color = Color(0xFFBDD4E5), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF07182C),
        shape = RoundedCornerShape(19.dp),
    )
}

/** Audio output diagnostics: never silently toggle the speaker when opening this popup. */
@Composable
private fun AudioRepairPopup(
    muted: Boolean,
    incomingVoiceActive: Boolean,
    currentChannel: String,
    mediaSpeaker: Boolean,
    pocketTsMode: Boolean,
    onToggleMute: () -> Unit,
    onTestSound: () -> Unit,
    onPocketTs: () -> Unit,
    onSpeaker: () -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = CyanGlow)
                Spacer(Modifier.width(9.dp))
                Text("Ontvangstgeluid", color = LightText, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (incomingVoiceActive) "RX: TeamSpeak-spraak wordt ontvangen"
                    else "RX: momenteel geen inkomende spraak gedetecteerd",
                    color = if (incomingVoiceActive) StandbyGreen else Color(0xFFB1C8D9),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
                Text(
                    "Huidig TeamSpeak-kanaal: ${currentChannel.ifBlank { "Onbekend" }}",
                    color = Color(0xFFB1C8D9),
                    fontSize = 12.sp,
                )
                Text(
                    if (muted) "Geluid staat UIT (gedempt)."
                    else "Geluid staat AAN.",
                    color = if (muted) AmberGlow else StandbyGreen,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    when {
                        pocketTsMode -> "Audio via de originele PocketTS-gespreksroute (luidspreker of headset)."
                        mediaSpeaker -> "Audio via mediageluid op de telefoonluidspreker."
                        else -> "Er is een aangepaste audio-uitgang actief."
                    },
                    color = LightText,
                    fontSize = 13.sp,
                )
                Button(
                    onClick = onToggleMute,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF154A73)),
                ) {
                    Text(if (muted) "Geluid aanzetten" else "Geluid dempen")
                }
                Button(
                    onClick = onTestSound,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF154A73)),
                ) {
                    Text("Testgeluid afspelen")
                }
                Button(
                    onClick = onPocketTs,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF08763C)),
                ) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("Origineel PocketTS geluid")
                }
                Button(
                    onClick = onSpeaker,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF154A73)),
                ) {
                    Text("Alternatief: telefoonluidspreker")
                }
                Text(
                    "PocketTS herstelt de oorspronkelijke audio-instellingen. Dit is de aanbevolen keuze als PocketTS op dit toestel goed werkte. Test daarna het geluid opnieuw.",
                    color = Color(0xFFB1C8D9), fontSize = 12.sp,
                )
                Text(
                    "Zorg dat beide gebruikers in hetzelfde TeamSpeak-kanaal zitten. Probeer bij PocketTS-geluid ook het gespreksvolume hoger te zetten; bij luidsprekerstand het mediavolume.",
                    color = Color(0xFFB1C8D9), fontSize = 12.sp,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text("Sluiten", color = LightText) }
        },
        containerColor = DeepPanel,
        shape = RoundedCornerShape(18.dp),
    )
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
