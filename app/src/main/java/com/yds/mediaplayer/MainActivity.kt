package com.yds.mediaplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.yds.aliyunvideocommon.base.AlivcWheelDialogFragment
import com.yds.mediaplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var databinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initView()
    }

    private fun initView() {

        databinding.dialog.setOnClickListener {
            val dialog = MyDialogFragment()
            dialog.show(supportFragmentManager)
        }

        databinding.wheelView.setOnClickListener {
            val intent = Intent(this,WheelViewActivity::class.java)
            startActivity(intent)
        }

        databinding.alivcWheelDialogFragment.setOnClickListener {
            val dialog = AlivcWheelDialogFragment.Builder(supportFragmentManager)
                .setWheelData(arrayOf("one","two","three","four"))
                .cancelString("取消")
                .sureString("确定")
                .onWheelDialogListener(object:AlivcWheelDialogFragment.OnWheelDialogListener{
                    override fun onClickLeft(dialog: DialogFragment?, value: String?) {
                        Toast.makeText(this@MainActivity,"$value",Toast.LENGTH_SHORT).show()
                    }

                    override fun onClickRight(dialog: DialogFragment?, value: String?) {
                        Toast.makeText(this@MainActivity,"$value",Toast.LENGTH_SHORT).show()
                    }

                    override fun onValueChanged(dialog: DialogFragment?, value: String?) {
                        Toast.makeText(this@MainActivity,"$value",Toast.LENGTH_SHORT).show()
                    }

                })
                .create()
            dialog.show()
        }

        databinding?.alphaInAnimation?.setOnClickListener {
            val intent = Intent(this@MainActivity,AlphaInAnimActivity::class.java)
            startActivity(intent)
        }

        databinding.aliyunRenderView.setOnClickListener {
            val intent = Intent(this@MainActivity,AliyunRenderViewActivity::class.java)
            startActivity(intent)
        }


    }


}