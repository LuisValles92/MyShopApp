package com.example.myshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myshop.EmpresaTab.HomeFragment
import com.example.myshop.EmpresaTab.TabActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignIn.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_empresa.*

class EmpresaActivity : AppCompatActivity() {
    val TAG = "EmpresaActivity"
    private lateinit var auth: FirebaseAuth

    //LINEAS DEL LOGIN CON GOOGLE
    private val RC_SIGN_IN = 1
    private var gso: GoogleSignInOptions? = null

    // LINEAS DEL LOGIN CON GOOGLE
    private fun signIn() {
        val googleSignInClient = GoogleSignIn.getClient(this, gso!!)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // LINEAS DEL LOGIN CON GOOGLE
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }
    }

    // LINEAS DEL LOGIN CON GOOGLE
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    //LINEAS PARA IMPLEMENTAR LOS DATOS AL REALTIME DATABASE DE FIREBASE
                    val database = FirebaseDatabase.getInstance()
                    val myRef = database.getReference("Tiendas")
                    val uidTienda = auth.currentUser!!.uid
                    val correo = auth.currentUser!!.email
                    val imageUrl =
                        "https://firebasestorage.googleapis.com/v0/b/myshop-48885.appspot.com/o/images%2F5083a144-f7ee-4569-bd2b-4cc0f6336fb7?alt=media&token=7a45f1d8-7af2-42e8-9c56-56e9234a393e"
                    // Guardar los datos en firebase
                    val userCollection = myRef.child(user?.uid.toString())
                    //userCollection.child("Nombre").setValue("Actualice nombre")
                    //userCollection.child("CP").setValue("Actualice CP")
                    userCollection.child("Correo electrónico").setValue(correo)
                    userCollection.child("CIF").setValue(uidTienda)
                    //userCollection.child("URLdeimagen").setValue(imageUrl)
                    val intent = Intent(this, TabActivity::class.java)
                    startActivity(intent)


                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }

                // ...
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empresa)

        // LINEAS DEL LOGIN CON GOOGLE
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        sign_in_button.setOnClickListener {
            signIn()
        }

        setSupportActionBar(toolbar3)

        //Botón de atrás en el Toolbar + Función
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val intent = Intent(this, TabActivity::class.java)
            startActivity(intent)
        }

        aeBotonOlvCont.setOnClickListener {
            val intent = Intent(this, OlvContActivity::class.java)
            startActivity(intent)
        }

        aeBotonAcceso.setOnClickListener {
            /*
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
             */


            var error = ""
            if (aeCorreo.text.toString().isEmpty()) {
                error += "*Correo electrónico: Campo vacío\n"
            }
            if (aeContrasenia.text.toString().isEmpty()) {
                error += "*Contraseña: Campo vacío\n"
            }
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            } else {
                val email = aeCorreo.text.toString()
                val contrasenia = aeContrasenia.text.toString()
                onLogin(email, contrasenia)
            }


        }

        aeBotonRegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }

        //finish()

    }

    //Función de botón de atrás en el Toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun onLogin(Email: String, Password: String) {
        auth.signInWithEmailAndPassword(Email, Password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //Log.d(TAG, "signInWithEmail:success")
                    //val user = auth.currentUser
                    //updateUI(user)
                    val intent = Intent(this, TabActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "ERROR EN EL INICIO DE SESIÓN",
                        Toast.LENGTH_SHORT
                    ).show()
                    //updateUI(null)
                }

                // ...
            }
    }
}
