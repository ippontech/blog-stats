package tech.ippon.blog.stats.service

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.logging.log4j.LogManager
import tech.ippon.blog.stats.model.GitFile
import tech.ippon.blog.stats.model.Post

class PostsService {

    private val logger = LogManager.getLogger(javaClass)

    fun loadPosts(): List<Post> {
        val requestFactory = NetHttpTransport().createRequestFactory()

        logger.info("Loading list of posts from GitHub")
        val request = requestFactory.buildGetRequest(
                GenericUrl("https://api.github.com/repos/ippontech/blog-usa/contents/posts"))
        val rawResponse = request.execute().parseAsString()
        val type = object : TypeToken<List<GitFile>>() {}.type
        val gitFiles: List<GitFile> = Gson().fromJson(rawResponse, type)
        logger.info("Found ${gitFiles.size} files")

        logger.info("Loading each post")
        val posts = gitFiles.map { requestFactory.buildGetRequest(GenericUrl(it.downloadUrl)) }
                .map { it.executeAsync() }
                .map { it.get() }
                .map { it.parseAsString() }
                .map { it.split('\n') }
                .map { extractPost(it) }
        logger.info("Done loading posts")

        return posts
    }

    private fun extractPost(lines: List<String>): Post {
        val title = findTitle(lines)
        val authors = findAuthors(lines).stream()
        val author = authors.findFirst().get()
        val date = findDate(lines)
        return Post(title, author, date)
    }

    private fun findAuthors(lines: List<String>): List<String> {
        val idx = lines.indexOf("authors:")
        return lines.drop(idx + 1)
                .takeWhile { it.startsWith("- ") }
                .map { it.substringAfter("- ") }
    }

    private fun findTitle(lines: List<String>): String =
            lines.filter { it.startsWith("title: ") }
                    .map { it.substringAfter("title: ") }
                    .map { it.trimStart('"').trimEnd('"') }
                    .first()

    private fun findDate(lines: List<String>): String =
            lines.filter { it.startsWith("date: ") }
                    .map { it.substringAfter("date: ") }
                    .map { it.replace('T', ' ') }
                    .map { it.replace(".000Z", "") }
                    .first()
}
