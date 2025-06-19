package com.example.recycle.appExample1.uicomponents.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recycle.appExample1.uicomponents.auth.GoogleButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun Login(
    onLoginSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = Firebase.auth
    val context = LocalContext.current

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("553782313749-bhcrrq62v8anmfst5mjlda05ntaac07r.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        onLoginSuccess(account.email ?: "Unknown")
                    } else {
                        val errorText = when (authResult.exception?.message) {
                            "The user account has been disabled by an administrator." ->
                                "계정이 비활성화되었습니다. 관리자에게 문의하세요."
                            "An internal error has occurred. [ INVALID_IDP_RESPONSE : Error 403 ]" ->
                                "내부 오류로 인해 로그인할 수 없습니다. 다시 시도해주세요."
                            else ->
                                "로그인에 실패했습니다. 인터넷 연결 상태를 확인하거나 다시 시도해주세요."
                        }
                        errorMessage = errorText
                    }
                }
        } catch (e: ApiException) {
            if (e.statusCode == 12501) {
                errorMessage = null
            } else {
                val Message = when (e.statusCode) {
                    7 -> "네트워크 연결에 문제가 있습니다. 인터넷 상태를 확인해주세요."
                    10 -> "해당 앱이 구글 로그인을 사용할 권한이 없습니다. Firebase 설정을 확인해주세요."
                    else -> "계정 인증 중 오류가 발생했습니다. (${e.statusCode})"
                }
                errorMessage = Message
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login",
            fontSize = 32.sp,
            modifier = Modifier
                .padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("PW") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClick@{
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "이메일과 비밀번호를 모두 입력해주세요."
                    return@onClick
                }
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onLoginSuccess(email)
                        } else {
                            val exception = task.exception
                            if (exception != null) {
                                val message = when ((exception as? com.google.firebase.auth.FirebaseAuthException)?.errorCode) {
                                    "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD" ->
                                        "이메일 또는 비밀번호가 올바르지 않습니다."
                                    "ERROR_INVALID_CREDENTIAL" ->
                                        "해당 계정은 구글 계정으로 가입되어 있습니다.\n구글 로그인 버튼을 사용해 주세요."
                                    else -> "로그인 실패: ${exception.localizedMessage}"
                                }
                                errorMessage = message
                            }
                        }
                    }
            },
            modifier = Modifier
                .width(150.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6A4FB6),
                contentColor = Color.White
            )
        ) {
            Text(text = "로그인")
        }

        Spacer(modifier = Modifier.height(20.dp))

        GoogleButton(
            name = "구글 로그인",
            onClick = {
                googleSignInClient.signOut().addOnCompleteListener {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                }
            }
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = Color.Red)
        }
    }
}