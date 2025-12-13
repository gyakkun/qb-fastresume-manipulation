import Manipulation.FAST_RESUME_EXTENSION
import Manipulation.bencode
import Manipulation.getFolder
import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import com.google.common.base.Charsets.UTF_8
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.streams.asStream

fun main() {
    System.err.println("hello world")
    val logger = LoggerFactory.getLogger(Manipulation.javaClass)
    val atomicInt = AtomicInteger(0)
    val resolve = getFolder().resolve("new").apply { this.mkdirs() }
    getFolder().walk().asStream().parallel().forEach outer@{
        if (it.extension != FAST_RESUME_EXTENSION) return@outer
        runCatching {
            val map = bencode.decode(Files.readAllBytes(it.toPath()), Type.DICTIONARY)
            val trackers = map["trackers"] as ArrayList<ArrayList<ByteBuffer>>
            val origSize = trackers.sumOf { it.size }
            trackers.forEach { arr ->
                val iter = arr.iterator()
                while (iter.hasNext()) {
                    val next = iter.next()
                    val str = next.array().toString(StandardCharsets.UTF_8)
                    if (str.startsWith("udp://")) iter.remove()
                }
            }
            val newSize = trackers.sumOf { it.size }
            if (origSize == newSize) return@outer
            logger.info("Removed {} udp tracker for file {}", (origSize - newSize), it.toPath())
            val encodeByteArray = bencode.encode(map)
            it.writeBytes(encodeByteArray)
            return@outer
        }
            .onFailure { ex -> logger.error("Exception: {}", it.toPath(), ex) }
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

