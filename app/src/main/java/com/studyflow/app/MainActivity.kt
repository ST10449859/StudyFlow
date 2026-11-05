package com.studyflow.app

import com.studyflow.app.data.StudyFlowDatabase
import com.studyflow.app.data.TaskEntity
import com.studyflow.app.data.toEntity
import com.studyflow.app.data.toTask
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import java.util.UUID
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.studyflow.app.ui.theme.StudyFlowTheme
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ---------- DATA MODELS ----------

data class AuthRequest(
    val email: String,
    val password: String,
    val fullName: String? = null
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val weeklyStudyGoal: Int = 480,
    val language: String = "English"
)

data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val subject: String,
    val dueDate: String,
    val priority: String,
    val completed: Boolean
)

data class TaskRequest(
    val title: String,
    val description: String?,
    val subject: String,
    val dueDate: String,
    val priority: String
)

data class Habit(
    val id: String,
    val title: String,
    val frequency: String,
    val currentStreak: Int,
    val longestStreak: Int
)

data class HabitRequest(
    val title: String,
    val frequency: String
)

data class StudySessionRequest(
    val subject: String,
    val durationMinutes: Int
)

data class Progress(
    val studyMinutes: Int,
    val completedTasks: Int,
    val totalTasks: Int,
    val xp: Int,
    val level: Int,
    val streak: Int
)

data class SettingsRequest(
    val fullName: String,
    val weeklyStudyGoal: Int,
    val language: String
)

// ---------- API ----------


interface StudyFlowApi {

    @POST("api/auth/register.php")
    suspend fun register(
        @Body request: AuthRequest
    ): AuthResponse

    @POST("api/auth/login.php")
    suspend fun login(
        @Body request: AuthRequest
    ): AuthResponse

    @GET("api/tasks.php")
    suspend fun tasks(
        @Header("Authorization") auth: String
    ): List<Task>

    @POST("api/tasks.php")
    suspend fun createTask(
        @Header("Authorization") auth: String,
        @Body request: TaskRequest
    ): Task

    @PUT("api/tasks.php")
    suspend fun updateTask(
        @Header("Authorization") auth: String,
        @Query("id") id: String,
        @Body request: TaskRequest
    ): Task

    @DELETE("api/tasks.php")
    suspend fun deleteTask(
        @Header("Authorization") auth: String,
        @Query("id") id: String
    )

    @POST("api/habits.php")
    suspend fun createHabit(
        @Header("Authorization") auth: String,
        @Body request: HabitRequest
    ): Habit

    @GET("api/habits.php")
    suspend fun habits(
        @Header("Authorization") auth: String
    ): List<Habit>

    @POST("api/study-sessions.php")
    suspend fun studySession(
        @Header("Authorization") auth: String,
        @Body request: StudySessionRequest
    ): Progress

    @GET("api/progress.php")
    suspend fun progress(
        @Header("Authorization") auth: String
    ): Progress

    @PUT("api/settings.php")
    suspend fun settings(
        @Header("Authorization") auth: String,
        @Body request: SettingsRequest
    ): User
}


// ---------- API CLIENT ----------

object ApiClient {

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: StudyFlowApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StudyFlowApi::class.java)
}

// ---------- SESSION STORAGE ----------

class SessionStore(context: Context) {

    private val prefs = context.getSharedPreferences(
        "studyflow_session",
        Context.MODE_PRIVATE
    )

    var token: String?
        get() = prefs.getString("token", null)
        private set(value) {
            prefs.edit().putString("token", value).apply()
        }

    var user: User?
        get() = prefs.getString("user", null)?.let {
            com.google.gson.Gson().fromJson(it, User::class.java)
        }
        private set(value) {
            prefs.edit().putString(
                "user",
                value?.let { com.google.gson.Gson().toJson(it) }
            ).apply()
        }

