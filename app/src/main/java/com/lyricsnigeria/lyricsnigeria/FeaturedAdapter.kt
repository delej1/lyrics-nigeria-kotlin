package com.lyricsnigeria.lyricsnigeria

class FeaturedAdapter {
    var artist: String? = null
    var cover: String? = null
    var genre: String? = null
    var song: String? = null
    var uploader: String? = null
    var lyrics: String? = null
    var beat: String? = null


    constructor(artist: String, cover: String, genre: String, song: String, uploader: String, lyrics: String, beat: String) {
        this.artist = artist
        this.cover = cover
        this.genre = genre
        this.song = song
        this.uploader = uploader
        this.lyrics = lyrics
        this.beat = beat
    }

    constructor() {

    }
}