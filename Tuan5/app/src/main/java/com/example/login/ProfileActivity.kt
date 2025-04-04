package com.example.login

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userName = intent.getStringExtra("user_name") ?: "Unknown"
        val userEmail = intent.getStringExtra("user_email") ?: "No email"
        val userPhoto = intent.getStringExtra("user_photo")

        setContent {
            ProfileScreen(
                userName = userName,
                userEmail = userEmail,
                userPhoto = userPhoto,
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    userPhoto: String?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val savedDateOfBirth = sharedPreferences.getString("date_of_birth", "")
    val dateOfBirth = remember { mutableStateOf(savedDateOfBirth ?: "") }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val selectedDate = dateFormat.format(calendar.time)
            dateOfBirth.value = selectedDate
            sharedPreferences.edit().putString("date_of_birth", selectedDate).apply()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Ảnh đại diện với biểu tượng máy ảnh
        Box(
            modifier = Modifier.size(120.dp)
        ) {
            if (userPhoto != null) {
                Image(
                    painter = rememberAsyncImagePainter(userPhoto),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    color = Color.Gray
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userName.first().toString(),
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            // Biểu tượng máy ảnh
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3399FF)),
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Camera Icon",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card chứa các trường thông tin
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(4.dp, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Tên
                Text(
                    text = "Name",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = userName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email
                Text(
                    text = "Email",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = userEmail,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ngày sinh
                Text(
                    text = "Date of Birth",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = dateOfBirth.value,
                    onValueChange = { /* Không cần vì dùng DatePicker */ },
                    modifier = Modifier
                        .fillMaxWidth(),
                    readOnly = true,
                    placeholder = { Text("Select your date of birth") },
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dropdown),
                                contentDescription = "Dropdown Icon"
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))


        Button(
            onClick = {
                val googleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
                googleSignInClient.signOut().addOnCompleteListener {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    (context as ComponentActivity).finish()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF4444),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Sign Out",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nút Back
//        Button(
//            onClick = onBackClick,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(48.dp)
//                .padding(horizontal = 16.dp),
//            shape = RoundedCornerShape(8.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color(0xFF3399FF),
//                contentColor = Color.White
//            )
//        ) {
//            Text(
//                text = "Back",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium
//            )
//        }
    }
}

