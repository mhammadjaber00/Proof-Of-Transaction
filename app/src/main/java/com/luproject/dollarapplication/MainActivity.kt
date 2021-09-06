package com.luproject.dollarapplication

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.luproject.dollarapplication.adapter.TransactionsAdapter
import com.luproject.dollarapplication.fragments.AddTransactionFragment
import com.luproject.dollarapplication.models.Transaction
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var  launcher: ActivityResultLauncher<Uri>

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(t: Transaction) {
        ( rvTransactions.adapter as TransactionsAdapter).addTransaction(t)
        rvTransactions.smoothScrollToPosition(0)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState==null) {
            setContentView(R.layout.activity_main)

            launcher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
                if (success) {
                    // The image was saved into the given Uri -> do something with it
                }
            }


            fabAddTransaction.setOnClickListener {
                Dexter.withContext(this)
                        .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                                val f = AddTransactionFragment()
                                f.show(supportFragmentManager, "TAG")
                            }

                            override fun onPermissionRationaleShouldBeShown(p0: MutableList<PermissionRequest>?, p1: PermissionToken?) {
                                p1?.continuePermissionRequest()
                            }

                        }).check()
            }
            val g = Gson()
            val shr = getSharedPreferences("trans", MODE_PRIVATE)
            val data = shr.getString("transes", null)

            rvTransactions.adapter = TransactionsAdapter(ArrayList()){
                val f = AddTransactionFragment()
                f.transaction = it
                f.show(supportFragmentManager, "TAG")
            }
            if (data == null) {
                tvEmptyList.visibility = View.VISIBLE
                val emptyAnim = AnimationUtils.makeInAnimation(this, true)
                tvEmptyList.startAnimation(emptyAnim)
            } else {
                val dataList = g.fromJson<List<Transaction>>(data, object : TypeToken<List<Transaction?>?>() {}.type)
                if (dataList.isEmpty()) {
                    tvEmptyList.visibility = View.VISIBLE
                    val emptyAnim = AnimationUtils.makeInAnimation(this, true)
                    tvEmptyList.startAnimation(emptyAnim)
                } else {
                    tvEmptyList.visibility = View.GONE
                    rvTransactions.adapter = TransactionsAdapter((dataList.asReversed()).toMutableList() as java.util.ArrayList<Transaction>){
                        val f = AddTransactionFragment()
                        f.transaction = it
                        f.show(supportFragmentManager, "TAG")
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);

    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }
}