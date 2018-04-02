package tech.ippon.blog.stats

import tech.ippon.blog.stats.service.NotificationService
import tech.ippon.blog.stats.service.PostsService
import tech.ippon.blog.stats.service.SpreadsheetService

fun main(args: Array<String>) {
    updateSheet()
    sendNotification()
}

fun updateSheet() {
    val postsService = PostsService()
    val posts = postsService.loadPosts()

    val spreadsheetService = SpreadsheetService()
    spreadsheetService.updatePosts(posts)
    spreadsheetService.updateConsultants()
}

fun sendNotification() {
    val notificationService = NotificationService()
    notificationService.sendNotification("Blog Stats updated")
}
