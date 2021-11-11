package com.example.myapplication

import android.Manifest
import android.Manifest.permission
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import android.os.StrictMode
import android.provider.Settings
import androidx.core.content.FileProvider
import android.content.pm.ResolveInfo
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi







//@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private var downloadId: Long? = null
    private var manager: DownloadManager? = null
    private var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        println("onCreate method")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println("download apk")

        var destination: String? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            println("Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q")
            destination = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        } else {
            println("Build.VERSION.SDK_INT < Build.VERSION_CODES.Q")
            destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        }

        //var destination: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        println("destination = $destination")
        //println("destination new = ${this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()}")

        val fileName = "AppName.apk"
        destination += fileName
        //println("destination = $destination")
        val uri = Uri.parse("file://" + destination)
        //val uri = Uri.parse("content://" + destination)
        println("uri = $uri")
        file = File(destination)
        if (file!!.exists()) {
            println("file is exists")
            file!!.delete()
        } else {
            println("file don't exists")
        }

        /*
        var destination = File(this.getFilesDir(), "mydir")
        if (!destination.exists()) {
            destination.mkdir()
        }
        val fileName = "AppName.apk"

        println("destination = $destination")
        val uri = Uri.parse("file://" + destination.toString() + "/" + fileName)
        println("uri = $uri")
        val file = File(destination, fileName)
        if (file.exists()) {
            println("file is exists")
            file.delete()
        } else {
            println("file don't exists")
        }
        */
        val url = "https://scontent.whatsapp.net/v/t61.25591-34/255773726_1228604364320961_7117821380740611394_n.apk/WhatsApp.apk?ccb=1-5&_nc_sid=4a4126&_nc_ohc=iWMj4laMQtYAX8FfxUr&_nc_ht=scontent.whatsapp.net&oh=d22aced36e22584f96bd14837acfdda3&oe=618DFFD4"

        val request = DownloadManager.Request(Uri.parse(url))
        println("manager request = " + request.toString())
        request.setDescription("Description text")
        println("request description")
        request.setTitle("Title text")
        println("request title")
        request.setDestinationUri(uri)
        println("set destination")

        //val v: DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        //val manager: DownloadManager = baseContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager = baseContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        //var downloadId: Long? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            println("Build.VERSION.SDK_INT >= Build.VERSION_CODES.M")
            if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                println("permission WRITE_EXTERNAL_STORAGE granted")
                downloadId = manager!!.enqueue(request)
                println("downloadId = $downloadId")
            } else {
                println("permission WRITE_EXTERNAL_STORAGE denied")

                //val set: MutableSet<String> = mutableSetOf()
                //set.add(permission.WRITE_EXTERNAL_STORAGE)
                //ActivityCompat.requestPermissions(this, set.toTypedArray(), 1)
                ActivityCompat.requestPermissions(this, arrayOf(permission.WRITE_EXTERNAL_STORAGE), 1)
                //downloadId = manager.enqueue(request)
                println("downloadId = $downloadId")
            }
        } else {
            println("Build.VERSION.SDK_INT < Build.VERSION_CODES.M")
            try {
                downloadId = manager!!.enqueue(request)
            } catch (e: Exception) {
                println("error = ${e.message}")
            }

        }

        if (downloadId != null) {
            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    /*
                    // этот может использоваться вместо FileProvider
                    // https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                        m.invoke(null)
                    }
                    */


                    println("then flags")
                    val install: Intent?

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (packageManager.canRequestPackageInstalls()) {
                            println("can request package installs")
                            //install = Intent(Intent.ACTION_INSTALL_PACKAGE)
                            install = Intent(Intent.ACTION_VIEW)
                            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                            val photoURI = FileProvider.getUriForFile(baseContext, baseContext.getApplicationContext().getPackageName() + ".provider", file!!);
                            install.setDataAndType(photoURI,
                                manager!!.getMimeTypeForDownloadedFile(downloadId!!))

                            println("setDataAntType")
                            try {
                                startActivity(install)
                                println("after startActivity")
                                baseContext.unregisterReceiver(this)
                                println("finish receiver()")
                                finish()
                                println("after finish")
                            } catch (e: Exception) {
                                println("error = ${e.message}")
                            }
                        } else {
                            println("do not request package installs")
                            install = Intent()
                                .setAction(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                                .setData(Uri.parse(String.format("package:%s", packageName)))

                            startActivityForResult(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getPackageName()))), 1234)

                            /*
                            println("create intent")
                            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            println("add flags")
                            val photoURI = FileProvider.getUriForFile(baseContext, baseContext.getApplicationContext().getPackageName() + ".provider", file);
                            install.setDataAndType(photoURI,
                                manager.getMimeTypeForDownloadedFile(downloadId))
                            println("install intent = ${install.toString()}")
                            */

                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                        install = Intent(Intent.ACTION_VIEW)
                        install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        val photoURI = FileProvider.getUriForFile(baseContext, baseContext.getApplicationContext().getPackageName() + ".provider", file!!);
                        install.setDataAndType(photoURI,
                            manager!!.getMimeTypeForDownloadedFile(downloadId!!))


                        println("setDataAntType")
                        try {
                            startActivity(install)
                            println("after startActivity")
                            baseContext.unregisterReceiver(this)
                            println("finish receiver()")
                            finish()
                            println("after finish")
                        } catch (e: Exception) {
                            println("error = ${e.message}")
                        }
                    } else {
                        install = Intent(Intent.ACTION_VIEW)
                        install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        //intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK);
                        install.setDataAndType(uri,
                            manager!!.getMimeTypeForDownloadedFile(downloadId!!))

                        println("setDataAntType")
                        try {
                            startActivity(install)
                            println("after startActivity")
                            baseContext.unregisterReceiver(this)
                            println("finish receiver()")
                            finish()
                            println("after finish")
                        } catch (e: Exception) {
                            println("error = ${e.message}")
                        }
                    }
                }
            }
            baseContext.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    @RequiresApi(api = VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("onActivityResult")
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            if (packageManager.canRequestPackageInstalls()) {
                println("after can request package installs")

                println("can request package installs")
                //install = Intent(Intent.ACTION_INSTALL_PACKAGE)
                val install = Intent(Intent.ACTION_VIEW)
                install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                val photoURI = FileProvider.getUriForFile(baseContext, baseContext.getApplicationContext().getPackageName() + ".provider", file!!);
                install.setDataAndType(photoURI,
                    manager!!.getMimeTypeForDownloadedFile(downloadId!!))

                println("setDataAntType")
                try {
                    startActivity(install)
                    println("after startActivity")
                    //baseContext.unregisterReceiver(this)
                    println("finish receiver()")
                    finish()
                    println("after finish")
                } catch (e: Exception) {
                    println("error = ${e.message}")
                }
            }
        } else {
            //give the error
            println("give the error")
        }
    }
}