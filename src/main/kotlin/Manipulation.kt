import Manipulation.FAST_RESUME_EXTENSION
import Manipulation.bencode
import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import com.google.common.base.Charsets.UTF_8
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.util.*

fun main() {
    System.err.println("hello world")
    val logger = LoggerFactory.getLogger(Manipulation.javaClass)
    Manipulation.getFolder().walk().forEach outer@{
        if (it.extension != FAST_RESUME_EXTENSION) return@outer
        runCatching {
            val map = bencode.decode(Files.readAllBytes(it.toPath()), Type.DICTIONARY)
            logger.info("map {}", map)
        }
            .onFailure { ex -> logger.error("Exception: ", ex) }
    }
}

object Manipulation {

    private const val FAST_RESUME_FOLDER_KEY = "fastresume_folder"
    const val FAST_RESUME_EXTENSION = "fastresume"
    val bencode = Bencode(UTF_8, true)

    fun getFolder(): File {
        return Manipulation::class.java.getResourceAsStream("/constants.properties").use {
            val constants = Properties().apply { this.load(it) }
            val fastResumeFolder = constants[FAST_RESUME_FOLDER_KEY] as String
            return File(fastResumeFolder)
        }
    }
}

