package tech.ippon.blog.stats.service

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.log4j.Logger
import tech.ippon.blog.stats.model.GitFile
import tech.ippon.blog.stats.model.Post
import java.nio.file.Paths

class PostsService {

    private val logger = Logger.getLogger(javaClass)

    fun loadPostsFromGithub(): List<Post> {
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

    fun loadPostsFromFileSystem(path: String): List<Post> {
        val files = Paths.get(path).toFile().listFiles()
        val posts = files.map {
            val lines = it.readLines()
            extractPost(lines)
        }
        return posts
    }

    private fun extractPost(lines: List<String>): Post {
        try {
            val title = findTitle(lines)
            val authors = findAuthors(lines)
            val date = findDate(lines)
            return Post(title, authors, date)
        } catch (e: Exception) {
            val firstLines = lines.take(10).joinToString("\n")
            logger.error("Failing parsing post: $firstLines")
            throw Exception("Failing parsing post: $firstLines", e)
        }
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

    private fun findDate(lines: List<String>): String? =
            lines.filter { it.startsWith("date: ") }
                    .map { it.substringAfter("date: ") }
                    .map { it.replace('T', ' ') }
                    .map { it.replace(".000Z", "") }
                    .firstOrNull()
}
