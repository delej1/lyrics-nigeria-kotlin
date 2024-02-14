package com.lyricsnigeria.lyricsnigeria


class ChatAdapter {
    var category: String? = null
    var topic: String? = null
    var count: Long? = null



    constructor(category: String, topic: String, count: Long) {
        this.category = category
        this.topic = topic
        this.count = count
    }

    constructor() {

    }
}