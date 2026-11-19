package com.gsvegetable.gsclip

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import io.github.neoruaa.glasmio.core.GlasmioTheme
import io.github.neoruaa.glasmio.glass.GlasmioBackdropHost
import org.json.JSONArray
import org.json.JSONObject

private data class Note(val id: Long, val title: String, val body: String, val color: Color)

private val notePalette = listOf(
    Color(0xFFE8E0D3), Color(0xFFDCE7E2), Color(0xFFE5DFF0), Color(0xFFF0DFD8), Color(0xFFE6E8D8)
)

private val starterNotes = listOf(
    Note(1, "灵感收集", "把突然想到的点子先放在这里。\n不需要整理，先记录。", Color(0xFFE8E0D3)),
    Note(2, "周末计划", "去逛一家没去过的书店\n整理桌面和照片", Color(0xFFDCE7E2)),
    Note(3, "关于一个新项目", "让笔记像便签一样自由地散落在画布上。", Color(0xFFE5DFF0)),
)

private fun loadNotes(context: Context): List<Note> {
    val raw = context.getSharedPreferences("gsclip_local", Context.MODE_PRIVATE).getString("notes", null)
        ?: return starterNotes
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(Note(item.getLong("id"), item.optString("title"), item.optString("body"), Color(item.optInt("color", 0xFFE8E0D3.toInt()))))
            }
        }
    }.getOrElse { starterNotes }
}

private fun encodeNotes(notes: List<Note>): String = JSONArray().apply {
    notes.forEach { note ->
        put(JSONObject().apply {
            put("id", note.id)
            put("title", note.title)
            put("body", note.body)
            put("color", note.color.toArgb())
        })
    }
}.toString()

private fun loadPositions(context: Context): Map<Long, Offset> {
    val raw = context.getSharedPreferences("gsclip_local", Context.MODE_PRIVATE).getString("positions", null) ?: return emptyMap()
    return runCatching {
        val array = JSONArray(raw)
        buildMap {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                put(item.getLong("id"), Offset(item.getDouble("x").toFloat(), item.getDouble("y").toFloat()))
            }
        }
    }.getOrDefault(emptyMap())
}

private fun encodePositions(positions: Map<Long, Offset>): String = JSONArray().apply {
    positions.forEach { (id, point) ->
        put(JSONObject().apply { put("id", id); put("x", point.x); put("y", point.y) })
    }
}.toString()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GsclipApp() }
    }
}

@Composable
private fun GsclipApp() {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("gsclip_local", Context.MODE_PRIVATE) }
    val notes = remember(context) { mutableStateListOf<Note>().apply { addAll(loadNotes(context)) } }
    val positions = remember(context) { mutableStateMapOf<Long, Offset>().apply { putAll(loadPositions(context)) } }
    var canvasMode by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Note?>(null) }
    var search by remember { mutableStateOf("") }
    var draftTitle by remember { mutableStateOf("") }
    var draftBody by remember { mutableStateOf("") }
    var lastDeleted by remember { mutableStateOf<Note?>(null) }
    var lastDeletedPosition by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(notes.toList()) {
        prefs.edit().putString("notes", encodeNotes(notes)).apply()
    }
    LaunchedEffect(positions.toMap()) {
        prefs.edit().putString("positions", encodePositions(positions)).apply()
    }

    GlasmioTheme {
        GlasmioBackdropHost(
            modifier = Modifier.fillMaxSize(),
            sampledContent = { Box(Modifier.fillMaxSize().background(Color(0xFFF4F3F0))) },
            overlayContent = {
                if (editing != null) {
                    EditorScreen(
                        title = draftTitle, body = draftBody,
                        onTitleChange = { draftTitle = it },
                        onBodyChange = { draftBody = it },
                        canDelete = editing?.id != -1L,
                        onCancel = { editing = null },
                        onClose = {
                            val old = editing
                            if (old != null && old.id != -1L) {
                                val index = notes.indexOfFirst { it.id == old.id }
                                if (index >= 0) notes[index] = old.copy(title = draftTitle, body = draftBody)
                            } else if (draftTitle.isNotBlank() || draftBody.isNotBlank()) {
                                val created = Note(System.currentTimeMillis(), draftTitle.ifBlank { "未命名笔记" }, draftBody, notePalette[notes.size % notePalette.size])
                                notes.add(0, created)
                                positions.remove(created.id)
                            }
                            editing = null
                        },
                        onDelete = {
                            editing?.let { note ->
                                if (note.id != -1L) {
                                    lastDeleted = notes.find { it.id == note.id }
                                    lastDeletedPosition = positions.remove(note.id)
                                    notes.removeAll { it.id == note.id }
                                }
                            }
                            editing = null
                        }
                    )
                } else {
                    HomeScreen(
                        notes = notes.filter { search.isBlank() || it.title.contains(search, true) || it.body.contains(search, true) },
                        canvasMode = canvasMode, search = search,
                        positions = positions,
                        deletedNote = lastDeleted,
                        onUndoDelete = {
                            lastDeleted?.let { deleted ->
                                notes.add(0, deleted)
                                lastDeletedPosition?.let { positions[deleted.id] = it }
                            }
                            lastDeleted = null
                            lastDeletedPosition = null
                        },
                        onSearchChange = { search = it }, onModeChange = { canvasMode = it },
                        onCreate = { draftTitle = ""; draftBody = ""; editing = Note(-1, "", "", Color(0xFFE8E0D3)) },
                        onOpen = { note -> editing = note; draftTitle = note.title; draftBody = note.body }
                    )
                }
            }
        )
    }
}

