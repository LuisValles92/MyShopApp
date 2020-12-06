package com.example.myshop

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myshop.ClienteActivity.CCPActivity
import com.example.myshop.ClienteActivity.ProductSearchActivity
import com.example.myshop.FS.FSActivity
import com.example.myshop.SliderTab.SliderActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar2)

        //Botón de atrás en el Toolbar + Función
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true)


        amBotonCP.setOnClickListener {
            val intent = Intent(this, CCPActivity::class.java)
            startActivity(intent)
        }

        amBotonProducto.setOnClickListener{
            val intent= Intent(this, ProductSearchActivity::class.java)
            startActivity(intent)
        }

        amBotonEmpresa.setOnClickListener {
            val intent = Intent(this, EmpresaActivity::class.java)
            startActivity(intent)
        }

        /*amBotonFS.setOnClickListener {
            val intent = Intent(this, FSActivity::class.java)
            startActivity(intent)
        }*/

    }

    //Función de botón de atrás en el Toolbar
    override fun onSupportNavigateUp(): Boolean {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Salir de la aplicación")

        builder.setMessage("¿Está seguro de que desea salir de la aplicación?")

        builder.setPositiveButton("SI") { dialog, which ->

            onBackPressed()
        }

        builder.setNegativeButton("NO") { dialog, which ->
        }

        val dialog: AlertDialog = builder.create()

        dialog.show()
        return true
    }

    //Cuando pulse atrás desde cualquier momento en MainActivity se saldrá de la aplicación quedando esta en segundo plano
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

}
