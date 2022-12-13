package com.trill.ecommerce.screens.history

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andremion.counterfab.CounterFab
import com.gkemon.XMLtoPDF.PdfGenerator
import com.gkemon.XMLtoPDF.PdfGenerator.XmlToPDFLifecycleObserver
import com.gkemon.XMLtoPDF.PdfGeneratorListener
import com.gkemon.XMLtoPDF.model.FailureResponse
import com.gkemon.XMLtoPDF.model.SuccessResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.itextpdf.text.Document
import com.trill.ecommerce.R
import com.trill.ecommerce.adapter.ReceiptAdapter
import com.trill.ecommerce.databinding.FragmentReceiptBinding

import com.trill.ecommerce.model.HistoryModel
import com.trill.ecommerce.model.OrderModel
import com.trill.ecommerce.util.ui.LoadingFragment
import kotlin.math.roundToInt


@Suppress("InvalidSetHasFixedSize")
class ReceiptFragment : Fragment() {

    private var _binding: FragmentReceiptBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingFragmentHelper: LoadingFragment.LoadingFragmentHelper

    private lateinit var auth: FirebaseAuth

    private var totalAmountTextView: TextView? = null
    private var subTotalAmountTextView: TextView? = null
    private var taxTextView: TextView? = null
    private var dateTextView: TextView? = null
    private var timeTextView: TextView? = null
    private var receiptNoTextView: TextView? = null

    private lateinit var historyViewModel: HistoryModel
    
    private var button: MaterialButton? = null

    private var recyclerView: RecyclerView? = null
    var receiptAdapter: ReceiptAdapter? = null

    private val STORAGE_CODE = 1001
    private val TAG= "PERMISSION_TAG"

    private var xmlToPDFLifecycleObserver: XmlToPDFLifecycleObserver? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hideBottomNavView()
        hideFAB()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReceiptBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initViews(root)

        onBackButtonPressed(root)

        historyViewModel = ViewModelProvider(this)[HistoryModel::class.java]
        historyViewModel.getMutableLiveDataHistoryItem().observe(viewLifecycleOwner, Observer {
            populateFields(it)
            val adapter = ReceiptAdapter(requireContext(), it.cartItemList!!)
            recyclerView!!.adapter = adapter
        })



