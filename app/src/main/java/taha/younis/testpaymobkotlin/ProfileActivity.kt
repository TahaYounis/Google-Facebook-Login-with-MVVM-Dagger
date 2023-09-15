package taha.younis.testpaymobkotlin

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import taha.younis.testpaymobkotlin.databinding.ActivityProfileBinding
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private val profileViewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSignOut.setOnClickListener {
            profileViewModel.signOut()
            finish()
        }

        val user = intent.getSerializableExtra("user") as? User

        user.let {
            binding.apply {
                Glide.with(this@ProfileActivity).load(it!!.photoUrl).circleCrop().into(imgProfile)
                tvProfileName.text = it.name
                tvEmail.text = it.email
//                tvPhone.text = it.phone
            }
        }
    }
}