    fun save(response: AuthResponse) {
        token = response.token
        user = response.user
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

// ---------- VIEW MODEL ----------

class StudyFlowViewModel : ViewModel() {

    private lateinit var store: SessionStore
    private lateinit var taskDao: com.studyflow.app.data.TaskDao

    var token by mutableStateOf<String?>(null)
        private set

    var user by mutableStateOf<User?>(null)
        private set

    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set

    var habits by mutableStateOf<List<Habit>>(emptyList())
        private set

    var progress by mutableStateOf(Progress(0, 0, 0, 0, 1, 0))
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    // Initialise local database and session storage
    fun init(context: Context) {
        if (!::store.isInitialized) {

            store = SessionStore(context)

            val database = StudyFlowDatabase.getDatabase(context)
            taskDao = database.taskDao()

            token = store.token
            user = store.user

            // Observe tasks stored locally in Room
            viewModelScope.launch {
                taskDao.getAllTasks().collect { localTasks ->
                    tasks = localTasks.map { it.toTask() }
                }
            }

            if (token != null) {
                refresh()
            }
        }
    }

    private fun auth() = "Bearer ${token ?: ""}"

    private fun error(e: Throwable) {
        Log.e("StudyFlow", "API error", e)
        message = e.message ?: "Something went wrong"
    }

    fun clearMessage() {
        message = null
    }

    // Register
    fun register(
        name: String,
        email: String,
        password: String,
        done: () -> Unit
    ) = launch {
        runCatching {
            ApiClient.api.register(
                AuthRequest(email, password, name)
            )
        }.onSuccess {
            store.save(it)
            token = it.token
            user = it.user
            done()
        }.onFailure(::error)
    }

    // Login
    fun login(
        email: String,
        password: String,
        done: () -> Unit
    ) = launch {
        runCatching {
            ApiClient.api.login(
                AuthRequest(email, password)
            )
        }.onSuccess {
            store.save(it)
            token = it.token
            user = it.user
            refresh()
            done()
        }.onFailure(::error)
    }

    // Logout
    fun logout() {
        store.clear()
        token = null
        user = null
        tasks = emptyList()
        habits = emptyList()
    }

    // Load tasks from API and cache them in Room
    fun refresh() = launch {

        if (token == null) return@launch

        loading = true

        runCatching {
            ApiClient.api.tasks(auth())
        }.onSuccess { serverTasks ->

            taskDao.insertTasks(
                serverTasks.map { it.toEntity() }
            )

        }.onFailure { exception ->
            Log.e(
                "StudyFlow",
                "Could not refresh tasks. Showing local tasks.",
                exception
            )
            message = "Offline mode: showing saved tasks"
        }

        runCatching {
            ApiClient.api.habits(auth())
        }.onSuccess {
            habits = it
        }.onFailure(::error)

        runCatching {
            ApiClient.api.progress(auth())
        }.onSuccess {
            progress = it
        }.onFailure(::error)

        loading = false
    }

    // Save a task locally first, then attempt to send it to the API
    fun addTask(request: TaskRequest) = launch {

        val localId = UUID.randomUUID().toString()

        val localTask = TaskEntity(
            id = localId,
            title = request.title,
            description = request.description,
            subject = request.subject,
            dueDate = request.dueDate,
            priority = request.priority,
            completed = false,
            isSynced = false
        )

        // Save locally immediately
        taskDao.insertTask(localTask)

        if (token == null) {
            message = "Task saved on this device"
            return@launch
        }

        // Try to save the task to the server
        runCatching {
            ApiClient.api.createTask(auth(), request)
        }.onSuccess { serverTask ->

            // Replace temporary local task with server task
            taskDao.deleteTask(localId)
            taskDao.insertTask(serverTask.toEntity())

            message = "Task saved successfully"

        }.onFailure { exception ->

            // Keep the locally saved task
            Log.e("StudyFlow", "Task saved locally; server unavailable", exception)
            message = "Task saved offline. It still needs syncing."
        }
    }

    // Update task completion locally and attempt API update
    fun completeTask(task: Task) = launch {

        val newStatus = !task.completed

        taskDao.updateTaskStatus(
            task.id,
            newStatus
        )

        if (token == null) {
            message = "Task updated locally"
            return@launch
        }

        // A temporary UUID task has not yet been created on the server
        val isLocalTask = runCatching {
            UUID.fromString(task.id)
        }.isSuccess

        if (isLocalTask) {
            message = "Task updated locally. It still needs syncing."
            return@launch
        }

        val request = TaskRequest(
            task.title,
            task.description,
            task.subject,
            task.dueDate,
            task.priority
        )

        runCatching {
            ApiClient.api.updateTask(
                auth(),
                task.id,
                request
            )
        }.onSuccess {
            taskDao.markTaskSynced(task.id)
            message = "Task updated"
        }.onFailure { exception ->
            Log.e("StudyFlow", "Task update saved locally", exception)
            message = "Task updated offline. It still needs syncing."
        }
    }

    // Delete a task when the server confirms deletion
    fun deleteTask(id: String) = launch {

        if (token == null) {
            message = "Connect to the internet to delete this task from the server"
            return@launch
        }

        runCatching {
            ApiClient.api.deleteTask(auth(), id)
        }.onSuccess {
            taskDao.deleteTask(id)
            message = "Task deleted"
        }.onFailure(::error)
    }

    // Add habit
    fun addHabit(request: HabitRequest) = launch {
        runCatching {
            ApiClient.api.createHabit(auth(), request)
        }.onSuccess {
            refresh()
        }.onFailure(::error)
    }

    // Save settings
    fun saveSettings(
        name: String,
        goal: Int,
        language: String
    ) = launch {

        runCatching {
            ApiClient.api.settings(
                auth(),
                SettingsRequest(name, goal, language)
            )
        }.onSuccess {
            user = it
            store.save(AuthResponse(token!!, it))
            message = "Settings saved"
        }.onFailure(::error)
    }

    // Save study session
    fun saveSession(
        subject: String,
        minutes: Int
    ) = launch {

        runCatching {
            ApiClient.api.studySession(
                auth(),
                StudySessionRequest(subject, minutes)
            )
        }.onSuccess {
            progress = it
            message = "Study session saved"
        }.onFailure(::error)
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}


// ---------- MAIN APP NAVIGATION ----------
@Composable
fun App(viewModel: StudyFlowViewModel = viewModel()) {

    val nav = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current

    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.init(context)
        initialized = true
    }

    if (!initialized) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val start = if (viewModel.token == null) {
        "login"
    } else {
        "dashboard"
    }

    Scaffold { pad ->

        Box(Modifier.padding(pad)) {

            NavHost(
                navController = nav,
                startDestination = start
            ) {

                composable("login") {
                    LoginScreen(
                        viewModel,
                        {
                            nav.navigate("dashboard") {
                                popUpTo("login") {
                                    inclusive = true
                                }
                            }
                        },
                        {
                            nav.navigate("register")
                        }
                    )
                }

                composable("register") {
                    RegisterScreen(viewModel) {
                        nav.navigate("dashboard") {
                            popUpTo("login") {
                                inclusive = true
                            }
                        }
                    }
                }

                composable("dashboard") {
                    DashboardScreen(viewModel, nav)
                }

                composable("tasks") {
                    TasksScreen(viewModel, nav)
                }

                composable("calendar") {
                    CalendarScreen(viewModel, nav)
                }

                composable("focus") {
                    FocusScreen(viewModel, nav)
                }

                composable("habits") {
                    HabitsScreen(viewModel, nav)
                }

                composable("progress") {
                    ProgressScreen(viewModel, nav)
                }

                composable("settings") {
                    SettingsScreen(viewModel, nav)
                }
            }

            viewModel.message?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2500)
                    viewModel.clearMessage()
                }
            }
        }
    }
}

