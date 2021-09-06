package com.luproject.dollarapplication.fragments

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toFile
import androidx.core.text.isDigitsOnly
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.luproject.dollarapplication.Cache
import com.luproject.dollarapplication.R
import com.luproject.dollarapplication.adapter.PhotoAdapter
import com.luproject.dollarapplication.models.Transaction
import com.williamww.silkysignature.views.SignaturePad
import kotlinx.android.synthetic.main.cell_transaction.*
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.fragment_add_transaction.tvLatitude
import kotlinx.android.synthetic.main.fragment_add_transaction.tvLongitude
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*


class AddTransactionFragment : BottomSheetDialogFragment(),Listener {

    private val imagesList  = ArrayList<String>()
    private var signaturePath  = ""
    private val serials  = ArrayList<String>()
    lateinit var location:Location
    var currency = ""
    lateinit var transaction: Transaction

    private lateinit var easyWayLocation: EasyWayLocation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheetInternal = d.findViewById<View>(R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheetInternal!!).peekHeight = Resources.getSystem().displayMetrics.heightPixels
        }
        return inflater.inflate(R.layout.fragment_add_transaction, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if(::transaction.isInitialized) {
            editTextTextPersonName.setText(transaction.from)
            editTextTextPersonName2.setText(transaction.amount.toString())
            editTextDate.setText(transaction.date)
            editTextTextPersonName2.isEnabled = false
            editTextTextPersonName.isEnabled = false
            tvLatitude.setText(transaction.lat.toString())
            tvLongitude.setText(transaction.lng.toString())
            rvPhotos.adapter =
                PhotoAdapter(transaction.imagePaths.map { Uri.parse(it) } as ArrayList<Uri>)

            btnAddPic.visibility = View.GONE
            tvBtn.text = "Close"
            btnSave.setOnClickListener {
                this.dismiss()
            }
            textView23.text = "View Transaction"
            signatureImage.visibility = View.VISIBLE
            signatureImage.setImageURI(Uri.parse(transaction.signaturePath))
            signature_pad.visibility = View.GONE
            var serials = ""
            transaction.serials.forEach {
                serials+="$it,"
            }
            tvSerials.text = serials

            btnViewLocation.setOnClickListener {
                val uri = java.lang.String.format(Locale.ENGLISH, "geo:%f,%f", transaction.lat, transaction.lng)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
               requireActivity().startActivity(intent)
            }

        }else{
            signature_pad.setOnSignedListener(object: SignaturePad.OnSignedListener{
                override fun onStartSigning() {

                }

                override fun onSigned() {
                    signaturePath = "-"
                }

                override fun onClear() {

                }

            })
            btnViewLocation.visibility = View.GONE
            signatureImage.visibility = View.GONE
            btnAddPic.setOnClickListener {
                ImagePicker.with(this)
                    .crop()	    			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start()
            }

            rvPhotos.adapter = PhotoAdapter(ArrayList())

            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val currentDate = sdf.format(Date())
            editTextDate.setText(currentDate)

            easyWayLocation = EasyWayLocation(requireActivity(), true, true, this)

            easyWayLocation.startLocation()
            btnSave.setOnClickListener {
                if(::location.isInitialized && imagesList.isNotEmpty() ) {
                    if(signaturePath.isEmpty()){
                        Toast.makeText(requireContext(),"Cannot add transaction without signing",Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    val g = Gson()
                    val shr = requireActivity().getSharedPreferences("trans", MODE_PRIVATE)
                    val data = shr.getString("transes", null)
                    if (data == null) {
                        val bmp = signature_pad.signatureBitmap

                        val f =Cache().saveImgToCache(
                            bmp,
                            "signature${editTextTextPersonName.text.toString()}",
                            requireContext()
                        )
                        val t = Transaction(
                            editTextTextPersonName.text.toString(),
                            location.latitude,
                            location.longitude,
                            imagesList,
                            editTextDate.text.toString(),
                            "${Uri.fromFile(f).path}/signature${editTextTextPersonName.text.toString()}.png",
                            editTextTextPersonName2.text.toString().toDouble(),
                            currency,
                            serials
                        )
                        val dataList: ArrayList<Transaction> = ArrayList<Transaction>()
                        dataList.add(t)
                        shr.edit().putString("transes", g.toJson(dataList)).apply()

                        EventBus.getDefault().post(t)



                    } else {
                        val bmp = signature_pad.signatureBitmap
                        val f =Cache().saveImgToCache(
                            bmp,
                            "signature${editTextTextPersonName.text.toString()}",
                            requireContext()
                        )
                        val t = Transaction(
                            editTextTextPersonName.text.toString(),
                            location.latitude,
                            location.longitude,
                            imagesList,
                            editTextDate.text.toString(),
                            "${Uri.fromFile(f).path}/signature${editTextTextPersonName.text.toString()}.png",
                            editTextTextPersonName2.text.toString().toDouble(),
                            currency,
                            serials
                        )
                        val dataList = g.fromJson<ArrayList<Transaction>>(
                            data,
                            object : TypeToken<ArrayList<Transaction?>?>() {}.type
                        )
                        dataList.add(t)
                        shr.edit().putString("transes", g.toJson(dataList)).apply()
                        EventBus.getDefault().post(t)


                    }
                    this.dismiss()
                }else{
                    Toast.makeText(
                        requireContext(),
                        "Please add images or wait for location to appear",
                        Toast.LENGTH_SHORT
                    ).show()
                }



            }
        }

    }


    override fun onResume() {
        super.onResume()
        if(::easyWayLocation.isInitialized)
             easyWayLocation.startLocation()
    }

    override fun onPause() {
        super.onPause()
        if(::easyWayLocation.isInitialized)
            easyWayLocation.endUpdates()
    }



    override fun locationOn() {
        Toast.makeText(requireContext(), "Location On", Toast.LENGTH_SHORT).show()

    }

    override fun currentLocation(location: Location?) {
        if(location!=null){
            tvLatitude.text = location.latitude.toString()
            tvLongitude.text = location.longitude.toString()
            this.location = location
        }

    }


    override fun locationCancelled() {
        Toast.makeText(requireContext(), "Location Cancelled", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!
                imagesList.add(uri.toString())
                (rvPhotos.adapter as PhotoAdapter).addPhoto(uri)
                runTextRecognition(BitmapFactory.decodeFile(uri.toFile().absolutePath))
                // Use Uri object instead of File to avoid storage permissions

            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {
                Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun runTextRecognition(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                processTextRecognitionResult(texts)
            }
            .addOnFailureListener { e -> // Task failed with an exception
                e.printStackTrace()
            }
    }

    private fun processTextRecognitionResult(texts: Text) {
        val blocks: List<Text.TextBlock> = texts.textBlocks
        if (blocks.isEmpty()) {
            return
        }
        for (i in blocks) {
            val lines: List<Text.Line> = i.lines
            for (j in lines) {
                var noSpaces = ""
                for(c in 0..j.text.length-1){
                    if(j.text[c].toString()!=" ")
                        noSpaces+=j.text[c]
                }
                if(noSpaces.length==12 && noSpaces[0].toString().isDigitsOnly().not() && noSpaces[1].toString().isDigitsOnly().not() && noSpaces[10].toString().isDigitsOnly() &&  noSpaces.subSequence(
                        2,
                        9
                    ).isDigitsOnly() ){
                    currency = "EUR"
                    if(tvSerials.text.toString() == ""){
                        tvSerials.text = "$noSpaces"

                    }else{
                        tvSerials.text = "${tvSerials.text},$noSpaces"
                    }
                    serials.add(noSpaces)
                    Log.d("FOUND_EURO","EUROOOOO $noSpaces")

                    return
                }

                if(noSpaces.length==11 && noSpaces[0].toString().isDigitsOnly().not() && noSpaces[1].toString().isDigitsOnly().not() && noSpaces[10].toString().isDigitsOnly().not() &&  noSpaces.subSequence(
                        2,
                        9
                    ).isDigitsOnly() ){
                    currency = "USD"
                    if(tvSerials.text.toString() == ""){
                        tvSerials.text = "$noSpaces"

                    }else{
                        tvSerials.text = "${tvSerials.text},$noSpaces"
                    }
                    serials.add(noSpaces)
                    return
                }

            }
        }
    }
}