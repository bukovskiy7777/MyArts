package com.company.art_and_culture.myarts.bottom_menu.home

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView

abstract class LifecycleViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    fun onAttached() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    fun onDetached() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
}