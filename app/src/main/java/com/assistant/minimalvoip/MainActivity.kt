package com.assistant.minimalvoip

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ChatScreen()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<Message>() }
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var isListening by remember { mutableStateOf(false) }

    // 添加欢迎消息
    if (messages.isEmpty()) {
        messages.add(
            Message(
                id = "1",
                text = "你好！我是贾维斯，你的智能语音助手。我可以帮助你发送语音消息和管理任务。",
                timestamp = System.currentTimeMillis(),
                isSent = false,
                type = MessageType.TEXT
            )
        )
    }

    // 请求麦克风权限
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "麦克风权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "需要麦克风权限才能使用语音功能", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ChatTopBar(
                title = "贾维斯语音助手",
                onCallClick = { 
                    Toast.makeText(context, "语音通话功能开发中...", Toast.LENGTH_SHORT).show()
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                inputText = inputText,
                onInputTextChange = { inputText = it },
                onSendClick = { text ->
                    if (text.isNotEmpty()) {
                        messages.add(
                            Message(
                                id = System.currentTimeMillis().toString(),
                                text = text,
                                timestamp = System.currentTimeMillis(),
                                isSent = true,
                                type = MessageType.TEXT
                            )
                        )
                        inputText = TextFieldValue("")
                        
                        // 模拟回复
                        coroutineScope.launch {
                            withContext(Dispatchers.Default) {
                                delay(1000) // 模拟网络延迟
                            }
                            withContext(Dispatchers.Main) {
                                messages.add(
                                    Message(
                                        id = (System.currentTimeMillis() + 1).toString(),
                                        text = "收到你的消息：\"$text\"。我会帮助你完成这个任务！",
                                        timestamp = System.currentTimeMillis(),
                                        isSent = false,
                                        type = MessageType.TEXT
                                    )
                                )
                            }
                        }
                    }
                },
                onVoiceClick = {
                    // 检查麦克风权限
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        isListening = true
                        // 模拟语音识别过程
                        coroutineScope.launch {
                            withContext(Dispatchers.Default) {
                                delay(2000) // 模拟语音识别时间
                            }
                            withContext(Dispatchers.Main) {
                                isListening = false
                                val voiceText = "这是语音识别的模拟结果"
                                messages.add(
                                    Message(
                                        id = System.currentTimeMillis().toString(),
                                        text = voiceText,
                                        timestamp = System.currentTimeMillis(),
                                        isSent = true,
                                        type = MessageType.VOICE
                                    )
                                )
                                // 模拟回复
                                delay(1000)
                                messages.add(
                                    Message(
                                        id = (System.currentTimeMillis() + 1).toString(),
                                        text = "我听到你说：\"$voiceText\"。我会帮助你处理的！",
                                        timestamp = System.currentTimeMillis(),
                                        isSent = false,
                                        type = MessageType.TEXT
                                    )
                                )
                            }
                        }
                    }
                },
                isListening = isListening
            )
        }
    ) { innerPadding ->
        ChatMessageList(
            messages = messages,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun ChatTopBar(
    title: String,
    onCallClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            
            IconButton(onClick = onCallClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_call),
                    contentDescription = "语音通话",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatMessageList(
    messages: List<Message>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        reverseLayout = false,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages) { message ->
            ChatMessageItem(message = message)
        }
    }
}

@Composable
fun ChatMessageItem(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isSent) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isSent) {
            BotAvatar()
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Surface(
            shape = RoundedCornerShape(
                topStart = if (message.isSent) 16.dp else 4.dp,
                topEnd = if (message.isSent) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = if (message.isSent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .widthIn(max = 280.dp)
            ) {
                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (message.isSent) Color.White else MaterialTheme.colorScheme.onSurface,
                                lineHeight = 1.5.sp
                            )
                        )
                    }
                    MessageType.VOICE -> {
                        VoiceMessageItem(
                            text = message.text,
                            isSent = message.isSent,
                            onPlayClick = { /* 播放语音消息 */ }
                        )
                    }
                    MessageType.IMAGE -> {
                        Text(
                            text = "图片消息: ${message.text}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    MessageType.VIDEO -> {
                        Text(
                            text = "视频消息: ${message.text}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    MessageType.FILE -> {
                        Text(
                            text = "文件消息: ${message.text}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (message.isSent) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    ),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }
        
        if (message.isSent) {
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatar()
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: TextFieldValue,
    onInputTextChange: (TextFieldValue) -> Unit,
    onSendClick: (String) -> Unit,
    onVoiceClick: () -> Unit,
    isListening: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isListening) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if (isListening) R.drawable.ic_stop else R.drawable.ic_mic
                    ),
                    contentDescription = if (isListening) "停止录音" else "语音输入",
                    tint = if (isListening) Color.White else MaterialTheme.colorScheme.primary
                )
            }
            
            BasicTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(horizontal = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                ),
                decorationBox = { innerTextField ->
                    if (inputText.text.isEmpty()) {
                        Text(
                            text = "请输入消息...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
            
            IconButton(
                onClick = { onSendClick(inputText.text) },
                enabled = inputText.text.isNotEmpty(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.text.isNotEmpty())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_send),
                    contentDescription = "发送消息",
                    tint = if (inputText.text.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun VoiceMessageItem(
    text: String,
    isSent: Boolean,
    onPlayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_play),
                contentDescription = "播放语音",
                tint = if (isSent) Color.White else MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "语音消息: $text",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isSent) Color.White else MaterialTheme.colorScheme.onSurface
                )
            )
        }
        
        Text(
            text = "2:30",
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSent) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
fun BotAvatar() {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "J",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun UserAvatar() {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondary
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "U",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

fun formatTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
}

enum class MessageType {
    TEXT,
    VOICE,
    IMAGE,
    VIDEO,
    FILE
}

data class Message(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isSent: Boolean,
    val type: MessageType = MessageType.TEXT
)

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MaterialTheme {
        ChatScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ChatMessageItemPreview() {
    MaterialTheme {
        ChatMessageItem(
            message = Message(
                id = "1",
                text = "你好！我是贾维斯，你的智能语音助手。",
                timestamp = System.currentTimeMillis(),
                isSent = false,
                type = MessageType.TEXT
            )
        )
    }
}
