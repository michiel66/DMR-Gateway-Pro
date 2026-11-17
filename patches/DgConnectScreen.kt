package io.github.korppujauho.pocketts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * DG Gateway Pro - the only normal connection screen.
 * Never asks a user for a TeamSpeak host, server password or port.
 * The underlying PocketTS connect/audio engine remains unchanged.
 */
@Composable
fun DgConnectScreen(
    error: String?,
    onConnect: (String) -> Unit,
    onDismissError: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.applicationContext.getSharedPreferences("dg_profile", 0) }
    var name by rememberSaveable { mutableStateOf(prefs.getString("display_name", "").orEmpty()) }
    var chosenCode by rememberSaveable { mutableIntStateOf(prefs.getInt("region_code", 0)) }
    var expanded by remember { mutableStateOf(false) }
    val region = DgRegions.byCode(chosenCode)
    val normalized = name.trim().replace(Regex("\\s+"), " ")
    val nickname = region?.let { DgRegions.nickname(it, normalized) }
    val canConnect = normalized.isNotBlank() && region != null

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF010714), Color(0xFF09233C), Color(0xFF020711))))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 17.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                Icon(Icons.Filled.Radio, contentDescription = null, tint = Color(0xFF45C9FF), modifier = Modifier.size(43.dp))
                Column {
                    Text("DG GATEWAY PRO", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    Text("Digitale Gateway Regio Netwerk Nederland", color = Color(0xFF83C8F5), fontSize = 11.sp, maxLines = 1)
                }
            }
            Spacer(Modifier.height(8.dp))
            val steel = Brush.linearGradient(listOf(Color(0xFF7C91A5), Color(0xFFE8F2FF), Color(0xFF536779)))
            Column(
                Modifier.fillMaxWidth()
                    .border(3.dp, steel, RoundedCornerShape(20.dp))
                    .background(Color(0xFF071729), RoundedCornerShape(20.dp))
                    .padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Welkom bij DG Gateway Pro", color = Color(0xFFE9F6FF), fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Text("Vul je naam in en kies de provincie waar je woont. Je DG-regio wordt automatisch ingesteld.",
                    color = Color(0xFFBED5E6), fontSize = 13.sp)

                Text("Gebruikersnaam", color = Color(0xFFE8F3FC), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.replace("\n", " ").replace("\r", " ").take(22) },
                    placeholder = { Text("Bijvoorbeeld: Michiel") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Provincie", color = Color(0xFFE8F3FC), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Box {
                    Row(
                        Modifier.fillMaxWidth()
                            .border(1.dp, Color(0xFF3A9BCE), RoundedCornerShape(11.dp))
                            .background(Color(0xFF102A42), RoundedCornerShape(11.dp))
                            .clickable { expanded = true }
                            .padding(horizontal = 13.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(region?.province ?: "Kies jouw provincie", color = Color.White,
                            fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Provincies tonen", tint = Color(0xFF5DD2FF))
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.heightIn(max = 370.dp),
                    ) {
                        DgRegions.all.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.province) },
                                onClick = { chosenCode = option.code; expanded = false },
                            )
                        }
                    }
                }

                if (nickname != null && normalized.isNotBlank()) {
                    Column(
                        Modifier.fillMaxWidth()
                            .background(Color(0xFF102B35), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF2AB96B), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text("Je naam op TeamSpeak", color = Color(0xFFB9D4E7), fontSize = 12.sp)
                        Text(nickname, color = Color(0xFF41F08B), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text("${region?.province} · ${region?.label}", color = Color(0xFFBED5E6), fontSize = 12.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF38E17B), modifier = Modifier.size(20.dp))
                    Text("De TeamSpeak-server is automatisch ingesteld.", color = Color(0xFFC1DCEB), fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        val chosen = region ?: return@Button
                        val cleanName = normalized.take(22)
                        if (cleanName.isEmpty()) return@Button
                        prefs.edit().putString("display_name", cleanName).putInt("region_code", chosen.code).apply()
                        onConnect(DgRegions.nickname(chosen, cleanName))
                    },
                    enabled = canConnect,
                    modifier = Modifier.fillMaxWidth().height(53.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF057D42)),
                ) {
                    Icon(Icons.Filled.Radio, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("VERBINDEN", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
            if (!error.isNullOrBlank()) {
                Column(
                    Modifier.fillMaxWidth().background(Color(0xFF3B1724), RoundedCornerShape(13.dp)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text("Verbinden mislukt", color = Color(0xFFFFBFC4), fontWeight = FontWeight.Bold)
                    Text(error, color = Color.White, fontSize = 12.sp)
                    TextButton(onClick = onDismissError) { Text("Melding sluiten", color = Color.White) }
                }
            }
            Text("DG-groepen zijn eigen TeamSpeak-regiogroepen en geen officiële DMR-talkgroups.",
                color = Color(0xFF99AEC3), fontSize = 11.sp)
        }
    }
}
