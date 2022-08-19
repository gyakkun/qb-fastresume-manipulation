import Manipulation.FAST_RESUME_EXTENSION
import Manipulation.bencode
import Manipulation.getFolder
import com.dampcake.bencode.Bencode
import com.dampcake.bencode.Type
import com.google.common.base.Charsets.UTF_8
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.ByteBuffer
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
            val byteBuffer = (map["info"]!! as Map<String, Object>)["name"] as ByteBuffer
            val name = String(byteBuffer.array(), UTF_8)
            if (name != "[BDMV][210929] ずっと真夜中でいいのに。 - 温れ落ち度") return@outer
            logger.info("filename {}", it.name)
            // logger.info("map {}", map)
            logger.info("torrent name {}", name)
            val encodeByteArray = bencode.encode(map)
            Files.write(getFolder().toPath().resolve("new/" + it.name), encodeByteArray)
            return@outer
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

