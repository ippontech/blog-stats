package tech.ippon.blog.stats.model

data class Post(
        val title: String,
        val author: String,
        val date: String) {

    fun year(): String = if (date == "") "" else date.substring(0, 4)
}