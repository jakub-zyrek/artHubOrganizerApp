package com.example.arthuborganizer.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.io.File
import java.io.FileOutputStream
import java.util.Properties
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.*
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class CreateQRAndSend {
    fun generateQRCode(content: String, width: Int, height: Int): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File {
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    fun createPDFWithQRCode(context: Context, bitmap: Bitmap, pdfFileName: String, name: String, time: String, place: String, type: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width + 40, bitmap.height + 160, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawBitmap(bitmap, 20f, 10f, null)

        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.textAlign = Paint.Align.CENTER

        val xPos = (canvas.width / 2).toFloat()
        val yPos = (bitmap.height + 25).toFloat()

        canvas.drawText(name, xPos, yPos, paint)
        canvas.drawText(time, xPos, yPos + 25, paint)
        canvas.drawText(place, xPos, yPos + 50, paint)
        canvas.drawText(type, xPos, yPos + 75, paint)

        pdfDocument.finishPage(page)

        val pdfFile = File(context.filesDir, pdfFileName)
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
    }

    fun sendEmailUsingWorkManager(context: Context, subject: String, body: String, toEmail: String, pdfPaths: MutableList<Pair<String, String>>) {
        val pdfPathsString = pdfPaths.map { "${it.first}|${it.second}" }.toTypedArray()

        val inputData = workDataOf(
            "subject" to subject,
            "body" to body,
            "toEmail" to toEmail,
            "pdfPaths" to pdfPathsString
        )

        val sendEmailWorkRequest = OneTimeWorkRequestBuilder<SendEmailWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(sendEmailWorkRequest)
    }

    fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    class SendEmailWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

        override fun doWork(): Result {
            val subject = inputData.getString("subject") ?: return Result.failure()
            val body = inputData.getString("body") ?: return Result.failure()
            val toEmail = inputData.getString("toEmail") ?: return Result.failure()
            val pdfPathsString = inputData.getStringArray("pdfPaths") ?: return Result.failure()

            val pdfPaths = pdfPathsString.map {
                val parts = it.split("|")
                Pair(parts[0], parts[1])
            }.toMutableList()

            return try {
                sendEmail(subject, body, toEmail, pdfPaths)
                pdfPaths.forEach {
                    File(it.first).delete()
                }
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }

        private fun sendEmail(subject: String, body: String, toEmail: String, pdfPaths: MutableList<Pair<String, String>>) {
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication("arthuborganizer@gmail.com", "zuao nkji spam igiy")
                }
            })

            try {
                val message = MimeMessage(session)
                message.setFrom(InternetAddress("arthuborganizer@gmail.com"))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                message.subject = subject

                val multipart = MimeMultipart()

                val messageBodyPart = MimeBodyPart()
                messageBodyPart.setText(body)

                multipart.addBodyPart(messageBodyPart)

                for (item in pdfPaths) {
                    val attachmentBodyPart = MimeBodyPart()
                    attachmentBodyPart.attachFile(File(item.first))
                    attachmentBodyPart.fileName = item.second
                    multipart.addBodyPart(attachmentBodyPart)
                }

                message.setContent(multipart)
                Transport.send(message)
            } catch (e: MessagingException) {
                e.printStackTrace()
                throw e
            }
        }
    }
}
