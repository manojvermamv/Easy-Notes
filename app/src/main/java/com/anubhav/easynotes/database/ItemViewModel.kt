package com.anubhav.easynotes.database

import androidx.annotation.LayoutRes

interface ItemViewModel {
    @get:LayoutRes
    var layoutId: Int

    var viewType: Int
        get() {return 0}
        set(v) {}

}