// ---------- HEADER ----------

@Composable
fun Header(
    title: String,
    nav: NavHostController,
    showBack: Boolean = false
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (showBack) {
            IconButton(
                onClick = { nav.popBackStack() }
            ) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        }

        Text(
            title,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

// ---------- LOGIN SCREEN ----------

@Composable
fun LoginScreen(
    vm: StudyFlowViewModel,
    onDone: () -> Unit,
    onRegister: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            "StudyFlow",
            style = MaterialTheme.typography.displaySmall
        )

        Text("Study smarter. Stay organised.")

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && pass.length >= 6) {
                    vm.login(email, pass, onDone)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log in")
        }

        TextButton(onClick = {}) {
            Text("Forgot password")
        }

        TextButton(onClick = {}) {
            Text("Google SSO — PoE")
        }

        TextButton(onClick = onRegister) {
            Text("Create an account")
        }
    }
}

// ---------- REGISTER SCREEN ----------

@Composable
fun RegisterScreen(
    vm: StudyFlowViewModel,
    onDone: () -> Unit
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            "Create your account",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Password (6+ characters)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (
                    name.isNotBlank() &&
                    email.isNotBlank() &&
                    pass.length >= 6
                ) {
                    vm.register(name, email, pass, onDone)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create account")
        }
    }
}

