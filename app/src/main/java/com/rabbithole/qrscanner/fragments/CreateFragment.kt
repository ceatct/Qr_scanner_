package com.rabbithole.qrscanner.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rabbithole.factory.CreateViewModelFactory
import com.rabbithole.qrscanner.R
import com.rabbithole.qrscanner.fragments.models.CreateViewModel
import com.rabbithole.qrscanner.repositories.QrRepository
import com.rabbithole.qrscanner.repositories.QrRepositoryImpl

class CreateFragment : Fragment() {

    private lateinit var input: EditText
    private lateinit var result: ImageView
    private lateinit var confirm: ImageButton
    private lateinit var share: ImageButton
    private lateinit var save: ImageButton
    private lateinit var layout: LinearLayout

    private lateinit var viewModel: CreateViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_create, container, false)

        val repository: QrRepository = QrRepositoryImpl()
        viewModel = ViewModelProvider(this, CreateViewModelFactory(repository))[CreateViewModel::class.java]

        input = view.findViewById(R.id.input_edt)
        result = view.findViewById(R.id.result_img)
        confirm = view.findViewById(R.id.confirm_bt)
        share = view.findViewById(R.id.share_bt)
        save = view.findViewById(R.id.save_bt)
        layout = view.findViewById(R.id.layout)

        confirm.setOnClickListener {
            val text = input.text.toString()
            viewModel.createQrCode(text)
        }

        save.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
            viewModel.saveImageToExternalStorage(requireContext(), result)
        }

        share.setOnClickListener {
            viewModel.shareImageFromImageView(requireContext(), result)
        }

        viewModel.qrData.observe(viewLifecycleOwner) { qrData ->
            result.setImageBitmap(repository.creteQr(qrData.text, 512))
            layout.visibility = VISIBLE
        }

        return view
    }
}