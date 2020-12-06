package com.example.myshop.EmpresaTab


import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myshop.EmpresaActivity
import com.example.myshop.MainActivity

import com.example.myshop.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_update.*
import java.io.IOException
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class UpdateFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var DatabaseReference: DatabaseReference? = null
    private var Database: FirebaseDatabase? = null
    private var user: FirebaseUser? = null
    private var UserReference: DatabaseReference? = null

    //LINEAS DE SUBIR FOTO
    private val PICK_IMAGE_REQUEST = 1234
    private var filePath: Uri? = null
    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null
    private var imageUrl: String =
        "https://firebasestorage.googleapis.com/v0/b/myshop-48885.appspot.com/o/images%2F5083a144-f7ee-4569-bd2b-4cc0f6336fb7?alt=media&token=7a45f1d8-7af2-42e8-9c56-56e9234a393e"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update, container, false)
    }

    //LINEAS DE SUBIR FOTO
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST &&
            resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, filePath)
                fuimagePreview!!.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    //LINEAS DE SUBIR FOTO
    private fun uploadFile() {

        if (filePath != null) {
            val progressDialog = ProgressDialog(context)
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
                        context,
                        "Imagen de tienda subida con éxito",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(
                        context,
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
            Toast.makeText(context, "Selecciona imagen de tienda", Toast.LENGTH_SHORT)
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

        val scrowView111 = view!!.findViewById<ScrollView>(R.id.scrowView1)
        val swipeRefresh111 = view!!.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh1)

        listener = ViewTreeObserver.OnScrollChangedListener {

            val scrollY = scrowView111.scrollY

            swipeRefresh111.isEnabled = scrollY < 50
        }

        scrowView111.viewTreeObserver.addOnScrollChangedListener(listener)
    }

    //LINEAS DE SWIPE REFRESH
    private fun swipeAction() {

        fuNombreTienda.text?.clear()
        fuCP.text?.clear()
        filePath = null
        val bitmap = null
        fuimagePreview.setImageBitmap(bitmap)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //LINEAS DE SWIPE REFRESH - SOLUCIÓN AL PROBLEMA DE HACER SCROW HACIA ARRIBA Y SE ACTIVA SWIPE REFRESH
        swipeSolution()

        //LINEAS DE SWIPE REFRESH
        swipeRefresh1.setOnRefreshListener {
            swipeAction()
            swipeRefresh1.isRefreshing = false
        }

        //LINEAS DE SUBIR FOTO
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference
        fuBotonSeleccionar.setOnClickListener {
            showFileChooser()
        }
        fuBotonSubir.setOnClickListener {
            uploadFile()
        }


        auth = FirebaseAuth.getInstance()
        user = auth.currentUser


        Database = FirebaseDatabase.getInstance()
        DatabaseReference = Database!!.reference.child("Tiendas")
        UserReference = DatabaseReference!!.child(user!!.uid)

        fuBotonActualizar.setOnClickListener {

            var error = ""
            if (fuNombreTienda.text.toString().count() <= 1) {
                error += "*Nombre de tienda: Debe contener más de un caracter\n"
            }
            if (fuCP.text.toString().count() != 5) {
                error += "*CP: Debe contener cinco caracteres exactos\n"
            }
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            } else {
                val nuevoNombre = fuNombreTienda.text.toString()
                val nuevoCP = fuCP.text.toString()
                UserReference!!.child("Nombre").setValue(nuevoNombre)
                UserReference!!.child("CP").setValue(nuevoCP)
                UserReference!!.child("URLdeimagen").setValue(imageUrl)
                Toast.makeText(context, "ACTUALIZACIÓN COMPLETADA", Toast.LENGTH_SHORT).show()
            }
        }

        fuEliminarCuenta.setOnClickListener {

            val builder = AlertDialog.Builder(context)

            builder.setTitle("Eliminar cuenta")

            builder.setMessage("¿Está seguro de que desea eliminar cuenta? Se eliminarán también todos sus productos")

            builder.setPositiveButton("SI") { dialog, which ->


                //LINEAS PARA ELIMINAR LOS PRODUCTOS DE LA TIENDA QUE VAMOS A ELIMINAR
                FirebaseDatabase.getInstance().reference.child("Productos")
                    .orderByChild("tiendaCIF").equalTo(user?.uid)
                    .addValueEventListener(object : ValueEventListener {

                        //FLAG PARA QUE DEJE DE ESCUCHAR UNA VEZ ESCUCHADO + CONDICION
                        var proccesDone: Boolean = false

                        override fun onCancelled(p0: DatabaseError) {
                            Log.d("MyApp", "Error $p0")
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            //Log.d("MyApp", "resultados ok ")


                            //CONDICION
                            if (snapshot.exists() && !proccesDone) {
                                for (item in snapshot.children) {
                                    //Log.d("MyApp", "resultados ok ${item.key}")
                                    item.ref.removeValue()
                                }
                            } else {
                                proccesDone = true
                            }


                        }
                    })


                //LINEA QUE ELIMINA LOS DATOS DE LA BBDD DE LA TIENDA
                UserReference?.removeValue()
                //LINEA QUE ELIMINA EL USUARIO EN AUTENTICACION
                user!!.delete()

                signOut()

                Toast.makeText(
                    context,
                    "Cuenta eliminada con éxito",
                    Toast.LENGTH_SHORT
                ).show()

            }

            builder.setNegativeButton("NO") { dialog, which ->
            }

            val dialog: AlertDialog = builder.create()

            dialog.show()

        }

    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(context, EmpresaActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

}
