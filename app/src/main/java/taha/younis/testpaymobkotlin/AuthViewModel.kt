package taha.younis.testpaymobkotlin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _userGoogle = MutableLiveData<User>()
    val userGoogle: LiveData<User> get() = _userGoogle

    private val _userFacebook = MutableLiveData<User>()
    val userFacebook: LiveData<User> get() = _userFacebook

    fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: AuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                val user = User(
                    name = firebaseUser!!.displayName,
                    email = firebaseUser.email,
                    phone = firebaseUser.phoneNumber,
                    photoUrl = firebaseUser.photoUrl.toString()
                )
                _userGoogle.value = user
            }
            .addOnFailureListener { exception ->
            }
    }

    fun handleFacebookAccessToken(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = firebaseAuth.currentUser
                    val newUser = User(
                        id = user!!.uid,
                        name = user.displayName,
                        email = user.email,
                        photoUrl = user.photoUrl.toString(),
                        phone = user.phoneNumber
                    )
                    _userFacebook.value = newUser
                } else {
                    // If sign in fails, display a message to the user. }
                }
            }
    }

    fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
        LoginManager.getInstance().logOut()
    }
}