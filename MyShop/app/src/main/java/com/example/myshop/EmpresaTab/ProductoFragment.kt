package com.example.myshop.EmpresaTab


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myshop.ClienteActivity.Producto

import com.example.myshop.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.custom_modal_dialog_producto.view.*
import kotlinx.android.synthetic.main.fragment_producto.*
import kotlinx.android.synthetic.main.item_producto.view.*

/**
 * A simple [Fragment] subclass.
 */
class ProductoFragment : Fragment() {

    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    lateinit var adapter: FirebaseRecyclerAdapter<*, *>

    //LINEAS PARA COGER CIF Y NOMBRE DE TIENDA
    private lateinit var auth: FirebaseAuth
    private var DatabaseReference: DatabaseReference? = null
    private var Database: FirebaseDatabase? = null
    private var user: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_producto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //LINEAS PARA COGER CIF Y NOMBRE DE TIENDA
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        fav.setOnClickListener {
            alertProductDialog()
        }

        initialise()

        setUpRecycler()
    }

    private fun initialise() {
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Productos")
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreProducto = view.tvnombreproducto
        val precioProducto = view.tvprecioproducto
        val descripcionProducto = view.tvdescripcionproducto
        val deleteProducto = view.iv_cross
    }

    private fun setUpRecycler() {

        //IMPORTANTE
        val query =
            FirebaseDatabase.getInstance().reference.child("Productos").orderByChild("tiendaCIF")
                .equalTo(user?.uid)


        val options = FirebaseRecyclerOptions.Builder<Producto>()
            .setQuery(query, Producto::class.java)
            .build()

        recyclerProductos.layoutManager = GridLayoutManager(context, 1)
        adapter = object : FirebaseRecyclerAdapter<Producto?, ViewHolder?>(options) {

            override fun onBindViewHolder(holder: ViewHolder, p1: Int, item: Producto) {
                holder.nombreProducto?.text = item.NombreP
                holder.precioProducto?.text = item.PrecioP
                holder.descripcionProducto?.text = item.DescripcionP
                holder.deleteProducto.setOnClickListener {

                    //AlertDialogBuilder
                    val dBuilder = context?.let {
                        AlertDialog.Builder(it)
                            .setTitle("Eliminar producto")

                            .setMessage("¿Está seguro de que desea eliminar producto?")

                            .setPositiveButton("SI") { dialog, which ->

                                removeItem(snapshots[holder.adapterPosition])

                            }

                            .setNegativeButton("NO") { dialog, which ->
                            }

                    }
                    //show dialog
                    dBuilder?.show()

                }

            }

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder { // Create a new instance of the ViewHolder, in this case we are using a custom
                val view: View = LayoutInflater.from(context)
                    .inflate(R.layout.item_producto, parent, false)
                return ViewHolder(view)
            }

        }
        recyclerProductos.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }


    private fun removeItem(todoItem: Producto) {

        mDatabaseReference!!.child(todoItem.IdP.toString()).removeValue()
        Toast.makeText(
            context,
            "Producto eliminado",
            Toast.LENGTH_SHORT
        ).show()

    }

    private fun alertProductDialog() {
        //Inflate the dialog with custom view
        val mDialogView =
            LayoutInflater.from(context).inflate(R.layout.custom_modal_dialog_producto, null)
        //AlertDialogBuilder
        val mBuilder = context?.let {
            AlertDialog.Builder(it)
                .setView(mDialogView)
                .setTitle("Añadir Producto")
        }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        //login button click of custom layout
        mDialogView.dialogAnadirBtn.setOnClickListener {
            val producto = Producto()
            val productoTitle = mDialogView.findViewById<EditText>(R.id.etnombreproducto)
            val productoDescription = mDialogView.findViewById<EditText>(R.id.etdescripcionproducto)
            val productoPrice = mDialogView.findViewById<EditText>(R.id.etpreciopoducto)


            producto.NombreP = productoTitle.text.toString()
            producto.DescripcionP = productoDescription.text.toString()
            producto.PrecioP = productoPrice.text.toString()

            //LINEAS PARA COGER ID DE PRODUCTO
            val NewItem = mDatabaseReference!!.push()
            producto.IdP = NewItem.key


            //LINEAS PARA COGER CIF Y NOMBRE DE TIENDA
            Database = FirebaseDatabase.getInstance()
            DatabaseReference = Database!!.reference.child("Tiendas")
            val UserReference = DatabaseReference!!.child(user!!.uid)
            UserReference.addValueEventListener(object : ValueEventListener {

                //FLAG PARA QUE DEJE DE ESCUCHAR UNA VEZ ESCUCHADO + CONDICION
                var proccesDone: Boolean = false

                override fun onCancelled(p0: DatabaseError) {
                    Log.d("MyApp", "Error $p0")
                }

                override fun onDataChange(snapshot: DataSnapshot) {


                    //CONDICION
                    if (snapshot.exists() && !proccesDone) {
                        for (item in snapshot.children) {
                            val nameProductShop = snapshot.child("Nombre").value as? String
                            val cifProductShop = snapshot.child("CIF").value as? String
                            producto.TiendaN = nameProductShop
                            producto.TiendaCIF = cifProductShop
                            NewItem.setValue(producto)
                        }
                    } else {
                        proccesDone = true
                    }


                }
            })


            Toast.makeText(
                context,
                "Producto añadido con la ID " + producto.IdP,
                Toast.LENGTH_SHORT
            ).show()

            mAlertDialog?.dismiss()
        }
        //cancel button click of custom layout
        mDialogView.dialogCancelBtn.setOnClickListener {
            mAlertDialog?.dismiss()
        }
    }

}
