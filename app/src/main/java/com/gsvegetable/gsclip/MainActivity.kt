package com.gsvegetable.gsclip

import android.content.Context
import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.neoruaa.glasmio.core.GlasmioTheme
import io.github.neoruaa.glasmio.glass.GlasmioBackdropHost

private val ink = Color(0xFF24231F)
private val muted = Color(0xFF89877F)
private val paper = Color(0xFFF3F2EE)
private val noteColors = listOf(Color(0xFFFFF0C9), Color(0xFFE3EEE7), Color(0xFFE9E4F5), Color(0xFFF4E3DF), Color(0xFFE3EBF5))
private data class Note(val id: Long, val title: String, val body: String, val color: Int = 0, val x: Float = 24f, val y: Float = 24f, val updated: Long = System.currentTimeMillis())

private object NoteStore {
    private const val PREFS = "gsclip_notes"
    private const val KEY = "items"
    fun load(context: Context): List<Note> = runCatching {
        val array = JSONArray(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]"))
        (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            Note(o.getLong("id"), o.optString("title"), o.optString("body"), o.optInt("color"), o.optDouble("x", 24.0).toFloat(), o.optDouble("y", 24.0).toFloat(), o.optLong("updated"))
        }
    }.getOrDefault(emptyList())
    fun save(context: Context, notes: List<Note>) {
        val array = JSONArray()
        notes.forEach { n -> array.put(JSONObject().put("id", n.id).put("title", n.title).put("body", n.body).put("color", n.color).put("x", n.x).put("y", n.y).put("updated", n.updated)) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, array.toString()).apply()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent { GsclipApp() }
    }
}

@Composable
private fun GsclipApp() {
    val context = LocalContext.current
    val notes = remember { mutableStateListOf<Note>().also { it.addAll(NoteStore.load(context)) } }
    var canvasMode by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var search by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selectedColor by remember { mutableIntStateOf(0) }
    fun persist() = NoteStore.save(context, notes)
    fun openEditor(note: Note?) {
        editingId = note?.id ?: -1L
        title = note?.title.orEmpty()
        body = note?.body.orEmpty()
        selectedColor = note?.color ?: 0
    }
    fun saveEditor() {
        val id = editingId
        if (title.isNotBlank() || body.isNotBlank()) {
            if (id == null || id == -1L) notes.add(0, Note(System.currentTimeMillis(), title.trim(), body, selectedColor, x = 24f + (notes.size % 4) * 22f, y = 28f + (notes.size % 5) * 24f))
            else {
                val index = notes.indexOfFirst { it.id == id }
                if (index >= 0) notes[index] = notes[index].copy(title = title.trim(), body = body, color = selectedColor, updated = System.currentTimeMillis())
            }
            persist()
        }
        editingId = null
    }
    GlasmioTheme {
        GlasmioBackdropHost(
            modifier = Modifier.fillMaxSize(),
            sampledContent = { Box(Modifier.fillMaxSize().background(paper)) },
            overlayContent = {
                if (editingId != null) NoteEditor(title, body, selectedColor, { title = it }, { body = it }, { selectedColor = it }, { saveEditor() })
                else HomeScreen(
                    notes = notes.filter { search.isBlank() || it.title.contains(search, true) || it.body.contains(search, true) },
                    canvasMode = canvasMode, search = search, onSearch = { search = it }, onMode = { canvasMode = it },
                    onCreate = { openEditor(null) }, onOpen = { openEditor(it) },
                    onMove = { id, x, y -> val i = notes.indexOfFirst { it.id == id }; if (i >= 0) { notes[i] = notes[i].copy(x = x.coerceAtLeast(0f), y = y.coerceAtLeast(0f)); persist() } },
                )
            }
        )
    }
}

@Composable
private fun HomeScreen(notes: List<Note>, canvasMode: Boolean, search: String, onSearch: (String) -> Unit, onMode: (Boolean) -> Unit, onCreate: () -> Unit, onOpen: (Note) -> Unit, onMove: (Long, Float, Float) -> Unit, onDelete: (Long) -> Unit) {
    Scaffold(containerColor = Color.Transparent, floatingActionButton = {
        FloatingActionButton(onClick = onCreate, shape = CircleShape, containerColor = ink, contentColor = Color.White, modifier = Modifier.shadow(12.dp, CircleShape)) {
            Icon(Icons.Rounded.Add, contentDescription = "新建笔记")
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("GSCLIP  /  YOUR THOUGHT SPACE", fontSize = 10.sp, color = muted, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(7.dp))
                    Text(if (canvasMode) "灵感画布" else "随手记", fontSize = 34.sp, color = ink, fontWeight = FontWeight.Bold, letterSpacing = (-1.2).sp)
                }
                GlassModeSwitch(canvasMode, onMode)
            }
            Spacer(Modifier.height(20.dp))
            GlassSearch(search, onSearch)
            Spacer(Modifier.height(19.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (canvasMode) "自由摆放" else "最近记录", fontSize = 15.sp, color = ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("${notes.size} 条笔记", fontSize = 12.sp, color = muted)
            }
            Spacer(Modifier.height(12.dp))
            AnimatedContent(canvasMode, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "workspace") { canvas ->
                if (canvas) CanvasBoard(notes, onOpen, onMove)
                else if (notes.isEmpty()) EmptyState(onCreate)
                else LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    items(notes, key = { it.id }) { note -> NoteRow(note, onClick = { onOpen(note) }) }
                }
            }
        }
    }
}

