package taha.younis.testpaymobkotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.facebook.*
import com.facebook.internal.CallbackManagerImpl
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import taha.younis.testpaymobkotlin.Constants.RC_GOOGLE_SIGN_IN
import taha.younis.testpaymobkotlin.databinding.ActivityMainBinding
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val authViewModel by viewModels<AuthViewModel>()
    private lateinit var callbackManager: CallbackManager

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        setupFacebookSignIn()
        setupGoogleSignIn()

        authViewModel.userGoogle.observe(this) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user", it)
            startActivity(intent)
        }
        authViewModel.userFacebook.observe(this){
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user", it)
            startActivity(intent)
        }
    }

    private fun setupFacebookSignIn() {
        binding.btnFacebookSignIn.setOnClickListener {
            val permissions = listOf("email", "public_profile")
            LoginManager.getInstance().logInWithReadPermissions(this@MainActivity, permissions)
            LoginManager.getInstance()
                .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        authViewModel.handleFacebookAccessToken(result.accessToken)
                    }

                    override fun onCancel() {}
                    override fun onError(error: FacebookException) {}
                })
        }
    }

    private fun setupGoogleSignIn() {
        binding.btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN && resultCode == Activity.RESULT_OK) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    authViewModel.signInWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                // Handle sign-in failure
            }
        } else if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }
}
