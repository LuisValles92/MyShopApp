package com.example.myshop

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import com.example.myshop.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.IOException
import java.util.*


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_registro.*
import java.util.regex.Pattern

class RegistroActivity : AppCompatActivity() {

    //LINEAS DE SUBIR FOTO
    private val PICK_IMAGE_REQUEST = 1234
    private var filePath: Uri? = null
    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null
    private var imageUrl: String =
        "https://firebasestorage.googleapis.com/v0/b/myshop-48885.appspot.com/o/images%2F5083a144-f7ee-4569-bd2b-4cc0f6336fb7?alt=media&token=7a45f1d8-7af2-42e8-9c56-56e9234a393e"

    //LINEAS DE SUBIR FOTO
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST &&
            resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imagePreview!!.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    //LINEAS DE SUBIR FOTO
    private fun uploadFile() {

        if (filePath != null) {

            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Subiendo...")
            progressDialog.show()

            val imageRef = storageReference!!.child("images/" + UUID.randomUUID().toString())

            imageRef.putFile(filePath!!)

                .addOnSuccessListener { it ->

                    val result = it.metadata!!.reference!!.downloadUrl
                    result.addOnSuccessListener {

                        imageUrl = it.toString()
                        //RECOGER LINK EN BBDD

                    }

                    progressDialog.dismiss()
                    Toast.makeText(
                        applicationContext,
                        "Imagen de tienda subida con éxito",
                        Toast.LENGTH_SHORT
                    ).show()

                }

                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(
                        applicationContext,
                        "Error en la subida de imagen de tienda",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                .addOnProgressListener { taskSnapshot ->

                    val progress =
                        100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    progressDialog.setMessage("Subiendo " + progress.toInt() + "%...")

                }

        } else if (filePath == null) {

            Toast.makeText(applicationContext, "Selecciona imagen de tienda", Toast.LENGTH_SHORT)
                .show()

            imageUrl =
                "https://firebasestorage.googleapis.com/v0/b/myshop-48885.appspot.com/o/images%2F5083a144-f7ee-4569-bd2b-4cc0f6336fb7?alt=media&token=7a45f1d8-7af2-42e8-9c56-56e9234a393e"

        }

    }

    //LINEAS DE SUBIR FOTO
    private fun showFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "SELECT PICTURE"), PICK_IMAGE_REQUEST)
    }


    //LINEAS DE SWIPE REFRESH - SOLUCIÓN AL PROBLEMA DE HACER SCROW HACIA ARRIBA Y SE ACTIVA SWIPE REFRESH
    private fun swipeSolution() {
        val listener: ViewTreeObserver.OnScrollChangedListener

        listener = ViewTreeObserver.OnScrollChangedListener {
            val scrollY = scrowView2.scrollY

            swipeRefresh2.isEnabled = scrollY < 50
        }

        scrowView2.viewTreeObserver.addOnScrollChangedListener(listener)
    }

    //LINEAS DE SWIPE REFRESH
    private fun swipeAction() {

        arNombreTienda.text?.clear()
        arCP.text?.clear()
        arCorreo.text?.clear()
        arContrasenia.text?.clear()
        arRepetirContrasenia.text?.clear()
        filePath = null
        val bitmap = null
        imagePreview.setImageBitmap(bitmap)

    }


    val TAG = "RegistroActivity"
    var EMAIL_REGEX = "[-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\\.[a-zA-Z]{2,4}"
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        //LINEAS DE SWIPE REFRESH - SOLUCIÓN AL PROBLEMA DE HACER SCROW HACIA ARRIBA Y SE ACTIVA SWIPE REFRESH
        swipeSolution()

        //LINEAS DE SWIPE REFRESH
        swipeRefresh2.setOnRefreshListener {
            swipeAction()
            swipeRefresh2.isRefreshing = false
        }

        setSupportActionBar(toolbar5)

        //Botón de atrás en el Toolbar + Función
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        //LINEAS DE SUBIR FOTO
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference
        arBotonSeleccionar.setOnClickListener {
            showFileChooser()
        }
        arBotonSubir.setOnClickListener {
            uploadFile()
        }


        auth = FirebaseAuth.getInstance()

        arBotonRegistro.setOnClickListener {
            /*
            val intent = Intent(this, EmpresaActivity::class.java)
            startActivity(intent)
             */
            var error = ""
            if (arNombreTienda.text.toString().count() <= 1) {
                error += "*Nombre de tienda: Debe contener más de un caracter\n"
            }
            if (arCP.text.toString().count() != 5) {
                error += "*CP: Debe contener cinco caracteres exactos\n"
            }
            if (!checkRegex(arCorreo.text.toString(), EMAIL_REGEX)) {
                error += "*Email: Debe contener un @ y un punto, ejemplo: X@X.XX\n"
            }
            if (arContrasenia.text.toString().count() <= 4) {
                error += "*Contraseña: Debe contener más de 4 caracteres con algún número\n"
            }
            if (arRepetirContrasenia.text.toString() != arContrasenia.text.toString()) {
                error += "*Repetir contraseña: No coincide con el campo contraseña\n"
            }
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "REGISTRO EN PROCESO", Toast.LENGTH_SHORT).show()
                onSignup(
                    arNombreTienda.text.toString(),
                    arCP.text.toString(),
                    arCorreo.text.toString(),
                    arContrasenia.text.toString(),
                    arRepetirContrasenia.text.toString()
                )
            }
        }
    }

    //Función de botón de atrás en el Toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun onSignup(
        nombretienda: String,
        cp: String,
        correo: String,
        contrasenia: String,
        repcontrasenia: String
    ) {
        auth.createUserWithEmailAndPassword(correo, contrasenia)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //Log.d(TAG, "createUserWithEmail:success")
                    //updateUI(user)
                    user = auth.currentUser!!
                    verifyEmail()

                    //LINEAS PARA IMPLEMENTAR LOS DATOS AL REALTIME DATABASE DE FIREBASE
                    val database = FirebaseDatabase.getInstance()
                    val myRef = database.getReference("Tiendas")
                    val uidTienda = auth.currentUser!!.uid
                    // Guardar los datos en firebase
                    val userCollection = myRef.child(user.uid.toString())
                    userCollection.child("Nombre").setValue(nombretienda)
                    userCollection.child("CP").setValue(cp)
                    userCollection.child("Correo electrónico").setValue(correo)
                    //userCollection.child("Contraseña").setValue(contrasenia)
                    //userCollection.child("Repetir contraseña").setValue(repcontrasenia)
                    userCollection.child("CIF").setValue(uidTienda)
                    userCollection.child("URLdeimagen").setValue(imageUrl)
                    /*//FALSE SI NO ESTA VERIFICADO; TRUE SI LO ESTA
                    userCollection.child("Verificacion")
                        .setValue(auth.currentUser?.isEmailVerified.toString())*/

                    Toast.makeText(this, "REGISTRO COMPLETADO", Toast.LENGTH_SHORT).show()

                    updateprofile(nombretienda, cp, user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "ERROR: EL CORREO YA ESTÁ REGISTRADO",
                        Toast.LENGTH_SHORT
                    ).show()
                    //updateUI(null)
                }

                // ...
            }

    }

    private fun updateprofile(nombretienda: String, cp: String, user: FirebaseUser?) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName("$nombretienda $cp")
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")



                    onBackPressed()
                    //val intent= Intent(this, EmpresaActivity::class.java)
                    //startActivity(intent)
                }
            }
    }

    fun checkRegex(field: String, regex: String): Boolean {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(field)
        return matcher.matches()
    }

    private fun verifyEmail() {
        user.sendEmailVerification().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    baseContext,
                    "Correo de verificación enviado a " + user.getEmail(),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Log.e(TAG, "sendEmailVerification", task.exception)
                Toast.makeText(
                    baseContext,
                    "Error en el envío del correo de verifación",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
