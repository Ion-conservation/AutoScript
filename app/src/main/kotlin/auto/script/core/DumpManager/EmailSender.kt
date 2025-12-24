package auto.script.core.DumpManager

import java.io.File

interface EmailSender {
    fun sendEmail(
        to: String,
        subject: String,
        body: String,
        attachments: List<File>
    )
}