// ---------- DASHBOARD SCREEN ----------

@Composable
fun DashboardScreen(
    vm: StudyFlowViewModel,
    nav: NavHostController
) {

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            "Good day, ${vm.user?.fullName ?: "Student"}",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Keep your StudyFlow moving.")

        Spacer(Modifier.height(18.dp))

        Card(Modifier.fillMaxWidth()) {

            Column(Modifier.padding(18.dp)) {

                Text(
                    "Today's progress",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    "${vm.progress.completedTasks}/${vm.progress.totalTasks} tasks completed"
                )

                LinearProgressIndicator(
                    progress = {
                        if (vm.progress.totalTasks == 0) {
                            0f
                        } else {
                            vm.progress.completedTasks.toFloat() /
                                    vm.progress.totalTasks
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "${vm.progress.xp} XP • Level ${vm.progress.level} • 🔥 ${vm.progress.streak} day streak"
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { nav.navigate("tasks") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("My Tasks")
        }

        Button(
            onClick = { nav.navigate("focus") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Focus Session")
        }

        Button(
            onClick = { nav.navigate("habits") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Study Habits")
        }

        Button(
            onClick = { nav.navigate("progress") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Progress & Rewards")
        }

        Button(
            onClick = { nav.navigate("calendar") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calendar")
        }

        Button(
            onClick = { nav.navigate("settings") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Settings")
        }
    }
}

// ---------- TASKS SCREEN ----------

@Composable
fun TasksScreen(
    vm: StudyFlowViewModel,
    nav: NavHostController
) {

    var show by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Header("My Tasks", nav, true)

        Button(
            onClick = { show = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ New Task")
        }

        LazyColumn {

            items(vm.tasks) { task ->

                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {

                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(Modifier.weight(1f)) {

                            Text(
                                task.title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text("${task.subject} • Due ${task.dueDate}")

                            Text("Priority: ${task.priority}")
                        }

                        Checkbox(
                            checked = task.completed,
                            onCheckedChange = {
                                vm.completeTask(task)
                            }
                        )
                    }
                }
            }
        }
    }

    if (show) {

        AlertDialog(

            onDismissRequest = {
                show = false
            },

            confirmButton = {

                Button(
                    onClick = {

                        if (
                            title.isNotBlank() &&
                            subject.isNotBlank()
                        ) {

                            vm.addTask(
                                TaskRequest(
                                    title,
                                    null,
                                    subject,
                                    if (due.isBlank()) "No date" else due,
                                    priority
                                )
                            )

                            show = false
                            title = ""
                            subject = ""
                            due = ""
                            priority = "MEDIUM"
                        }
                    }
                ) {
                    Text("Save")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = { show = false }
                ) {
                    Text("Cancel")
                }
            },

            title = {
                Text("New task")
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") }
                    )

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") }
                    )

                    OutlinedTextField(
                        value = due,
                        onValueChange = { due = it },
                        label = { Text("Due date") }
                    )

                    OutlinedTextField(
                        value = priority,
                        onValueChange = {
                            priority = it.uppercase()
                        },
                        label = { Text("Priority") }
                    )
                }
            }
        )
    }
}

// ---------- CALENDAR SCREEN ----------

@Composable
fun CalendarScreen(
    vm: StudyFlowViewModel,
    nav: NavHostController
) {

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Header("Calendar", nav, true)

        Text(
            "Upcoming academic work",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        vm.tasks.sortedBy { it.dueDate }.forEach { task ->

            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {

                ListItem(
                    headlineContent = {
                        Text(task.title)
                    },
                    supportingContent = {
                        Text("${task.dueDate} • ${task.subject}")
                    }
                )
            }
        }
    }
}

// ---------- FOCUS SCREEN ----------

@Composable
fun FocusScreen(
    vm: StudyFlowViewModel,
    nav: NavHostController
) {

    var seconds by remember { mutableIntStateOf(1500) }
    var running by remember { mutableStateOf(false) }
    var subject by remember { mutableStateOf("Mathematics") }

    LaunchedEffect(running) {

        while (running && seconds > 0) {
            kotlinx.coroutines.delay(1000)
            seconds--
        }

        if (seconds == 0) {
            running = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Header("Focus Session", nav, true)

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") }
        )

        Spacer(Modifier.height(50.dp))

        Text(
            String.format("%02d:%02d", seconds / 60, seconds % 60),
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { running = !running },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (running) "Pause" else "Start")
        }

        Button(
            onClick = {
                vm.saveSession(subject, 25)
                seconds = 1500
                running = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Finish 25-minute session")
        }

        Text("Pomodoro: 25 minutes focus + 5 minutes break")
    }
}

// ---------- HABITS SCREEN ----------

@Composable
fun HabitsScreen(
    vm: StudyFlowViewModel,
    nav: NavHostController
) {

    var title by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Daily") }

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Header("Study Habits", nav, true)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Habit name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = frequency,
            onValueChange = { frequency = it },
            label = { Text("Frequency") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    vm.addHabit(HabitRequest(title, frequency))
                    title = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add habit")
        }

        LazyColumn {

            items(vm.habits) { habit ->

                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {

                    ListItem(
                        headlineContent = {
                            Text(habit.title)
                        },
                        supportingContent = {
                            Text(
                                "${habit.frequency} • 🔥 ${habit.currentStreak} day streak • Best ${habit.longestStreak}"
                            )
                        }
                    )
                }
            }
        }
    }
}

// ---------- PROGRESS SCREEN ----------

@Composable
fun ProgressScreen(
    vm: StudyFlowViewModel,
    nav: NavHostController
) {

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    val p = vm.progress

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Header("Progress & Rewards", nav, true)

        Card(Modifier.fillMaxWidth()) {

            Column(Modifier.padding(20.dp)) {

                Text(
                    "${p.xp} XP",
                    style = MaterialTheme.typography.displaySmall
                )

                Text("Level ${p.level}")
                Text("Study time: ${p.studyMinutes} minutes")
                Text("Tasks: ${p.completedTasks}/${p.totalTasks}")
                Text("🔥 ${p.streak} day streak")
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Badges",
            style = MaterialTheme.typography.titleLarge
        )

        Text("🏆 First Study Session")
        Text("📚 Assignment Finisher")
        Text("🔥 7-Day Streak")
    }
}

// ---------- SETTINGS SCREEN ----------

@Composable
fun SettingsScreen(
    vm: StudyFlowViewModel,
    nav: NavHostController
) {

    var name by remember(vm.user) {
        mutableStateOf(vm.user?.fullName ?: "")
    }

    var goal by remember(vm.user) {
        mutableStateOf(
            (vm.user?.weeklyStudyGoal ?: 480).toString()
        )
    }

    var lang by remember(vm.user) {
        mutableStateOf(vm.user?.language ?: "English")
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Header("Settings", nav, true)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = goal,
            onValueChange = {
                goal = it.filter { character -> character.isDigit() }
            },
            label = { Text("Weekly study goal (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = lang,
            onValueChange = { lang = it },
            label = { Text("Language") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                vm.saveSettings(
                    name,
                    goal.toIntOrNull() ?: 480,
                    lang
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save settings")
        }

        Button(
            onClick = {
                vm.logout()
                nav.navigate("login") {
                    popUpTo(0)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log out")
        }

        Text(
            "PoE additions planned: SSO, Room offline sync, FCM push notifications, Afrikaans and isiXhosa localisation."
        )
    }
}

// ---------- MAIN ACTIVITY ----------

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StudyFlowTheme {
                App()
            }
        }
    }
}
