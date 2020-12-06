package com.example.myshop.EmpresaTab


import android.app.SharedElementCallback
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myshop.R
import android.content.Intent
import android.util.Log
import android.widget.*
import androidx.appcompat.view.ActionMode
import com.example.myshop.EmpresaActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var DatabaseReference: DatabaseReference? = null
    private var Database: FirebaseDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (!user!!.isEmailVerified) {
            Toast.makeText(context, "Verifica tu correo", Toast.LENGTH_SHORT).show()
            signOut()
        }

        Database = FirebaseDatabase.getInstance()
        DatabaseReference = Database!!.reference.child("Tiendas")
        val UserReference = DatabaseReference!!.child(user.uid)

        //PARA QUE NO DE ERROR EN EL ONDATACHANGE ES NECESARIO PASARLO ASI
        val bienvenidoHome = view.findViewById<TextView>(R.id.fhbienvenidoHome)
        val imageShop = view.findViewById<ImageView>(R.id.fhimageShop)

        UserReference.addValueEventListener(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
                Log.d("MyApp", "Error $p0")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val imagenURL = snapshot.child("URLdeimagen").value as? String
                val name = snapshot.child("Nombre").value as? String
                val postalcode = snapshot.child("CP").value as? String
                val email = snapshot.child("Correo electrónico").value as? String
                bienvenidoHome.text = "Tienda:\n$name   $postalcode\n\nCorreo electrónico:\n$email"
                Picasso.get().load(imagenURL).into(imageShop)

                //LINEAS DEL PROGRESSBAR
                view.findViewById<ProgressBar>(R.id.loader)!!.visibility = View.GONE
                view.findViewById<RelativeLayout>(R.id.fhRelativeLayout)!!.visibility = View.GONE
            }
        })

        /*
        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email

            fhbienvenidoHome.text = "Tienda:\n$name\n\nCorreo electrónico:\n$email"

            // Check if user's email is verified
            val emailVerified = user.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            val uid = user.uid
        }
         */

    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(context, EmpresaActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    /*
    override fun onPreExecute() {
        super.onPreExecute()
        /* Showing the ProgressBar, Making the main design GONE */
        view?.findViewById<ProgressBar>(R.id.loader)!!.visibility = View.VISIBLE
        view?.findViewById<LinearLayout>(R.id.fhContainer)!!.visibility = View.GONE
    }

     */

}