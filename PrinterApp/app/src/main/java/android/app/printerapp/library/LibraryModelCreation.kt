package android.app.printerapp.library

import android.app.AlertDialog
import android.app.printerapp.Log
import android.app.printerapp.R
import android.app.printerapp.viewer.DataStorage
import android.app.printerapp.viewer.GcodeFile
import android.app.printerapp.viewer.StlFile
import android.app.printerapp.viewer.ViewerMainFragment
import android.app.printerapp.viewer.ViewerSurfaceView
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.fileChooser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ShortBuffer

/**
 *
 * this method will create a new folder structure from a file in our system
 * @author alberto-baeza
 */
object LibraryModelCreation {
    var mAlert: AlertDialog? = null
    var mName: String? = null
    var mSnapshotSurface: ViewerSurfaceView? = null
    var mSnapshotLayout: FrameLayout? = null

    private val mHandler = Handler()
    private const val WAIT_TIME = 2000

    private var mContext: Context? = null
    private var mFile: File? = null
    private var mFileQueue: ArrayList<File>? = null
    private var mCount = 0

    //Static method to create a folder structure
    @JvmStatic
    fun createFolderStructure(context: Context?, source: File?) {
        //Catch null pointer because file browser buttons aren't implemented
        if (source != null) {
            mName = source.name.substring(0, source.name.lastIndexOf('.'))
            mContext = context
            mFile = source

            /*File root = new File(LibraryController.getParentFolder().getAbsolutePath() +
					"/Files/" + mName);*/
            val root = File(LibraryController.getCurrentPath().toString() + "/" + mName)

            val mainFolder: File
            val secondaryFolder: File

            Log.i("OUT", "File " + root.absolutePath + " source " + mFile!!.name)


            //root folder
            if (root.mkdirs()) {
                if ((mFile!!.name.contains(".stl")) || (mFile!!.name.contains(".STL"))) {
                    mainFolder = File(root.absolutePath + "/_stl")
                    secondaryFolder = File(root.absolutePath + "/_gcode")
                } else {
                    mainFolder = File(root.absolutePath + "/_gcode")
                    secondaryFolder = File(root.absolutePath + "/_stl")
                }
                //gcode folder
                if (secondaryFolder.mkdir()) {
                }


                //stl folder
                if (mainFolder.mkdir()) {
                    try {
                        val target = File(mainFolder.absolutePath + "/" + mFile!!.name)


                        if (mFile!!.exists()) {
                            val `in`: InputStream = FileInputStream(mFile)
                            val out: OutputStream = FileOutputStream(target)


                            // Copy the bits from instream to outstream
                            val buf = ByteArray(1024)
                            var len: Int

                            while ((`in`.read(buf).also { len = it }) > 0) {
                                out.write(buf, 0, len)
                            }

                            `in`.close()
                            out.close()
                        } else {
                        }

                        openModel(mContext!!, target.absolutePath)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * Open model to take screenshot
     * @param context
     * @param path
     */
    private fun openModel(context: Context, path: String) {
        val generatingProjectDialog =
            LayoutInflater.from(context).inflate(R.layout.dialog_loading_project, null)
        mSnapshotLayout =
            generatingProjectDialog.findViewById<View>(R.id.framesnapshot) as FrameLayout

        Log.i("OUT", "Opening to snap $path")
        var count = context.getString(R.string.generating_project)

        if (mFileQueue != null) count += " (" + (mCount - (mFileQueue!!.size - 1)) + "/" + mCount + ")"

//        val createFolderDialog = MaterialDialog(
//            mContext!!,
//            MaterialDialog.getDEFAULT_BEHAVIOR()
//        ).show { p: MaterialDialog? -> }

        val createFolderDialog = MaterialDialog(mContext!!)
            .cancelable(false)
            .noAutoDismiss()
            .show {
        }

        createFolderDialog.show()


        //We need the alertdialog instance to dismiss it

        val file = File(path)
        val list: MutableList<DataStorage> = ArrayList()
        val data = DataStorage()

        if (StlFile.checkFileSize(file, mContext)) {
            if (LibraryController.hasExtension(0, path)) {
                StlFile.openStlFile(context, file, data, ViewerMainFragment.DO_SNAPSHOT)
            } else if (LibraryController.hasExtension(1, path)) {
                GcodeFile.openGcodeFile(context, file, data, ViewerMainFragment.DO_SNAPSHOT)
            }

            mSnapshotSurface = ViewerSurfaceView(
                context,
                list,
                ViewerSurfaceView.NORMAL,
                ViewerMainFragment.DO_SNAPSHOT,
                null
            )
            list.add(data)
        } else createFolderDialog.cancel()
    }

    /**
     * This method is called from STlFile or GcodeFile when data is ready to render. Add the view to the layout.
     */
    @JvmStatic
    fun takeSnapshot() {
        mSnapshotSurface!!.setZOrderOnTop(true)
        mSnapshotLayout!!.addView(mSnapshotSurface)
    }

    /**
     * Creates the snapshot of the model
     */
    @JvmStatic
    fun saveSnapshot(width: Int, height: Int, bb: ByteBuffer) {
        val screenshotSize = width * height

        var pixelsBuffer: IntArray? = IntArray(screenshotSize)
        bb.asIntBuffer()[pixelsBuffer]
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmap.setPixels(pixelsBuffer!!, screenshotSize - width, -width, 0, 0, width, height)
        pixelsBuffer = null

        val sBuffer = ShortArray(screenshotSize)
        val sb = ShortBuffer.wrap(sBuffer)
        bitmap.copyPixelsToBuffer(sb)

        //Making created bitmap (from OpenGL points) compatible with Android bitmap
        for (i in 0 until screenshotSize) {
            val v = sBuffer[i]
            sBuffer[i] =
                (((v.toInt() and 0x1f) shl 11) or (v.toInt() and 0x7e0) or ((v.toInt() and 0xf800) shr 11)).toShort()
        }
        sb.rewind()
        bitmap.copyPixelsFromBuffer(sb)

        try {
            val fos = FileOutputStream(
                LibraryController.getCurrentPath().toString() + "/" + mName + "/" + mName + ".thumb"
            )
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        dismissSnapshotAlert()
    }

    /**
     * Dismiss the loading project dialog after a few seconds.
     */
    private fun dismissSnapshotAlert() {
        mHandler.postDelayed({
            mAlert!!.dismiss()
            //Only show delete dialog if there is no queue //TODO
            if (mFileQueue == null) deleteFileDialog()
            else checkQueue()

            val intent = Intent("notify")
            intent.putExtra("message", "Files")
            LocalBroadcastManager.getInstance(mContext!!).sendBroadcast(intent)
        }, WAIT_TIME.toLong())
    }

    private fun deleteFileDialog() {
        val adb = AlertDialog.Builder(mContext)
        adb.setTitle(R.string.library_delete_dialog_original)
        adb.setPositiveButton(
            R.string.ok
        ) { dialogInterface, i ->
            LibraryController.deleteFiles(
                mFile
            )
        }
        adb.setNegativeButton(R.string.cancel, null)
        adb.show()

        /**
         * Use an intent because it's an asynchronous static method without any reference (yet)
         */
        val intent = Intent("notify")
        intent.putExtra("message", "Files")
        LocalBroadcastManager.getInstance(mContext!!).sendBroadcast(intent)
    }

    /************************************************************
     *
     * JOB QUEUE
     *
     */
    //Send a file list to enqueue jobs
    fun enqueueJobs(context: Context?, q: ArrayList<File>?) {
        mFileQueue = q
        mCount = mFileQueue!!.size
        createFolderStructure(context, mFileQueue!![0])
    }

    //Check if there are more files in the queue
    fun checkQueue() {
        if (mFileQueue != null) {
            mFileQueue!!.removeAt(0) //Remove last file

            if (mFileQueue!!.size > 0) { //If there are more

                createFolderStructure(
                    mContext,
                    mFileQueue!![0]
                ) //Create folder again
            } else {
                mFileQueue = null //Remove queue
                mCount = 0
            }
        }
    }
}
