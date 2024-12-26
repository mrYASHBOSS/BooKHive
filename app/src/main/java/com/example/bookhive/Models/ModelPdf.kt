package com.example.bookhive.Models

class ModelPdf {

    //variables
    var uid: String = ""
    var id: String = ""
    var title: String = ""
    var description: String = ""
    var categoryId: String = ""
    var uri: String = ""
    var timestamp: Long = 0
    var viewCount: Long = 0
    var downloadsCount: Long = 0
    var isFavorite = false

    //empty constructor (required by firebase)
    constructor()
    //parameterized constructor
    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        uri: String,
        timestamp: Long,
        viewCount: Long,
        downloadsCount: Long,
        isFavorite: Boolean
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.uri = uri
        this.timestamp = timestamp
        this.viewCount = viewCount
        this.downloadsCount = downloadsCount
        this.isFavorite = isFavorite

    }


}