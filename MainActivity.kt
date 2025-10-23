package com.example.contact


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CONTACTS = 100
    private val PERMISSION_REQUEST_CALL = 101

    private lateinit var btnRequestPermission: Button
    private lateinit var rvContacts: RecyclerView
    private lateinit var adapter: ContactsAdapter
    private val contactsList = mutableListOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRequestPermission = findViewById(R.id.btnRequestPermission)
        rvContacts = findViewById(R.id.rvContacts)
        rvContacts.layoutManager = LinearLayoutManager(this)
        adapter = ContactsAdapter(contactsList, this::onCallClicked)
        rvContacts.adapter = adapter

        btnRequestPermission.setOnClickListener {
            checkContactsPermission()
        }

        // Charger contacts si permission déjà accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
            btnRequestPermission.isEnabled = false
        }
    }

    private fun checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), PERMISSION_REQUEST_CONTACTS)
        } else {
            loadContacts()
            btnRequestPermission.isEnabled = false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadContacts()
                    btnRequestPermission.isEnabled = false
                } else {
                    Toast.makeText(this, "Permission contacts refusée", Toast.LENGTH_SHORT).show()
                }
            }
            PERMISSION_REQUEST_CALL -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Rien à faire ici, l'appel est déclenché dans onCallClicked après permission
                } else {
                    Toast.makeText(this, "Permission appel refusée", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadContacts() {
        contactsList.clear()
        val uri = ContactsContract.Contacts.CONTENT_URI
        val cursor = contentResolver.query(uri, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")

        cursor?.use {
            if (it.count > 0) {
                while (it.moveToNext()) {
                    val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                    val hasPhoneNumber = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                    if (hasPhoneNumber > 0) {
                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(id),
                            null
                        )
                        phoneCursor?.use { pCursor ->
                            while (pCursor.moveToNext()) {
                                val phoneNumber = pCursor.getString(pCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                contactsList.add(Contact(id, name ?: "Inconnu", phoneNumber))
                            }
                        }
                    }
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun onCallClicked(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startCall(phoneNumber)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), PERMISSION_REQUEST_CALL)
        }
    }

    private fun startCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }
}