package com.anubhav.notedown.database

import androidx.annotation.LayoutRes

interface ItemViewModel {
    @get:LayoutRes
    var layoutId: Int

    var viewType: Int
        get() {return 0}
        set(v) {}

}