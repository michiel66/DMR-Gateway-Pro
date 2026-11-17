#!/usr/bin/env python3
"""Apply DG Gateway Pro branding to a CLEAN PocketTS v1.7.0 source tree.

The upstream Kotlin audio engine and TeamSpeak protocol source are NOT patched.
Changes include a fixed Heerlen connection screen, region labels, branding and the default PTT preference.
Based on PocketTS (KorppuJauho), licensed under GPL-3.0; 2026-11-16.
"""
from pathlib import Path
import argparse
import shutil
import re

parser = argparse.ArgumentParser()
parser.add_argument("upstream", type=Path, help="Clean checkout of PocketTS v1.7.0")
args = parser.parse_args()
root = args.upstream.resolve()
overlay = Path(__file__).resolve().parent.parent


def replace_once(path: str, old: str, new: str) -> None:
    p = root / path
    data = p.read_text(encoding="utf-8")
    count = data.count(old)
    if count != 1:
        raise RuntimeError(f"Expected one occurrence of marker in {path}, found {count}: {old[:75]!r}")
    p.write_text(data.replace(old, new), encoding="utf-8")


gradle = "app/build.gradle.kts"
replace_once(gradle, 'applicationId = "io.github.korppujauho.pocketts"', 'applicationId = "nl.dg.gateway.pro"')
replace_once(gradle, 'versionCode = 8', 'versionCode = 4')
replace_once(gradle, 'versionName = "1.7.0"', 'versionName = "2.0.1"')
replace_once(gradle, 'manifestPlaceholders["appLabel"] = "PocketTS"', 'manifestPlaceholders["appLabel"] = "DG Gateway Pro"')
replace_once(gradle, 'manifestPlaceholders["appLabel"] = "PocketTStest"', 'manifestPlaceholders["appLabel"] = "DG Gateway Pro"')
replace_once(gradle, 'manifestPlaceholders["appLabel"] = "PocketTSshrunk"', 'manifestPlaceholders["appLabel"] = "DG Gateway Pro Shrunk"')
replace_once(gradle, 'buildConfigField("boolean", "UPDATE_CHECK", "true")', 'buildConfigField("boolean", "UPDATE_CHECK", "false")')

replace_once("app/src/main/res/values/strings.xml",
             '<string name="app_name" translatable="false">PocketTS</string>',
             '<string name="app_name" translatable="false">DG Gateway Pro</string>')

# PTT is the default only for this new package; any user-selected voice activation is still respected.
replace_once("app/src/main/kotlin/io/github/korppujauho/pocketts/VoiceSettings.kt",
             'val activation: Activation = Activation.VAD,', 'val activation: Activation = Activation.PTT,')
# Reliable Android receive audio by default: media playback to the built-in loudspeaker,
# not USAGE_VOICE_COMMUNICATION, which is sometimes routed to the quiet earpiece on Samsung.
# The outgoing MicSender/TeamSpeak voice transport are unchanged; the user's route
# preferences still work and the dashboard offers an explicit "Audio herstellen" fallback.
replace_once("app/src/main/kotlin/io/github/korppujauho/pocketts/VoiceSettings.kt",
             'val echoCancellation: Boolean = true,', 'val echoCancellation: Boolean = false,')
replace_once("app/src/main/kotlin/io/github/korppujauho/pocketts/VoiceSettings.kt",
             'val useHeadset: Boolean = true,', 'val useHeadset: Boolean = false,')

# The built-in two-tone test must follow the same device route as the actual receive player.
replace_once(
    "app/src/main/kotlin/io/github/korppujauho/pocketts/TestSound.kt",
    """            try {
                track.write(pcm, 0, pcm.size)""",
    """            if (!v.callAudio && !v.useHeadset) {
                val audio = MainActivity.current?.getSystemService(android.media.AudioManager::class.java)
                val speaker = audio?.getDevices(android.media.AudioManager.GET_DEVICES_OUTPUTS)
                    ?.firstOrNull { it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                if (speaker != null) track.setPreferredDevice(speaker)
            }
            try {
                track.write(pcm, 0, pcm.size)""",
)

# The large dashboard PTT replaces the original small floating overlay in this branding.
replace_once("app/src/main/kotlin/io/github/korppujauho/pocketts/VoiceSettings.kt",
             'val floatingPtt: Boolean = true,', 'val floatingPtt: Boolean = false,')