@Composable
private fun GlassModeSwitch(canvas: Boolean, onMode: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = .68f), modifier = Modifier.shadow(8.dp, RoundedCornerShape(18.dp))) {
        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            ModeButton(!canvas, { onMode(false) }) { Icon(Icons.Rounded.ViewAgenda, null, modifier = Modifier.size(19.dp)) }
            ModeButton(canvas, { onMode(true) }) { Icon(Icons.Rounded.GridView, null, modifier = Modifier.size(19.dp)) }
        }
    }
}
@Composable
private fun ModeButton(selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(Modifier.clip(RoundedCornerShape(14.dp)).background(if (selected) Color.White else Color.Transparent).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 9.dp), contentAlignment = Alignment.Center) {
        CompositionLocalProvider(LocalContentColor provides if (selected) ink else muted) { content() }
    }
}
@Composable
private fun GlassSearch(value: String, onValue: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = .72f)).padding(horizontal = 15.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Search, null, tint = muted, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text("搜索笔记与灵感", color = muted, fontSize = 14.sp)
            BasicTextField(value, onValue, singleLine = true, textStyle = TextStyle(color = ink, fontSize = 14.sp), modifier = Modifier.fillMaxWidth())
        }
    }
}
@Composable
private fun NoteRow(note: Note, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().shadow(5.dp, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = .78f)).clickable(onClick = onClick).padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(42.dp).clip(CircleShape).background(noteColors[note.color.coerceIn(noteColors.indices)]))
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(note.title.ifBlank { "无标题" }, color = ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Spacer(Modifier.height(5.dp))
            Text(note.body.ifBlank { "空白笔记" }, color = muted, fontSize = 13.sp, maxLines = 2, lineHeight = 19.sp)
        }
        Text("›", color = muted, fontSize = 24.sp)
    }
}
@Composable
private fun CanvasBoard(notes: List<Note>, onOpen: (Note) -> Unit, onMove: (Long, Float, Float) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)).background(Color(0xFFECEBE6))) {
        Box(Modifier.fillMaxSize()) {
            Text("拖动便签，自由整理你的想法", color = muted, fontSize = 11.sp, modifier = Modifier.align(Alignment.TopStart).padding(16.dp))
            notes.forEach { note ->
                CanvasNote(note, onOpen = { onOpen(note) }, onMove = { x, y -> onMove(note.id, x, y) })
            }
            if (notes.isEmpty()) Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("这里还很空", color = ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("点击右下角 + 添加第一张便签", color = muted, fontSize = 12.sp)
            }
        }
    }
}
@Composable
private fun CanvasNote(note: Note, onOpen: () -> Unit, onMove: (Float, Float) -> Unit) {
    var position by remember(note.id) { mutableStateOf(note.x to note.y) }
    LaunchedEffect(note.x, note.y) { position = note.x to note.y }
    val density = androidx.compose.ui.platform.LocalDensity.current
    Column(Modifier.offset(x = position.first.dp, y = position.second.dp).width(156.dp)
        .shadow(9.dp, RoundedCornerShape(5.dp)).clip(RoundedCornerShape(5.dp)).background(noteColors[note.color.coerceIn(noteColors.indices)])
        .pointerInput(note.id) {
            detectDragGestures(onDrag = { change, drag ->
                change.consume()
                val x = position.first + drag.x / density.density
                val y = position.second + drag.y / density.density
                position = x to y
                onMove(x, y)
            })
        }.clickable(onClick = onOpen).padding(13.dp)) {
        Text(note.title.ifBlank { "无标题" }, color = ink, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
        Spacer(Modifier.height(7.dp))
        Text(note.body.ifBlank { "…" }, color = Color(0xFF625E56), fontSize = 12.sp, lineHeight = 17.sp, maxLines = 7)
    }
}
@Composable
private fun EmptyState(onCreate: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 70.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("还没有笔记", color = ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(7.dp))
        Text("把脑海里的第一件事记下来。", color = muted, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Text("创建笔记  +", color = ink, modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = .8f)).clickable(onClick = onCreate).padding(horizontal = 18.dp, vertical = 11.dp), fontSize = 13.sp)
    }
}
@Composable
private fun NoteEditor(title: String, body: String, color: Int, onTitle: (String) -> Unit, onBody: (String) -> Unit, onColor: (Int) -> Unit, onClose: () -> Unit) {
    Column(Modifier.fillMaxSize().background(paper).padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = ink, fontSize = 30.sp, modifier = Modifier.clickable(onClick = onClose).padding(end = 16.dp))
            Text("编辑笔记", color = muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Surface(onClick = onClose, shape = CircleShape, color = ink, contentColor = Color.White) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("完成", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(35.dp))
        BasicTextField(title, onTitle, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = ink, fontSize = 29.sp, fontWeight = FontWeight.Bold, letterSpacing = (-.7).sp),
            decorationBox = { inner -> if (title.isEmpty()) Text("标题", color = Color(0xFFB6B3AB), fontSize = 29.sp, fontWeight = FontWeight.Bold); inner() })
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            noteColors.forEachIndexed { index, tint ->
                Box(Modifier.size(25.dp).clip(CircleShape).background(tint).clickable { onColor(index) }) {
                    if (index == color) Box(Modifier.align(Alignment.Center).size(8.dp).clip(CircleShape).background(ink))
                }
            }
        }
        Spacer(Modifier.height(26.dp))
        Box(Modifier.fillMaxSize()) {
            BasicTextField(body, onBody, modifier = Modifier.fillMaxSize(), textStyle = TextStyle(color = Color(0xFF514F49), fontSize = 16.sp, lineHeight = 27.sp),
                decorationBox = { inner -> if (body.isEmpty()) Text("从这里开始记录……", color = Color(0xFFB6B3AB), fontSize = 16.sp); inner() })
        }
    }
}
