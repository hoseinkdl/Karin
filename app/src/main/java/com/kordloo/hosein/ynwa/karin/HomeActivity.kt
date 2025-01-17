package com.kordloo.hosein.ynwa.karin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.kordloo.hosein.ynwa.karin.adapter.CustomerAdapter
import com.kordloo.hosein.ynwa.karin.db.CustomerDAO
import com.kordloo.hosein.ynwa.karin.dialog.CustomerDialogFragment
import com.kordloo.hosein.ynwa.karin.event.CustomerEvent
import com.kordloo.hosein.ynwa.karin.model.Customer
import com.kordloo.hosein.ynwa.karin.util.Keys
import kotlinx.android.synthetic.main.activity_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : AppCompatActivity(), View.OnClickListener{

    private val adapter = CustomerAdapter()
    private val customerDAO = CustomerDAO()
    private var isEdit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        init()
    }

    private fun init() {
        recycler.layoutManager = LinearLayoutManager(this)
        adapter.setList(customerDAO.findAll()!!)
        // Go to next page
        adapter.setOnItemListener(object: OnItemListener<Customer> {
            override fun onClicked(customer: Customer) {
                AlertDialog.Builder(this@HomeActivity)
                    .setTitle(customer.name)
                    .setPositiveButton("فاکتور های قبلی") { dialog, which ->
                        goToArchiveListActivity(customer.id)
                        dialog.dismiss()
                    }
                    .setNegativeButton("خرید") { dialog, which ->
                        val intent = Intent(this@HomeActivity, ShopActivity::class.java)
                        intent.putExtra(Keys.CUSTOMER, customer)
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    .show()
            }
        })
        // Edit listener
        adapter.setOnEditListener(object: OnItemListener<Customer> {
            override fun onClicked(customer: Customer) {
                isEdit = true
                openCustomerDialog(customer)
            }
        })
        // Long click for remove item
        adapter.setOnLongItemListener(object: OnLongItemListener<Customer, Int> {
            override fun onClicked(customer: Customer, position: Int) {
                AlertDialog.Builder(this@HomeActivity)
                    .setMessage("مطمئنی میخوای حذف کنی ؟")
                    .setPositiveButton("حذف کن") { dialog, which ->
                        customerDAO.delete(customer.id)
                        adapter.remove(customer, position)
                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()

            }

        })
        recycler.adapter = adapter

        fab.setOnClickListener(this)
        ivAddWare.setOnClickListener(this)
        ivGallery.setOnClickListener(this)
    }

    private fun goToArchiveListActivity(customerId: Long) {
        val i = Intent(this, ArchiveListActivity::class.java)
        i.putExtra(Keys.CUSTOMER_ARCHIVE, 1000)
        i.putExtra(Keys.CUSTOMER, customerId)
        startActivity(i)
    }

    private fun openCustomerDialog(customer: Customer = Customer()) {
        val TAG = "dialog_customer"
        val fm = supportFragmentManager
        val ft = supportFragmentManager.beginTransaction()

        val fragment = fm.findFragmentByTag(TAG)
        if (fragment != null)
            ft.remove(fragment)

        val customerDialogFragment = CustomerDialogFragment()
        if (isEdit) {
            val b = Bundle()
            b.putParcelable(Keys.CUSTOMER, customer)
            customerDialogFragment.arguments = b
        }
        customerDialogFragment.show(ft, TAG)
    }

    override fun onClick(v: View?) {
        when (v) {
            fab -> {
                openCustomerDialog()
            }
            ivAddWare -> {
                val i = Intent(this, AddImageActivity::class.java)
                startActivity(i)
            }
            ivGallery -> {
                val i = Intent(this, GalleryActivity::class.java)
                startActivity(i)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventCustomer(customerEvent: CustomerEvent) {
        customerDAO.save(customerEvent.customer)
        adapter.addCustomer(customerEvent.customer)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        customerDAO.close()
    }
}