theme = "app/src/main/kotlin/io/github/korppujauho/pocketts/ui/Theme.kt"
palette = {
    "page": "0xFF020711",
    "header": "0xFF051322",
    "panel": "0xFF07192C",
    "card": "0xFF0A2036",
    "button": "0xFF103450",
    "input": "0xFF13283A",
    "text": "0xFFE3F3FF",
    "title": "0xFFF4FBFF",
    "bright": "0xFFFFFFFF",
    "username": "0xFFE0F2FF",
    "muted": "0xFFA2BDD2",
    "brand": "0xFF03BFFF",
    "accent": "0xFF008EFF",
    "accentFill": "0xFF0063B8",
    "talking": "0xFF17C6FF",
    "sender": "0xFF75D8FF",
    "red": "0xFFFF4149",
    "redButton": "0xFFAD201C",
    "green": "0xFF35FF72",
    "amber": "0xFFFF9100",
    "you": "0xFFFFB129",
    "youBorder": "0xFFFF9100",
}
data = (root / theme).read_text(encoding="utf-8")
for key, color in palette.items():
    pattern = r"(\bval " + re.escape(key) + r" = )Color\(0x[0-9A-Fa-f]+\)"
    data, n = re.subn(pattern, lambda m: m.group(1) + "Color(" + color + ")", data)
    if n != 1:
        raise RuntimeError(f"Expected one theme color: {key}; found {n}")
(root / theme).write_text(data, encoding="utf-8")

# A more prominent title/logo, while retaining the original disconnect, favourite and settings controls.
screens = "app/src/main/kotlin/io/github/korppujauho/pocketts/ui/Screens.kt"
replace_once(
    screens,
    'Modifier.fillMaxWidth().height(44.dp).background(Ts.header).padding(start = 16.dp, end = 4.dp)',
    'Modifier.fillMaxWidth().height(60.dp).background(Ts.header).padding(start = 13.dp, end = 4.dp)'
)
replace_once(
    screens,
    'Text(stringResource(R.string.app_name), color = Ts.bright, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))',
    'Text(stringResource(R.string.app_name), color = Ts.bright, fontWeight = FontWeight.Bold, fontSize = 19.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))'
)

server = "app/src/main/kotlin/io/github/korppujauho/pocketts/ui/ServerScreen.kt"
p = root / server
data = p.read_text(encoding="utf-8")
start_marker = "            Column(Modifier.fillMaxSize().padding(15.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {"
end_marker = "\n        PokeCards("
start = data.find(start_marker)
end = data.find(end_marker, start)
if start < 0 or end < 0 or data[start:end].count("ChannelTree(") != 1:
    raise RuntimeError("PocketTS 1.7.0 screen anchor changed. Refusing an unsafe patch.")
ending = "            }\n        }"
if not data[start:end].rstrip().endswith(ending):
    raise RuntimeError("Unexpected ServerScreen scope ending. Refusing patch.")

replacement = """            GatewayDashboard(
                state = state,
                actions = actions,
                view = view,
                onView = { view = it },
                onShare = { if (state.stream.sharing != null) actions.stopShare() else shareDialog = true },
                channelList = {
                    ChannelTree(
                        items,
                        TreeContext(state.ownId, state.ownChannel, voices, state.serverKey, state.badges, actions::watch) { it.id !in state.cannotTalk },
                        onChannel = { ch ->
                            if (ch.id != state.ownChannel && (ch.spacer == null || state.voice.joinSpacers)) {
                                if (!ch.hasPassword) actions.join(ch)
                                else {
                                    val password = actions.savedPassword(ch)
                                    if (password != null) actions.join(ch, password, remember = true)
                                    else actions.join(ch, quiet = true)
                                }
                            }
                        },
                        onPerson = { person = it },
                        modifier = Modifier.fillMaxSize(),
                        onLongChannel = if (state.phase == Session.Phase.CONNECTED)
                            ({ ch -> actions.channelDescription(ch); infoChannel = ch.id }) else null,
                    )
                },
                chatPanel = {
                    ChatPanel(
                        state, chatKey,
                        onView = { chatKey = it },
                        onCloseTab = actions::closePrivate,
                        onSend = actions::send,
                        onPicture = actions::sendPictures,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
            )
        }"""
data = data[:start] + replacement + data[end:]
p.write_text(data, encoding="utf-8")

destination = root / "app/src/main/kotlin/io/github/korppujauho/pocketts/ui/GatewayDashboard.kt"
shutil.copyfile(overlay / "patches/GatewayDashboard.kt", destination)