        return root

    }

    fun initViews(root: View?){
        loadingFragmentHelper =
            LoadingFragment.LoadingFragmentHelper(requireActivity().supportFragmentManager)

        auth = Firebase.auth

        xmlToPDFLifecycleObserver = XmlToPDFLifecycleObserver(requireActivity())
        lifecycle.addObserver(xmlToPDFLifecycleObserver!!)

        totalAmountTextView = root!!.findViewById(R.id.totalAmount)
        subTotalAmountTextView = root.findViewById(R.id.subTotalAmount)
        taxTextView = root.findViewById(R.id.taxAmount)
        button = root.findViewById(R.id.button)

        timeTextView = root.findViewById(R.id.time)
        dateTextView = root.findViewById(R.id.date)
        receiptNoTextView = root.findViewById(R.id.receiptNo)

        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerView!!.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView!!.layoutManager = layoutManager
        
        button!!.setOnClickListener {
            if (checkPermission()){
                Log.d(TAG, "Permission already granted, do the rest: ")
                downloadReceipt(root)
            }else{
                Log.d(TAG, "Permission already granted, request permission: ")
                requestPermission()
            }
        }
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android R or Above
            try {
                Log.d(TAG, "requestPermission:try")

                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this@ReceiptFragment.toString(), null)
                intent.data = uri

                storageActivityResultLauncher.launch(intent)

            }catch (e: Exception){
                Log.e(TAG, "requestPermission:catch", e)

                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION

                storageActivityResultLauncher.launch(intent)
            }
        }else{
            //Below Android R
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_CODE)

        }
    }

    private fun downloadReceipt(root: View) {
            savePDF()
    }

    private fun savePDF() {

        val inflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as (LayoutInflater)

        PdfGenerator.getBuilder()
            .setContext(requireActivity())
            .fromLayoutXMLSource()
            .fromLayoutXML(R.layout.fragment_receipt)
           // .setDefaultPageSize(PdfGenerator.PageSize.A4)
            .setFileName("Test-PDF")
            .savePDFSharedStorage(xmlToPDFLifecycleObserver)
            .actionAfterPDFGeneration(PdfGenerator.ActionAfterPDFGeneration.OPEN)
            .build(object : PdfGeneratorListener() {
                override fun onFailure(failureResponse: FailureResponse) {
                    super.onFailure(failureResponse)
                    Toast.makeText(requireContext(), "${failureResponse.errorMessage.toString()}", Toast.LENGTH_LONG).show()

                    Log.d(TAG, "${failureResponse.errorMessage.toString()}")
                }

                override fun onStartPDFGeneration() {}
                override fun onFinishPDFGeneration() {}
                override fun showLog(log: String) {
                    super.showLog(log)
                }

                override fun onSuccess(response: SuccessResponse) {
                    super.onSuccess(response)
                    Toast.makeText(requireContext(), "${response.toString()}", Toast.LENGTH_SHORT).show()
                }
            })


        val document = Document()
        val fileName = "TestTest"
        val filePath = Environment.getExternalStorageDirectory().toString()  + "/" + fileName + ".pdf"


        /*
        try {
            PdfWriter.getInstance(document, FileOutputStream(filePath))
            document.open()

            val data = "This is a test....."
            document.add(Paragraph(data))
            document.close()

            Toast.makeText(requireContext(), "Pdf saved in $filePath",Toast.LENGTH_SHORT ).show()

        }catch (e: java.lang.Exception){
            Toast.makeText(requireContext(), e.message.toString(),Toast.LENGTH_SHORT ).show()
        } */
    }

    private fun populateFields(it: OrderModel) {

        val subtotal = it.totalPayment!!.toInt() / 1.16
        val tax = it.totalPayment!!.toInt() - subtotal

        totalAmountTextView!!.text = "Ksh${"%, d".format(it.totalPayment)}" //it.totalPayment.toString()
        subTotalAmountTextView!!.text = "Ksh${"%, d".format(subtotal.roundToInt())}"  //subtotal.roundToInt().toString()
        taxTextView!!.text = "Ksh${"%, d".format(tax.roundToInt())}" //tax.roundToInt().toString()

        dateTextView!!.text = it.date
        timeTextView!!.text = it.time

        receiptNoTextView!!.text = it.transactionId

        it.cartItemList

    }

    private fun hideFAB() {
        val fab: CounterFab = requireActivity().findViewById(R.id.counterFab)
        fab.visibility = View.GONE
    }

    private fun onBackButtonPressed(root: View?) {
        val backPressedButton: ImageView = root!!.findViewById(R.id.backPressed)
        backPressedButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDetach() {
        super.onDetach()
        showBottomNavView()
    }

    private fun showBottomNavView() {
        val navbar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navbar.visibility = View.VISIBLE

    }

    private fun hideBottomNavView() {
        val navbar: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navbar.visibility = View.GONE

    }

    private fun showLoading(isLoading: Boolean) {
        // binding.buttonPrimary.isEnabled = !isLoading
        loadingFragmentHelper.showLoading(isLoading)
    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.d(TAG, "storageActivityResultLauncher: ")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Above Android 11
            if (Environment.isExternalStorageManager()){
                Log.d(TAG, "storageActivityResultLauncher: ")
                savePDF()
            }else{
                Log.d(TAG, "storageActivityResultLauncher: Manage external storage permission denied")
                Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }else{
            //Below Android 11


        }
    }

    private fun checkPermission(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Above Android R (11)
            Environment.isExternalStorageManager()
        }else{
            //Below Android 11
            val write = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_CODE){
            if (grantResults.isNotEmpty()){

                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED

                if (write && read){
                    Log.d(TAG, "onRequestPermissionResult: ")
                    savePDF()
                }else {
                    Log.d(TAG, "onRequestPermissionResult: ")
                    Toast.makeText(requireContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}