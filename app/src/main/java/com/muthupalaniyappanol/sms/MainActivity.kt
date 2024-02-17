package com.muthupalaniyappanol.sms

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Paper.init(applicationContext)
        setContent {
            SMSTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Scaffold(
                            topBar = {
                                 Text(text = "Automated SMS App", fontSize = 25.sp, textAlign = TextAlign.Center, modifier = Modifier
                                     .fillMaxWidth()
                                     .padding(10.dp))
                            },
                            content = {
                                Column(modifier = Modifier.padding(it)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextField(value = MainViewModel.phoneNumber.value, onValueChange = {
                                            MainViewModel.phoneNumber.value = it
                                            Paper.book().write("phoneNumber", it)
                                        }, modifier = Modifier.fillMaxWidth().padding(10.dp))
                                    }
                                    Messages(modifier = Modifier.padding(top = 10.dp))
                                }
                            }
                        )
                    }
                }
            }
        }

        setup()
        checkAndRequestPermissions()
    }


object MainViewModel {
    val messages = mutableStateOf(listOf<MessageData>())
    val phoneNumber = mutableStateOf("")
}

data class MessageData (val from :String, val to:String, val msg:String)

@Composable
fun Message(msg :MessageData, modifier: Modifier = Modifier) {
    Column(modifier = modifier
        .padding(10.dp)
        .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(10.dp))
        .padding(10.dp)
        .fillMaxWidth()
    ) {
        Text(text = "${msg.to} => ${msg.from}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground)
        Text(text = msg.msg, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun Messages(modifier: Modifier = Modifier){
    LazyColumn(
        modifier = modifier
    ) {
        items(MainViewModel.messages.value.size) {
            Message(msg=MainViewModel.messages.value[it])
        }
    }
}