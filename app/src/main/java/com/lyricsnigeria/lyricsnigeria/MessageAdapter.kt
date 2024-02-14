package com.lyricsnigeria.lyricsnigeria

class MessageAdapter {
    var name: String? = null
    var message: String? = null
    var time: String? = null

    constructor(name: String, message: String, time: String) {
        this.name = name
        this.message = message
        this.time = time

    }
    constructor() {}
}