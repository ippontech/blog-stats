package tech.ippon.blog.stats.service

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.S3Object
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ClearValuesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import org.apache.log4j.Logger
import tech.ippon.blog.stats.model.Post
import java.io.InputStream

class SpreadsheetService {

    private val applicationName = "Blog Stats"

    // spreadsheet IDs
    private val postsSpreadsheetId: String
    private val consultantsSpreadsheetId: String

    // sheet names
    private val postsSheet = "Posts"
    private val targetConsultantsSheet = "Consultants"
    private val sourceConsultantsSheet = "Current"

    private val logger = Logger.getLogger(javaClass)

    private val sheetsService: Sheets

    init {
        postsSpreadsheetId = System.getenv("POSTS_SPREADSHEET_ID")
        consultantsSpreadsheetId = System.getenv("CONSULTANTS_SPREADSHEET_ID")

        val credentialsLocation = System.getenv("CREDENTIALS_S3_LOCATION").split(":")
        val credentialsS3Object = getCredentialsFromS3(credentialsLocation[0], credentialsLocation[1])
        sheetsService = getSheetsService(credentialsS3Object.objectContent)
    }

    // copies the list of consultants from the source of truth to the target sheet
    fun updateConsultants() {
        // read the list of consultants from the other spreadsheet
        logger.info("Reading list of consultants")
        val consultants = sheetsService.spreadsheets().values()
                .get(consultantsSpreadsheetId, "$sourceConsultantsSheet!B3:B")
                .execute()
                .getValues()
                .flatMap { it } as List<String>
        logger.info("Found ${consultants.size} consultants")

        // clear the target sheet
        logger.info("Clearing the list of consultants")
        sheetsService.spreadsheets().values()
                .clear(postsSpreadsheetId, "$targetConsultantsSheet!A2:A", ClearValuesRequest())
                .execute()
        logger.info("Done clearing")

        // inject new values in the target sheet
        logger.info("Writing the new list of consultants")
        val values = consultants.sorted().map { listOf(it) }
        sheetsService.spreadsheets().values()
                .update(postsSpreadsheetId, "$targetConsultantsSheet!A2:A", ValueRange().setValues(values))
                .setValueInputOption("USER_ENTERED")
                .execute()
        logger.info("Done writing")
    }

    // update the sheets with the list of blog posts
    fun updatePosts(posts: List<Post>) {
        // clear previous values
        sheetsService.spreadsheets().values()
                .clear(postsSpreadsheetId, "$postsSheet!A2:D", ClearValuesRequest())
                .execute()

        // inject new values
        val values = posts
                .flatMap {
                    // one row per post/author (if an article has 3 authors, generate 3 rows)
                    val post = it
                    post.authors.map { listOf(post.title, post.date ?: "", post.year(), it) }
                }
                .toList()
        sheetsService.spreadsheets().values()
                .update(postsSpreadsheetId, "$postsSheet!A2:D", ValueRange().setValues(values))
                .setValueInputOption("USER_ENTERED")
                .execute()
    }

    private fun getCredentialsFromS3(credentialsS3Bucket: String, credentialsS3Key: String): S3Object {
        val s3Client = AmazonS3ClientBuilder.defaultClient()
        return s3Client.getObject(GetObjectRequest(credentialsS3Bucket, credentialsS3Key))
    }

    private fun getSheetsService(credentialsStream: InputStream): Sheets {
        val credentials = GoogleCredential
                .fromStream(credentialsStream)
                .createScoped(listOf(SheetsScopes.SPREADSHEETS))
        return Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory(), credentials)
                .setApplicationName(applicationName)
                .build()
    }
}