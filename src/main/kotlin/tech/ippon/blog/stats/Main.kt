package tech.ippon.blog.stats

import tech.ippon.blog.stats.service.PostsService
import tech.ippon.blog.stats.service.SpreadsheetService

fun main(args: Array<String>) {
    updateSheet()
}

fun updateSheet() {
    val postsService = PostsService()
    val posts = postsService.loadPosts()

    val spreadsheetService = SpreadsheetService()
    spreadsheetService.updatePosts(posts)
    spreadsheetService.updateConsultants()
}
