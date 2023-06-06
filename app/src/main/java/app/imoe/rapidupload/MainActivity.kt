package app.imoe.rapidupload

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import app.imoe.rapidupload.ui.theme.MoeRapidUploadTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoeRapidUploadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    MainAppBar()
                }
            }
        }
    }
}


@OptIn(DelicateCoroutinesApi::class)
fun saveToBaidu(context: Context, input: String, snackbarHostState: SnackbarHostState) {
    // 指定要打开的网页URL
    // https://pcs.baidu.com/rest/2.0/pcs/file?
    // method=rapidupload&ondup=newcopy
    // &app_id=266719
    // &content-md5=abdf6bfb01f3d3a5011239503b3c8360
    // &slice-md5=abdf6bfb01f3d3a5011239503b3c8360
    // &content-length=3637
    // &path=%2F%E6%88%91%E7%9A%84%E8%B5%84%E6%BA%90%2FH%202023.5.H
    val isInputError: Boolean = if (input.isEmpty()) {
        true
    } else if (input.contains("#")) {
        input.split("#").size != 4
    } else {
        true
    }

    if (isInputError) {
        GlobalScope.launch(Dispatchers.Main) {
            snackbarHostState.showSnackbar(
                message = "秒传格式错误，请使用四段式秒传链接",
                actionLabel = "CheckUpdateAction",
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
        }
        return
    }

    val baseUrl =
        "https://pcs.baidu.com/rest/2.0/pcs/file?method=rapidupload&ondup=newcopy&app_id=266719"

    //  abdf6bfb01f3d3a5011239503b3c8360#abdf6bfb01f3d3a5011239503b3c8360#3637#H 2023.5.H
    val inputList = input.split("#")
    val contentMd5 = inputList[0]
    val sliceMd5 = inputList[1]
    val contentLength = inputList[2]
    // /秒传/
    val basePath = "%2F%E7%A7%92%E4%BC%A0%2F"
    val path = basePath + inputList[3]

    val finalUrl =
        "$baseUrl&content-md5=$contentMd5&slice-md5=$sliceMd5&content-length=$contentLength&path=$path"

    // 创建Intent，并设置Action为ACTION_VIEW
    val intent = Intent(Intent.ACTION_VIEW)

    // 将网页URL转换为Uri对象
    val uri = Uri.parse(finalUrl)

    // 设置Intent的Data为网页的Uri
    intent.data = uri

    // 启动Intent，打开外部浏览器
    startActivity(context, intent, null)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(menuAction: () -> Unit = {}) {
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = {
        SnackbarHost(snackBarHostState) {
            Snackbar(
                modifier = Modifier.padding(
                    start = 16.dp, end = 16.dp, bottom = 80.dp
                )
            ) {
                Text(text = it.visuals.message)
            }
        }
    }, topBar = {
        TopAppBar(title = {
            Text(
                text = "秒传链接转存", maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }, actions = {
            IconButton(onClick = { /* doSomething() */ }) {
                Menu(snackBarHostState) {
                    menuAction()
                }
            }
        })
    }, content = { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            MyApp(snackBarHostState)
        }
    })
}


@Composable
fun MyApp(snackBarHostState: SnackbarHostState) {
    Column(
        modifier = Modifier.fillMaxSize(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .weight(1f),
            verticalArrangement = Arrangement.Top
        ) {
            InputAndButton(snackBarHostState = snackBarHostState)
            IntroducingBox()
        }
    }
}


@OptIn(
    ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun InputAndButton(snackBarHostState: SnackbarHostState) {
    val context = LocalContext.current
    var textValue by remember { mutableStateOf("") }
    val modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(1f)
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue -> textValue = newValue },
            label = { Text("请粘贴秒传链接") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
            ),
            modifier = modifier,
            maxLines = 8,
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
        )
        OutlinedButton(
            onClick = {
                keyboardController?.hide()
                saveToBaidu(context, textValue, snackBarHostState)
            },
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .fillMaxWidth(1f),
        ) {
            Text("单个转存")
        }
        OutlinedButton(
            onClick = {
                keyboardController?.hide()
                GlobalScope.launch(Dispatchers.Main) {
                    snackBarHostState.showSnackbar(
                        message = "就写了一个小时，没写好，别点了",
                        actionLabel = "BatchSaveAction",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
                    )
                }
            },
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .fillMaxWidth(1f),
        ) {
            Text("批量转存")
        }
        OutlinedButton(
            onClick = {
                keyboardController?.hide()
                textValue = ""
            },
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxWidth(1f),
        ) {
            Text("清空")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntroducingBox() {
    val textFont = MaterialTheme.typography.bodySmall
    val textTitle = MaterialTheme.typography.titleSmall
    OutlinedCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp), onClick = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(16.dp),
        ) {
            Text(text = "秒传说明", style = textTitle)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "秒传链接由4个部分组成，分别是： " + "资源MD5、资源分片MD5、资源的大小、资源名称。",
                style = textFont
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "秒传其实就是调用了百度官方的接口，我们只要提供相应的参数即可。\n\n" + "查看打开的页面，如果显示的是ctime不是error什么的应该就成功保存了。\n\n" + "路径为：/秒传/资源名称",
                style = textFont
            )
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun Menu(snackBarHostState: SnackbarHostState, menuAction: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val isShowDialog = remember { mutableStateOf(false) }
    AboutDialog(isShowDialog)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopStart)
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
        DropdownMenu(modifier = Modifier.background(MaterialTheme.colorScheme.background),
            expanded = expanded,
            onDismissRequest = { expanded = false }) {

            DropdownMenuItem(text = { Text("设置") }, onClick = {
                expanded = false
                menuAction()
            }, leadingIcon = {
                Icon(
                    Icons.Outlined.Settings, contentDescription = "设置"
                )
            })
            DropdownMenuItem(text = { Text("检查更新") }, onClick = {
                expanded = false
                GlobalScope.launch(Dispatchers.IO) {
                    snackBarHostState.showSnackbar(
                        message = "暂时没有新的更新",
                        actionLabel = "CheckUpdateAction",
                        duration = SnackbarDuration.Short,
                        withDismissAction = true
                    )
                }
            }, leadingIcon = {
                Icon(
                    Icons.Outlined.Build, contentDescription = "检查更新"
                )
            })
            DropdownMenuItem(text = { Text("关于") }, onClick = {
                expanded = false
                isShowDialog.value = true
            }, leadingIcon = {
                Icon(
                    Icons.Outlined.Info, contentDescription = "关于"
                )
            })
        }
    }
}

@Composable
fun AboutDialog(isShowDialog: MutableState<Boolean>) {
    if (isShowDialog.value) {
        AlertDialog(onDismissRequest = {
            isShowDialog.value = false
        }, title = {
            Text(text = "关于")
        }, text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(bottom = 8.dp)
            ) {
                Text(text = "作者: RinShiro")
                Text(text = "版本号: 1.0.1")
            }
        }, confirmButton = {
            OutlinedButton(onClick = {
                isShowDialog.value = false
            }) {
                Text(text = "OK")
            }
        })
    }
}