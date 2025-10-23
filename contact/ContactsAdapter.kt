package com.example.contact

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter(
    private val contacts: List<Contact>,
    private val callClickListener: (String) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFirstLetter: TextView = view.findViewById(R.id.tvFirstLetter)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val ivWhatsApp: ImageView = view.findViewById(R.id.ivWhatsApp)
        val btnCall: Button = view.findViewById(R.id.btnCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvFirstLetter.text = contact.name.firstOrNull()?.toString()?.uppercase() ?: "?"
        holder.tvName.text = contact.name

        // Ici on affiche toujours l'icône WhatsApp, vous pouvez ajouter votre logique ici
        holder.ivWhatsApp.visibility = View.VISIBLE

        holder.btnCall.setOnClickListener {
            callClickListener(contact.phone)
        }
    }
}