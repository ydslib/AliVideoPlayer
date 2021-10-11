package com.yds.aliyunvideocommon.baseadapter.entity

import java.io.Serializable

abstract class SectionEntity<T> : Serializable {
    var isHeader = false
    var t: T? = null
    var header: String? = null

    constructor(t:T?){
        this.isHeader = false
        this.header = null
        this.t = t
    }

    constructor(isHeader:Boolean,header:String?){
        this.isHeader = isHeader
        this.header = header
        this.t = null
    }
}