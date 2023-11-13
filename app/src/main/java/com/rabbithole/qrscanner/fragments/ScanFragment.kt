package com.rabbithole.qrscanner.fragments

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.GONE
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import com.rabbithole.qrscanner.MyAdapter
import com.rabbithole.qrscanner.R
import com.rabbithole.qrscanner.data.prefs.SharedPreferencesManager
import com.rabbithole.qrscanner.data.prefs.SharedPreferencesManagerImpl

class ScanFragment : Fragment() {

    private lateinit var resultText: TextView
    private lateinit var typeText: TextView
    private lateinit var scanButton: ImageButton
    private lateinit var photoButton: ImageButton
    private lateinit var copyButton: ImageButton
    private lateinit var shareButton: ImageButton
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var layout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var lets: TextView
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferencesManager = SharedPreferencesManagerImpl(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_scan, container, false)

        resultText = view.findViewById(R.id.result_txt)
        scanButton = view.findViewById(R.id.scan_bt)
        photoButton = view.findViewById(R.id.photo_bt)
        copyButton = view.findViewById(R.id.copy_bt)
        shareButton = view.findViewById(R.id.share_bt)
        typeText = view.findViewById(R.id.type_txt)
        layout = view.findViewById(R.id.layout)
        lets = view.findViewById(R.id.lets)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val elements = sharedPreferencesManager.getAllElements()
        adapter = MyAdapter(elements, requireContext())
        recyclerView.adapter = adapter

        if (adapter.itemCount > 0) {
            lets.visibility = GONE
        }

        scanButton.setOnClickListener {
            startCameraScanning()
        }

        photoButton.setOnClickListener {
            selectPhoto()
        }

        shareButton.setOnClickListener {
            shareText()
        }

        copyButton.setOnClickListener {
            val textToCopy = resultText.text.toString()
            val clipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("label", textToCopy)
            clipboardManager.setPrimaryClip(clipData)
        }

        val bottomSheetView: View = view.findViewById(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.peekHeight = 250

        return view
    }

    private fun shareText() {
        val textToShare = resultText.text.toString()
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare)
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun startCameraScanning() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setBeepEnabled(false)
        integrator.setPrompt("Scan a QR code")
        integrator.initiateScan()
    }

    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { imageUri ->
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val result = scanQRCodeFromBitmap(bitmap)
                    fillAdapter(requireView())
                    if (result.isNotEmpty()) {
                        handleScannedResult(result)
                    } else {
                        resultText.text = requireContext().getText(R.string.error_noImg)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    resultText.text = requireContext().getText(R.string.empty_error)
                }
            }
        }

        else if (requestCode == IntentIntegrator.REQUEST_CODE) {
            val scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (scanningResult != null) {
                if (scanningResult.contents == null) {
                    resultText.text = requireContext().getText(R.string.empty_error)
                } else {
                    layout.setPadding(0, 0, 0, 200)
                    resultText.text = scanningResult.contents
                    resultText.autoLinkMask = Linkify.WEB_URLS
                    resultText.movementMethod = LinkMovementMethod.getInstance()

                    sharedPreferencesManager.saveElement(scanningResult.contents)

                    fillAdapter(requireView())

                    if(isTextLink(scanningResult.contents)){
                        typeText.text = requireContext().getText(R.string.type_link)
                    }
                    else  typeText.text = requireContext().getText(R.string.type_text)
                    expandBottomSheet()
                }
            }
        }

    }

    fun fillAdapter(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val elements = sharedPreferencesManager.getAllElements()

        adapter = MyAdapter(elements, requireContext())
        recyclerView.adapter = adapter

        if(adapter.itemCount > 0){
            lets.visibility = GONE
        }
    }

    private fun scanQRCodeFromBitmap(bitmap: Bitmap): String {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val source: LuminanceSource = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        return try {
            val result = MultiFormatReader().decode(binaryBitmap)
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun handleScannedResult(scannedResult: String) {
        layout.setPadding(0, 0, 0, 200)
        resultText.text = scannedResult
        resultText.autoLinkMask = Linkify.WEB_URLS
        resultText.movementMethod = LinkMovementMethod.getInstance()

        sharedPreferencesManager.saveElement(scannedResult)
        val elements = sharedPreferencesManager.getAllElements()
        adapter.updateElements(elements)

        if (isTextLink(scannedResult)) {
            typeText.text = requireContext().getText(R.string.type_link)
        } else {
            typeText.text = requireContext().getText(R.string.type_text)
        }

        expandBottomSheet()
    }

    private fun isTextLink(input: String): Boolean {
        return android.util.Patterns.WEB_URL.matcher(input).matches()
    }

    private fun expandBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}