# Own icon instead of PocketTS branding. 108-dp vector renders both on launcher and AppHeader.
icon = r'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#020A1A" android:pathData="M0,0 L108,0 L108,108 L0,108 Z"/>
    <path android:fillColor="#15C8FF"
        android:pathData="M54,23 L49,37 L54,37 L59,37 Z M50,42 L43,81 L49,81 L54,59 L59,81 L65,81 L58,42 Z"/>
    <path android:strokeColor="#17C6FF" android:strokeWidth="4"
        android:strokeLineCap="round" android:fillColor="#00000000"
        android:pathData="M41,42 C32,51 32,64 41,72 M67,42 C76,51 76,64 67,72"/>
    <path android:strokeColor="#008EFF" android:strokeWidth="4"
        android:strokeLineCap="round" android:fillColor="#00000000"
        android:pathData="M32,34 C17,49 17,70 32,83 M76,34 C91,49 91,70 76,83"/>
    <path android:fillColor="#FF9100" android:pathData="M49,52 A5,5 0,1 1,59 52 A5,5 0,1 1,49 52 Z"/>
    <path android:strokeColor="#17C6FF" android:strokeWidth="3"
        android:strokeLineCap="round" android:fillColor="#00000000"
        android:pathData="M39,84 L69,84 M43,77 L65,77"/>
</vector>
'''
icon_path = root / "app/src/main/res/drawable/ic_launcher_foreground.xml"
icon_path.write_text(icon, encoding="utf-8")

# DG Gateway Pro: fixed-server connection screen and Dutch province-to-DG labels.
# The only selectable server address is built into the application and not shown in the UI.
main = "app/src/main/kotlin/io/github/korppujauho/pocketts/MainActivity.kt"
replace_once(
    main,
    "        Connect(state, favs)",
    """        io.github.korppujauho.pocketts.ui.DgConnectScreen(
            error = state.error,
            onConnect = { dgNickname ->
                connect(ctx, io.github.korppujauho.pocketts.ui.DgRegions.SERVER, dgNickname, "")
            },
            onDismissError = Session::clearError,
        )""",
)
replace_once(
    main,
    "favorites.value.firstOrNull { it.auto }?.let { connectFavorite(applicationContext, it) }",
    "// The fixed DG network must never auto-connect to unrelated servers.",
)
# Reject external teamspeak:// invitations. Other upstream link handlers remain unreachable.
p_main = root / main
activity = p_main.read_text(encoding="utf-8")
link_start = activity.find("    private fun openLink(intent: Intent?): Boolean {")
link_end = activity.find("\n    override fun onStart() {", link_start)
if link_start < 0 or link_end < 0:
    raise RuntimeError("PocketTS external-link anchors changed; refusing insecure patch.")
activity = activity[:link_start] + "    private fun openLink(intent: Intent?): Boolean = false // Fixed-server edition\n" + activity[link_end:]
p_main.write_text(activity, encoding="utf-8")

# Keep DG prefix even if a user edits their nickname via the original audio/settings panel.
replace_once(
    main,
    """        if (nickname.isNotEmpty() && nickname != Settings(context).nickname) {
            Settings(context).nickname = nickname
            Session.setNickname(nickname)
        }""",
    """        if (nickname.isNotEmpty() && nickname != Settings(context).nickname) {
            val prefs = context.getSharedPreferences("dg_profile", Context.MODE_PRIVATE)
            val region = io.github.korppujauho.pocketts.ui.DgRegions.byCode(prefs.getInt("region_code", 0))
            val cleanName = if (nickname.startsWith("DG-", ignoreCase = true))
                nickname.substringAfter(' ', "").trim().take(22) else nickname.trim().take(22)
            if (region != null && cleanName.isNotEmpty()) {
                val newNickname = io.github.korppujauho.pocketts.ui.DgRegions.nickname(region, cleanName)
                prefs.edit().putString("display_name", cleanName).apply()
                Settings(context).nickname = newNickname
                Session.setNickname(newNickname)
            }
        }""",
)

# Remove the old favourite server switcher: it would permit connecting elsewhere.
p_server = root / server
server_text = p_server.read_text(encoding="utf-8")
header_start = server_text.find("            AppHeader {\n")
header_end_tag = "                SettingsGear { settings = 0 }\n            }"
header_end = server_text.find(header_end_tag, header_start)
if header_start < 0 or header_end < 0:
    raise RuntimeError("PocketTS header anchors changed; cannot remove server switcher.")
server_text = server_text[:header_start] + """            AppHeader {
                HeaderButton(androidx.compose.material.icons.Icons.AutoMirrored.Filled.Logout,
                    stringResource(R.string.disconnect), tint = Ts.red) { actions.disconnect() }
                SettingsGear { settings = 0 }
            }""" + server_text[header_end + len(header_end_tag):]
p_server.write_text(server_text, encoding="utf-8")

# Complete DG region mapping and saved-profile login are native Compose UI.
for dg_file in ("DgRegions.kt", "DgConnectScreen.kt"):
    shutil.copyfile(
        overlay / "patches" / dg_file,
        root / "app/src/main/kotlin/io/github/korppujauho/pocketts/ui" / dg_file,
    )

# Leave the manifest, audio service, protocol and codec implementations unchanged.
print("Applied DG Gateway Pro branding and neon dashboard to PocketTS v1.7.0.")
print("Original TeamSpeak protocol and microphone/audio engine are unchanged.")
