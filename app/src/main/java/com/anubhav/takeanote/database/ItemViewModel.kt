package com.anubhav.takeanote.database

import androidx.annotation.LayoutRes
import com.anubhav.takeanote.R

interface ItemViewModel {
    @get:LayoutRes
    var layoutId: Int

    var viewType: Int
        get() {return 0}
        set(v) {}

}