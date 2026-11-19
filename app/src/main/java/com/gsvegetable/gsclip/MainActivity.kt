package com.gsvegetable.gsclip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.neoruaa.glasmio.core.GlasmioTheme
import io.github.neoruaa.glasmio.glass.GlasmioBackdropHost

private data class Note(val id: Long, val title: String, val body: String, val color: Color)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GsclipApp() }
    }
}

@Composable
private fun GsclipApp() {
    val notes = remember {
        mutableStateListOf(
            Note(1, "灵感收集", "把突然想到的点子先放在这里。\n不需要整理，先记录。", Color(0xFFE8E0D3)),
            Note(2, "周末计划", "去逛一家没去过的书店\n整理桌面和照片", Color(0xFFDCE7E2)),
            Note(3, "关于一个新项目", "让笔记像便签一样自由地散落在画布上。", Color(0xFFE5DFF0)),
        )
    }
    var canvasMode by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Note?>(null) }
    var search by remember { mutableStateOf("") }
    var draftTitle by remember { mutableStateOf("") }
    var draftBody by remember { mutableStateOf("") }

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
                        onClose = {
                            val old = editing
                            if (draftTitle.isNotBlank() || draftBody.isNotBlank()) {
                                if (old == null || old.id == -1L) notes.add(0, Note(System.currentTimeMillis(), draftTitle.ifBlank { "未命名笔记" }, draftBody, Color(0xFFE8E0D3)))
                                else {
                                    val index = notes.indexOfFirst { it.id == old.id }
                                    if (index >= 0) notes[index] = old.copy(title = draftTitle, body = draftBody)
                                }
                            }
                            editing = null
                        }
                    )
                } else {
                    HomeScreen(
                        notes = notes.filter { search.isBlank() || it.title.contains(search, true) || it.body.contains(search, true) },
                        canvasMode = canvasMode, search = search,
                        onSearchChange = { search = it }, onModeChange = { canvasMode = it },
                        onCreate = {
                            draftTitle = ""; draftBody = ""
                            editing = Note(-1, "", "", Color(0xFFE8E0D3))
                        },
                        onOpen = { note ->
                            editing = note
                            draftTitle = note.title; draftBody = note.body
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun HomeScreen(
    notes: List<Note>, canvasMode: Boolean, search: String,
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
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (canvasMode) "灵感画布" else "最近记录", style = MaterialTheme.typography.titleMedium, color = Color(0xFF33312E), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("${notes.size} 条", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
            Spacer(Modifier.height(12.dp))
            AnimatedContent(targetState = canvasMode, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "view-mode") { canvas ->
                LazyColumn(contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(notes, key = { it.id }) { note -> NoteCard(note, canvas, onClick = { onOpen(note) }) }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(note: Note, canvas: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(if (canvas) 26.dp else 22.dp),
        color = if (canvas) note.color.copy(alpha = .86f) else Color.White.copy(alpha = .72f),
        tonalElevation = 0.dp, shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(note.title.ifBlank { "未命名笔记" }, style = MaterialTheme.typography.titleMedium, color = Color(0xFF262522), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(note.body, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF77736D), maxLines = 3)
        }
    }
}

@Composable
private fun EditorScreen(
    title: String, body: String, onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit, onClose: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F3F0)).padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(26.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onClose) { Icon(Icons.Rounded.Close, null); Text("完成") }
            Text("正在编辑", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(22.dp))
        OutlinedTextField(value = title, onValueChange = onTitleChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("标题") }, textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), singleLine = true)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(value = body, onValueChange = onBodyChange, modifier = Modifier.weight(1f).fillMaxWidth(), placeholder = { Text("写点什么……") }, textStyle = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(20.dp))
    }
}