@Composable
private fun HomeScreen(
    notes: List<Note>, canvasMode: Boolean, search: String,
    positions: MutableMap<Long, Offset>,
    deletedNote: Note?,
    onUndoDelete: () -> Unit,
    onSearchChange: (String) -> Unit, onModeChange: (Boolean) -> Unit,
    onCreate: () -> Unit, onOpen: (Note) -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate, containerColor = Color(0xFF242424), contentColor = Color.White) {
                Icon(Icons.Rounded.Add, contentDescription = "新建笔记")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("GSCLIP", style = MaterialTheme.typography.labelMedium, color = Color(0xFF898782), fontWeight = FontWeight.SemiBold)
                    Text("随手记", style = MaterialTheme.typography.headlineLarge, color = Color(0xFF22211F), fontWeight = FontWeight.Bold)
                }
                Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = .68f)) {
                    Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { onModeChange(false) }) { Icon(Icons.Rounded.ViewAgenda, null, tint = if (!canvasMode) Color.Black else Color.Gray) }
                        TextButton(onClick = { onModeChange(true) }) { Icon(Icons.Rounded.GridView, null, tint = if (canvasMode) Color.Black else Color.Gray) }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = search, onValueChange = onSearchChange, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索笔记") }, leadingIcon = { Icon(Icons.Rounded.Search, null) },
                singleLine = true, shape = RoundedCornerShape(18.dp)
            )
            if (deletedNote != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("已删除「${deletedNote.title.ifBlank { "未命名笔记" }}」", color = Color(0xFF77736D), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onUndoDelete) { Text("撤销") }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (canvasMode) "灵感画布" else "最近记录", style = MaterialTheme.typography.titleMedium, color = Color(0xFF33312E), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("${notes.size} 条", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
            Spacer(Modifier.height(12.dp))
            AnimatedContent(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                targetState = canvasMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "view-mode"
            ) { canvas ->
                if (notes.isEmpty()) {
                    EmptyState(isSearching = search.isNotBlank(), onCreate = onCreate)
                } else if (canvas) {
                    CanvasBoard(notes = notes, positions = positions, onOpen = onOpen)
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(notes, key = { it.id }) { note -> NoteCard(note, false, onClick = { onOpen(note) }) }
                    }
                }
            }
        }
    }
}


@Composable
private fun EmptyState(isSearching: Boolean, onCreate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 72.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (isSearching) "没有找到这条笔记" else "这里还空着",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF393733),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isSearching) "试试其他关键词" else "把第一个想法放进来",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF898782)
        )
        if (!isSearching) {
            Spacer(Modifier.height(14.dp))
            TextButton(onClick = onCreate) { Text("新建一条笔记") }
        }
    }
}

@Composable
private fun NoteCard(note: Note, canvas: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(if (canvas) 26.dp else 22.dp),
        color = if (canvas) note.color.copy(alpha = .92f) else Color.White.copy(alpha = .72f),
        tonalElevation = 0.dp, shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(note.title.ifBlank { "未命名笔记" }, style = MaterialTheme.typography.titleMedium, color = Color(0xFF262522), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(note.body, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF77736D), maxLines = 4)
        }
    }
}

@Composable
private fun CanvasBoard(notes: List<Note>, positions: MutableMap<Long, Offset>, onOpen: (Note) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        val density = LocalDensity.current.density
        val rightX = (maxWidth.value - 250f).coerceAtLeast(0f)
        val boardHeight = maxOf(560.dp, (notes.size * 190 + 160).dp)
        Box(Modifier.fillMaxWidth().height(boardHeight)) {
            notes.forEachIndexed { index, note ->
                val initial = Offset(if (index % 2 == 0) 0f else rightX, (index / 2 * 190).toFloat())
                val position = positions[note.id] ?: initial
                NoteCard(
                    note = note, canvas = true, onClick = { onOpen(note) },
                    modifier = Modifier.offset(x = position.x.dp, y = position.y.dp)
                        .width(230.dp)
                        .pointerInput(note.id) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val current = positions[note.id] ?: initial
                                positions[note.id] = current + Offset(dragAmount.x / density, dragAmount.y / density)
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun EditorScreen(
    title: String, body: String, onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit, canDelete: Boolean,
    onCancel: () -> Unit, onClose: () -> Unit, onDelete: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F3F0)).imePadding().padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(26.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (canDelete) {
                TextButton(onClick = onDelete) { Text("删除笔记", color = Color(0xFFB34A43)) }
            } else {
                TextButton(onClick = onCancel) { Text("取消") }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("正在编辑", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                TextButton(onClick = onClose) { Icon(Icons.Rounded.Close, null); Text("完成") }
            }
        }
        Spacer(Modifier.height(22.dp))
        OutlinedTextField(value = title, onValueChange = onTitleChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("标题") }, textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), singleLine = true)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(value = body, onValueChange = onBodyChange, modifier = Modifier.weight(1f).fillMaxWidth(), placeholder = { Text("写点什么……") }, textStyle = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(20.dp))
    }
}
