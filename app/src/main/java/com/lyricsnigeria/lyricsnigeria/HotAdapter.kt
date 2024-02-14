package com.lyricsnigeria.lyricsnigeria

class HotAdapter {
    var artist: String? = null
    var cover: String? = null
    var song: String? = null
    var lyrics: String? = null
    var uploader: String? = null
    var beat: String? = null


    constructor(artist: String, cover: String, genre: String, song: String, uploader: String, lyrics: String, beat: String) {
        this.artist = artist
        this.cover = cover
        this.song = song
        this.lyrics = lyrics
        this.uploader = uploader
        this.beat = beat
    }

    constructor() {

    }
}

