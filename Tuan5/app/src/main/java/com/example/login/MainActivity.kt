package com.example.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("Login", "Google sign in failed", e)
            // Gửi thông báo thất bại vào UI
            setContent {
                LoginScreen(
                    onGoogleSignInClick = { startSignIn() },
                    loginResult = "Đăng nhập thất bại: ${e.message}"
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("625398267176-b4ij5b21qvkum69c5bjdo4ltgntd9iis.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            LoginScreen(
                onGoogleSignInClick = { startSignIn() },
                loginResult = null
            )
        }
    }

    private fun startSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Login", "signInWithCredential:success")
                    val user = auth.currentUser
                    // Chuyển sang ProfileActivity và gửi thông tin người dùng
                    val intent = Intent(this, ProfileActivity::class.java).apply {
                        putExtra("user_name", user?.displayName)
                        putExtra("user_email", user?.email)
                        putExtra("user_photo", user?.photoUrl?.toString())
                    }
                    startActivity(intent)
                    // Bỏ finish() để giữ MainActivity trong stack
                } else {
                    Log.w("Login", "signInWithCredential:failure", task.exception)
                    setContent {
                        LoginScreen(
                            onGoogleSignInClick = { startSignIn() },
                            loginResult = "Đăng nhập thất bại: ${task.exception?.message}"
                        )
                    }
                }
            }
    }
}

@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit,
    loginResult: String?
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }

    LaunchedEffect(loginResult) {
        loginResult?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                isLoading.value = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Hình nền kéo dài từ mép trên
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Background Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f) // Giữ chiều cao của hình nền
                    .align(Alignment.TopCenter)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 0.dp, // Bỏ padding trên để hình nền kéo dài từ mép trên
                        bottom = paddingValues.calculateBottomPadding(),

                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.6f))

                // Logo và tiêu đề
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logouth),
                        contentDescription = "UTH LOGO",
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SmartTasks",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3399FF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A simple and efficient to-do app",
                        fontSize = 14.sp,
                        color = Color(0xFF3399FF)
                    )
                }

                Spacer(modifier = Modifier.height(150.dp))

                // Chào mừng
                Text(
                    text = "Welcome",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dòng mô tả chào mừng
                Text(
                    text = "Ready to explore? Log in to get started.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Nút Sign in with Google
                OutlinedButton(
                    onClick = {
                        isLoading.value = true
                        onGoogleSignInClick()
                    },
                    enabled = !isLoading.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF3399FF)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gg),
                            contentDescription = "Google Icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLoading.value) "SIGNING IN..." else "SIGN IN WITH GOOGLE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer
                Text(
                    text = "© UTHSmartTasks",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}