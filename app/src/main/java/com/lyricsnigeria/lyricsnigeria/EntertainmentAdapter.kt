package com.lyricsnigeria.lyricsnigeria

class EntertainmentAdapter {

    var headline: String? = null
    var cover: String? = null
    var body: String? = null
    var source: String? = null


    constructor(headline: String, cover: String, content: String, source: String) {
        this.headline = headline
        this.cover = cover
        this.body = content
        this.source = source

    }

    constructor() {

    }
}