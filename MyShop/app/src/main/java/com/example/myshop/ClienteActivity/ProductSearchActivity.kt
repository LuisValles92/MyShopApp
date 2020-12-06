package com.example.myshop.ClienteActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myshop.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_ccp.toolbar0
import kotlinx.android.synthetic.main.activity_fs.view.*
import kotlinx.android.synthetic.main.activity_product_search.*
import kotlinx.android.synthetic.main.item_productosearch.view.*

class ProductSearchActivity : AppCompatActivity() {

    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    lateinit var adapter: FirebaseRecyclerAdapter<*, *>

    //LINEAS DE BUSCADOR
    private lateinit var searchView: SearchView
    private var textoSearch: String? = null

    //Función de botón de atrás en el Toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.main3, menu)
        searchView = menu.findItem(R.id.action_search_Nombre)?.actionView as SearchView

        val queryTextListener = object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(textoEscrito: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(textoEscrito: String?): Boolean {
                //TODO Cada vez que escribimos una letra entra aqui
                textoSearch = textoEscrito
                setUpRecycler()
                return false
            }
        }
        searchView.setOnQueryTextListener(queryTextListener)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_search)

        setSupportActionBar(toolbar10)

        //Botón de atrás en el Toolbar + Función
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)

        initialise()

        setUpRecycler()

    }

    private fun initialise() {
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Productos")
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val NombreP = view.tvnombreproducto
        val PrecioP = view.tvprecioproductosearch
        val TiendaN = view.tvnombretienda
        val TiendaCIF = view.tvciftienda2
        val DescripcionP = view.tvdescripcionproducto
    }

    private fun setUpRecycler() {

        Log.v("MyApp", "textoSearch1 $textoSearch")
        val query: Query = if (textoSearch != null && textoSearch != "") {
            Log.v("MyApp", "textoSearch2 $textoSearch")
            FirebaseDatabase.getInstance()
                .reference
                .child("Productos").orderByChild("nombreP").equalTo(textoSearch)
        } else {
            Log.v("MyApp", "textoSearch3 $textoSearch")
            FirebaseDatabase.getInstance()
                .reference
                .child("Productos")
        }


        val options = FirebaseRecyclerOptions.Builder<Producto>()
            .setQuery(query, Producto::class.java)
            .build()

        recycler2Productos.layoutManager = GridLayoutManager(this, 1)
        adapter = object : FirebaseRecyclerAdapter<Producto?, ViewHolder?>(options) {

            override fun onBindViewHolder(holder: ViewHolder, p1: Int, item: Producto) {
                holder.NombreP?.text = item.NombreP
                holder.PrecioP?.text = item.PrecioP
                holder.TiendaN?.text = item.TiendaN
                holder.TiendaCIF?.text = item.TiendaCIF
                holder.DescripcionP?.text = item.DescripcionP
            }


            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder { // Create a new instance of the ViewHolder, in this case we are using a custom
                val view: View = LayoutInflater.from(this@ProductSearchActivity)
                    .inflate(R.layout.item_productosearch, parent, false)
                return ViewHolder(view)
            }

        }
        recycler2Productos.adapter = adapter
        adapter.startListening()

    }

    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

}