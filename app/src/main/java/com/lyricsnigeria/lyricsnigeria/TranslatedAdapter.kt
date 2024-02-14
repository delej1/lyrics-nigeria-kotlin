package com.lyricsnigeria.lyricsnigeria

class TranslatedAdapter {
    var artist: String? = null
    var cover: String? = null
    var genre: String? = null
    var song: String? = null
    var uploader: String? = null
    var lyrics: String? = null


    constructor(artist: String, cover: String, genre: String, song: String, uploader: String, lyrics: String) {
        this.artist = artist
        this.cover = cover
        this.genre = genre
        this.song = song
        this.uploader = uploader
        this.lyrics = lyrics
    }

    constructor() {

    }
}