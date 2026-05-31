package com.example.qunlchitiu

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.qunlchitiu.ui.ManHinh.ManHinhGioiThieu
import com.example.qunlchitiu.ui.ManHinh.ManHinhThemGiaoDich
import com.example.qunlchitiu.ui.ManHinh.ManHinhNganSach
import com.example.qunlchitiu.ui.ManHinh.ManHinhChinh
import com.example.qunlchitiu.ui.ManHinh.ManHinhNhacNho
import com.example.qunlchitiu.ui.ManHinh.ManHinhMucTieu
import com.example.qunlchitiu.ui.ManHinh.ManHinhThongKe
import com.example.qunlchitiu.ui.ManHinh.ManHinhDanhSachGiaoDich
import com.example.qunlchitiu.ui.theme.QuảnLíChiTiêuTheme
import com.example.qunlchitiu.viewmodel.DieuKhienTaiChinh

class MainActivity : FragmentActivity() {
    private val TAG = "BudgetBuddyLifecycle"
    private var isAuth by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        enableEdgeToEdge()
        setContent {
            QuảnLíChiTiêuTheme {
                if (isAuth) {
                    BudgetBuddyApp()
                } else {
                    LockScreen { showBiometricPrompt() }
                }//abcde
            }
        }
        
        showBiometricPrompt()
    }

    private fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        
        // Kiểm tra xem thiết bị có hỗ trợ hoặc đã cài đặt bảo mật chưa
        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            // Nếu người dùng hủy hoặc có lỗi, vẫn ở màn hình khóa
                            Toast.makeText(applicationContext, "Lỗi xác thực: $errString", Toast.LENGTH_SHORT).show()
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            isAuth = true
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Toast.makeText(applicationContext, "Xác thực thất bại", Toast.LENGTH_SHORT).show()
                        }
                    })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("BudgetBuddy Security")
                    .setSubtitle("Sử dụng vân tay hoặc mã khóa máy để tiếp tục")
                    .setAllowedAuthenticators(authenticators)
                    // Khi dùng DEVICE_CREDENTIAL, không được setNegativeButtonText
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            else -> {
                // Nếu thiết bị không có bảo mật (máy ảo hoặc chưa cài PIN), cho phép vào thẳng
                isAuth = true
            }
        }
    }

    @Composable
    fun LockScreen(onRetry: () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF121212)),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4E157))
            ) {
                Text("Mở khóa ứng dụng", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    fun BudgetBuddyApp() {
        val navController = rememberNavController()
        val viewModel: DieuKhienTaiChinh = viewModel()
        
        Scaffold { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { 
                    ManHinhChinh(navController, viewModel) 
                }
                composable(
                    route = "add_expense?isIncome={isIncome}&expenseId={expenseId}",
                    arguments = listOf(
                        navArgument("isIncome") { 
                            type = NavType.BoolType
                            defaultValue = false 
                        },
                        navArgument("expenseId") {
                            type = NavType.IntType
                            defaultValue = -1
                        }
                    )
                ) { backStackEntry ->
                    val isIncome = backStackEntry.arguments?.getBoolean("isIncome") ?: false
                    val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: -1
                    ManHinhThemGiaoDich(navController, viewModel, isIncome, expenseId)
                }
                composable(
                    route = "stats?isIncome={isIncome}",
                    arguments = listOf(
                        navArgument("isIncome") {
                            type = NavType.BoolType
                            defaultValue = false
                        }
                    )
                ) { backStackEntry ->
                    val isIncome = backStackEntry.arguments?.getBoolean("isIncome") ?: false
                    ManHinhThongKe(navController, viewModel, isIncome)
                }
                composable(
                    route = "transactions?category={category}",
                    arguments = listOf(
                        navArgument("category") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category")
                    ManHinhDanhSachGiaoDich(navController, viewModel, category)
                }
                composable("about") { 
                    ManHinhGioiThieu(navController) 
                }
                composable("budget_overview") {
                    ManHinhNganSach(navController, viewModel)
                }
                composable("savings_goals") {
                    ManHinhMucTieu(navController, viewModel)
                }
                composable("reminders") {
                    ManHinhNhacNho(navController, viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")
    }
}
